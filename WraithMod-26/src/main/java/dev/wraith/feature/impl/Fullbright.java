package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;

import java.lang.reflect.Field;

public class Fullbright extends Feature {

    private static final double FULLBRIGHT = 16.0;
    private double savedGamma = 1.0;

    private static Field gammaValueField;
    static {
        try {
            gammaValueField = OptionInstance.class.getDeclaredField("value");
            gammaValueField.setAccessible(true);
        } catch (Exception ignored) {}
    }

    public Fullbright() {
        super("fullbright", "Fullbright", Category.VISUAL, false);
    }

    @Override
    public void onEnable(Minecraft client) {
        Object v = client.options.gamma().get();
        savedGamma = v instanceof Number ? ((Number) v).doubleValue() : 1.0;
        setGammaRaw(client, FULLBRIGHT);
    }

    @Override
    public void onDisable(Minecraft client) {
        setGammaRaw(client, savedGamma);
    }

    private static void setGammaRaw(Minecraft client, double value) {
        if (gammaValueField == null) return;
        try {
            gammaValueField.set(client.options.gamma(), value);
        } catch (Exception ignored) {}
    }
}
