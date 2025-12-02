package com.redsmods.sound_physics_perfected.mixin.client;

import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.redsmods.sound_physics_perfected.config.Config;
import net.minecraft.sounds.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SoundEvent.class)
public class Mixin_SoundMultiplier {

    @Expression("16.0")
    @ModifyExpressionValue(method = "getRange", at = @At("MIXINEXTRAS:EXPRESSION"))
    private float sound_physics_perfected$multiplySoundRange(float original) {
        return original * Config.getInstance().soundMult;
    }
}