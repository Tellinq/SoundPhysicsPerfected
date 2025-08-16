package com.redsmods.sound_physics_perfected.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MathUtil {

    /**
     * Restricts {@code value} so it lies between {@code min} and {@code max}.
     */
    public float clamp(float value, float min, float max) {
        return Math.min(Math.max(value, min), max);
    }
}

