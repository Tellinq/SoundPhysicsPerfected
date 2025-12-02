package com.redsmods.sound_physics_perfected.mixin.client;

import com.mojang.blaze3d.audio.Channel;
import com.redsmods.sound_physics_perfected.RedSoundInstance;
import com.redsmods.sound_physics_perfected.ReverbHelpers.LegacyReverb;
import com.redsmods.sound_physics_perfected.config.Config;
import com.redsmods.sound_physics_perfected.config.DebugType;
import com.redsmods.sound_physics_perfected.storageclasses.SoundData;
import com.redsmods.sound_physics_perfected.wrappers.RedPermeatedSoundInstance;
import com.redsmods.sound_physics_perfected.wrappers.RedPositionedSoundInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC10;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Map;

import static com.redsmods.sound_physics_perfected.RaycastingHelper.*;

@Mixin(SoundEngine.class)
public abstract class Mixin_SoundSystem {

    @Final
    @Shadow
    private SoundManager soundManager;
    @Final
    @Shadow
    private Map<SoundInstance, ChannelAccess.ChannelHandle> instanceToChannel;


    @Inject(
            method = "play",
            at = @At("HEAD"),
            cancellable = true)
    private void sound_physics_perfected$onSoundPlay(
            SoundInstance sound,
            //? if <1.21.6 {
            /*CallbackInfo ci
            *///?} else {
            CallbackInfoReturnable<SoundEngine.PlayResult> cir
            //?}
    ) {
        if (!fxHandler.efxInitialized) {
            fxHandler.initializeReverb();
        }

        if (sound_physics_perfected$isSoundBlacklisted(sound.toString())) return;

        if (!fxHandler.efxInitialized) return;

        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null || client.level == null || sound == null || soundManager == null) {
            return;
        }

        try {
            if (!(sound instanceof RedPositionedSoundInstance || sound instanceof TickableSoundInstance || sound instanceof RedPermeatedSoundInstance) && sound.getAttenuation() != SoundInstance.Attenuation.NONE) { // !replayList.contains(redSoundData)
                double soundX = sound.getX();
                double soundY = sound.getY();
                double soundZ = sound.getZ();
                Vec3 soundPos = new Vec3(soundX, soundY, soundZ);

                String soundId = sound.getLocation().toString();

                RedSoundInstance redSoundData = new RedSoundInstance(sound);
                SoundData soundData = new SoundData(redSoundData, soundPos, soundId);

                soundQueue.offer(soundData);

                while (soundQueue.size() > Config.getInstance().maxSounds) {
                    soundQueue.poll();
                }

                /*? if >= 1.21.6 {*/ cir.cancel(); /*?} else {*/ /*ci.cancel(); *//*?}*/
            } else if (Config.getInstance().permeation && sound instanceof RedPermeatedSoundInstance) {
                FXQueue.add((RedPermeatedSoundInstance) sound);
            }
        } catch (Exception e) {
            System.err.println("Error tracking sound: " + e.getMessage());
        }
    }

    @Inject(
            method = "tick(Z)V",
            at = @At(value = "INVOKE",
                    //? if >=1.21.6
                    target = "Lnet/minecraft/client/sounds/SoundEngine;tickInGameSound()V",
                    //? if <1.21.6
                    /*target = "Lnet/minecraft/client/sounds/SoundEngine;tickNonPaused()V",*/
                    shift = At.Shift.AFTER),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void sound_physics_perfected$onSoundTick(boolean paused, CallbackInfo ci) {
        if (!fxHandler.efxInitialized) {
            fxHandler.initializeReverb();
        }

        if (!fxHandler.efxInitialized) {
            return;
        }

        if (paused) {
            return;
        }

        while(!FXQueue.isEmpty()) {
            try {
                RedPermeatedSoundInstance sound = FXQueue.poll();
                ChannelAccess.ChannelHandle manager = instanceToChannel.get(sound);
                SourceManagerAccessor accessor = (SourceManagerAccessor) manager;
                Channel source = accessor.sound_physics_perfected$getChannel();
                int id = ((SourceAccessor) source).sound_physics_perfected$getSource();
                sound.setSource(id);
                fxHandler.applyMuffleToSource(id,sound.getPermeationIndex());
            } catch (Exception e) {
                if (Config.getInstance().debug != DebugType.OFF)
                    System.out.println("sourceID is invalid for a sound, non-issue" + e);
            }
        }

        if (Config.getInstance().reverb) {
            sound_physics_perfected$updateActiveSources(); // Brute force reverb to ALL sounds
        }
    }

    @ModifyVariable(method = "stop(Lnet/minecraft/client/resources/sounds/SoundInstance;)V", at = @At("HEAD"), argsOnly = true)
    private SoundInstance sound_physics_perfected$modifySoundParameter(SoundInstance sound) {
        if (!fxHandler.efxInitialized) {
            return sound; // fx aren't init, most likely permeation isn't playing
        }
        if(sound == null) {
            return sound; // sorry, if some other mod kills their sound by using a mixin, i am not finna be held responsible, that's their own fault.
        }
        soundQueue.remove(sound);
        SoundInstance customSound = soundInstanceMap.get(sound);
        RedPermeatedSoundInstance soundPermeation = soundPermInstanceMap.get(sound);
        if (soundPermeation != null)
            soundPermeation.setStopped(true);
        soundInstanceMap.remove(sound);
        soundPermInstanceMap.remove(sound);

        ChannelAccess.ChannelHandle sourceManager = instanceToChannel.get(soundPermeation);
        if (sourceManager != null) {
            sourceManager.execute(Channel::stop);
        }

        ChannelAccess.ChannelHandle sourceManagerNormal = instanceToChannel.get(customSound);
        if (sourceManagerNormal != null) {
            sourceManagerNormal.execute(Channel::stop);
        }

        // Return the custom sound if it exists, otherwise return the original
        return sound; // if its null, welp. minecraft's code does handle if the custom sound ended up being ended properly so it should be fine, will fix null pointers hopefully though :)
    }

    // Add method to clean up orphaned sounds
    @Inject(method = "stopAll()V", at = @At("HEAD"))
    private void sound_physics_perfected$onStopAll(CallbackInfo ci) {
        soundQueue.clear();
    }

    /**
     * Update all currently active sources with reverb
     * This is a brute-force approach that works when source tracking is difficult
     */
    @Unique
    private static void sound_physics_perfected$updateActiveSources() {
        long context = ALC10.alcGetCurrentContext();
        if (context == 0) {
            return;
        }

        AL10.alGetError();

        try {
            // Get all generated OpenAL sources and apply reverb
            // This requires keeping track of source IDs or iterating through all possible sources

            // Brute force approach - check source IDs 1-256 (typical range)
            for (int sourceId = 1; sourceId <= 256; sourceId++) {
                if (AL10.alIsSource(sourceId)) {
                    int state = AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE);
                    if (state == AL10.AL_PLAYING || state == AL10.AL_PAUSED) {
                        if (Config.getInstance().legacyReverb == LegacyReverb.VERSION140)
                            fxHandler.applyLegacyReverbToSource(sourceId);
                        else if (Config.getInstance().legacyReverb == LegacyReverb.VERSION100) {
                            fxHandler.applyInitalLegacyReverbToSource(sourceId);
                        } else
                            fxHandler.applyReverbToSource(sourceId);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Inject(method = "destroy()V", at = @At("HEAD"))
    private void sound_physics_perfected$onAudioEngineStop(CallbackInfo ci) {
        fxHandler.cleanupEFXResources();
    }

    @Inject(method = "loadLibrary()V", at = @At("TAIL"))
    private void sound_physics_perfected$onAudioEngineStart(CallbackInfo ci) {
        fxHandler.efxInitialized = false;
        fxHandler.initializeReverb();
    }

    @Unique
    private boolean sound_physics_perfected$isSoundBlacklisted(String soundName) {
        if (soundName == null || soundName.isEmpty()) {
            return false;
        }

        List<String> blacklist = Config.getInstance().soundBlacklist;
        if (blacklist == null || blacklist.isEmpty()) {
            return false;
        }

        // Check if any blacklist entry is contained in the sound name
        return blacklist.stream()
                .filter(entry -> entry != null && !entry.trim().isEmpty()) // filter out null/empty entries
                .anyMatch(entry -> soundName.toLowerCase().contains(entry.toLowerCase().trim()));
    }
}