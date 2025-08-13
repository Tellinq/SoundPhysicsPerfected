package com.redsmods.sound_physics_perfected.wrappers;

import com.redsmods.sound_physics_perfected.RaycastingHelper;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;
import net.minecraft.client.sound.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import static com.redsmods.sound_physics_perfected.RaycastingHelper.TICK_RATE;

@Getter
public class RedTickableInstance implements TickableSoundInstance {
    private final Identifier id;
    private final Sound sound;
    private final SoundCategory category;
    private final Vec3d originalPosition;
    private final float originalVolume;
    @Delegate private SoundInstance wrapped;
    private double x;
    private double y;
    private double z;
    @Setter private boolean done;
    private float volume;
    private float pitch;
    private int tickCount;
    @Setter private Vec3d targetPosition;
    private float targetVolume;

    public RedTickableInstance(Identifier id, Sound sound, SoundCategory category, Vec3d position, float volume, float pitch, SoundInstance wrapped, Vec3d originalPosition, float originalVolume) {
        this.id = id;
        this.sound = sound;
        this.category = category;
        this.x = position.x;
        this.y = position.y;
        this.z = position.z;
        this.done = false;
        this.volume = volume;
        this.pitch = pitch;
        this.wrapped = wrapped;
        this.originalPosition = originalPosition;
        this.originalVolume = originalVolume;
        tickCount = 0;
        targetPosition = position;
        targetVolume = volume;
    }

    @Override
    public void tick() {
        tickCount++;
        if (done || TICK_RATE == 0) return; // DONE or ticking sounds is off
        if (tickCount % TICK_RATE == 0) // only update once every .1 second
            RaycastingHelper.tickQueue.add(this);
        if (wrapped instanceof TickableSoundInstance)
            ((TickableSoundInstance) wrapped).tick();
        updatePos();
        updateVolume();
    }

    @Override
    public AttenuationType getAttenuationType() {
        return AttenuationType.NONE;
    }

    public void stop() {
        this.done = true;
    }

    public void updatePos() {
        // Calculate the direction vector to the target
        double deltaX = targetPosition.getX() - x;
        double deltaY = targetPosition.getY() - y;
        double deltaZ = targetPosition.getZ() - z;

        // Calculate the distance to the target
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
        double maxSpeed = 17.15; // m per tick, speed of sound

        // If we're already at the target or very close, set position directly
        if (distance <= 0.001 || distance > maxSpeed * TICK_RATE) {
            x = targetPosition.getX();
            y = targetPosition.getY();
            z = targetPosition.getZ();
            return;
        }

        // Maximum speed in blocks per tick

        // Calculate how far we can move this tick
        double moveDistance = Math.min(maxSpeed, distance);

        // Normalize the direction vector and scale by move distance
        double moveX = (deltaX / distance) * moveDistance;
        double moveY = (deltaY / distance) * moveDistance;
        double moveZ = (deltaZ / distance) * moveDistance;

        // Update position
        x += moveX;
        y += moveY;
        z += moveZ;
    }

    public void setVolume(float targetVolume) {
        if (!this.id.toString().contains("rain")) {
            this.targetVolume = targetVolume;
        }
    }
    public void updateVolume() {
        // Calculate the difference between current and target volume
        float deltaVolume = targetVolume - volume;
        float maxVolumeChange = Math.abs(deltaVolume / TICK_RATE);

        // If we're already at the target or very close, set volume directly
        if (Math.abs(deltaVolume) <= 0.001f || Math.abs(deltaVolume) > maxVolumeChange * TICK_RATE) {
            volume = targetVolume;
            return;
        }

        // Maximum volume change per tick

        // Calculate how much we can change this tick
        float volumeChange = Math.min(maxVolumeChange, Math.abs(deltaVolume));

        // Apply the change in the correct direction
        if (deltaVolume > 0) {
            volume += volumeChange;
        } else {
            volume -= volumeChange;
        }
    }

    @Override
    public Sound getSound() {
        return this.sound;
    }
}