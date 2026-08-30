package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;

import java.lang.reflect.Field;

public class Fullbright extends Feature {

    private static final double FULLBRIGHT = 16.0;
    private double savedGamma = 1.0;

    private static Field gammaValueField;
    static {
        try {
            gammaValueField = SimpleOption.class.getDeclaredField("value");
            gammaValueField.setAccessible(true);
        } catch (Exception ignored) {}
    }

    public Fullbright() {
        super("fullbright", "Fullbright", Category.VISUAL, false);
    }

    @Override
    public void onEnable(MinecraftClient client) {
        savedGamma = client.options.getGamma().getValue();
        setGammaRaw(client, FULLBRIGHT);
    }

    @Override
    public void onDisable(MinecraftClient client) {
        setGammaRaw(client, savedGamma);
    }

    private static void setGammaRaw(MinecraftClient client, double value) {
        if (gammaValueField == null) return;
        try {
            gammaValueField.set(client.options.getGamma(), value);
        } catch (Exception ignored) {}
    }
}
