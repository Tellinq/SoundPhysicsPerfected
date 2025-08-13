package com.redsmods.sound_physics_perfected.storageclasses;

import lombok.AllArgsConstructor;
import net.minecraft.util.math.Vec3d;

@AllArgsConstructor
public class RayHitData {
    public final RaycastResult rayResult;
    public final Vec3d direction;
    public final double weight; // Based on inverse square law
}
