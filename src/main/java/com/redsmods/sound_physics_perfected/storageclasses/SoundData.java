package com.redsmods.sound_physics_perfected.storageclasses;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.util.math.Vec3d;

// Inner class to store sound data
@AllArgsConstructor @EqualsAndHashCode
public class SoundData {
    public final SoundInstance sound;
    public final Vec3d position;
    public final String soundId;
}