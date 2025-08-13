package com.redsmods.sound_physics_perfected;

import lombok.*;
import lombok.experimental.Delegate;
import net.minecraft.client.sound.SoundInstance;

@AllArgsConstructor @EqualsAndHashCode @ToString
public class RedSoundInstance implements SoundInstance {
    @Delegate
    @Getter
    @NonNull SoundInstance original;
}