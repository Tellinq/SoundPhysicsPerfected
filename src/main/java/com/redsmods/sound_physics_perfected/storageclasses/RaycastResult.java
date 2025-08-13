package com.redsmods.sound_physics_perfected.storageclasses;

import lombok.AllArgsConstructor;
import net.minecraft.util.math.Vec3d;

@AllArgsConstructor
public class RaycastResult {
    public final double totalDistance;
    public final Vec3d initialDirection;
    public final SoundData hitEntity;
}

