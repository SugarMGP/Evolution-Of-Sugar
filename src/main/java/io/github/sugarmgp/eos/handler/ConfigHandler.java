package io.github.sugarmgp.eos.handler;

import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigHandler {
    public static ForgeConfigSpec commonConfig;
    public static ForgeConfigSpec.BooleanValue torcherinoExploding;

    static {
        ForgeConfigSpec.Builder commonBuilder = new ForgeConfigSpec.Builder();
        commonBuilder
                .comment("General Settings")
                .comment("全局设置")
                .push("general")
        ;

        torcherinoExploding = commonBuilder
                .comment("Whether to explode when using the EOS Torcherino")
                .comment("是否在使用 EOS Torcherino 时产生爆炸")
                .define("torcherinoExploding", true)
        ;

        commonBuilder.pop();
        commonConfig = commonBuilder.build();
    }
}
