package com.redsmods.sound_physics_perfected.mixin.client;

import com.redsmods.sound_physics_perfected.RaycastingHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class Mixin_ProcessPlayerRaycasts {

    private static int TICKS_SINCE_WORLD = 0;

    @Inject(method = "tick", at = @At("TAIL"))
    private void sound_physics_perfected$processPlayerSoundRaycasting(CallbackInfo ci) {

        Player player = (Player) (Object) this;
        Level world = player.level();

        // Only run on client side to avoid server lag
        if (!world.isClientSide()) {
            return;
        }

        RaycastingHelper.castBouncingRaysAndDetectSFX(world, player);
        RaycastingHelper.playQueuedObjects(++TICKS_SINCE_WORLD);
    }
}