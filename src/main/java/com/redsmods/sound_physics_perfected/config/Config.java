package com.redsmods.sound_physics_perfected.config;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.autogen.AutoGen;
import dev.isxander.yacl3.config.v2.api.autogen.CustomDescription;
import dev.isxander.yacl3.config.v2.api.autogen.CustomName;
import dev.isxander.yacl3.config.v2.api.autogen.MasterTickBox;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import dev.isxander.yacl3.platform.YACLPlatform;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;

public class Config {
    public static final ConfigClassHandler<Config> CONFIG = ConfigClassHandler.createBuilder(Config.class)
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(YACLPlatform.getConfigDir().resolve("SoundPhysicsPerfected2.json5"))
                    .setJson5(true)
                    .build())
            .build();

    public static Screen configScreen(@Nullable Screen parent) {
        return CONFIG.generateGui().generateScreen(parent);
    }

    public static Config instance() {
        return CONFIG.instance();
    }

    @AutoGen(category = "general", group = "main")
    @MasterTickBox({ "hitbox", "onlyTargetPlayers", "onlyTargetPlayers", "showWhileHit", "cubeSize", "distance", "entityLimit", "fillColor" })
    @CustomName("optimalaim.config.enabled.name")
    @CustomDescription("optimalaim.config.enabled.description")
    @SerialEntry(comment = "This option disables all the other options in this group")
    public boolean enabled = true;
}
