package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;

import java.lang.reflect.Field;

public class Fullbright extends Feature {
    private static final double FULLBRIGHT = 16.0;
    private double savedGamma = 1.0;
    private static Field valueField;
    static {
        try {
            valueField = OptionInstance.class.getDeclaredField("value");
            valueField.setAccessible(true);
        } catch (Exception ignored) {}
    }

    public Fullbright() { super("fullbright", "Fullbright", Category.VISUAL, false); }

    @Override
    public void onEnable(Minecraft mc) {
        if (valueField == null) return;
        try {
            savedGamma = (double) valueField.get(mc.options.gamma());
            valueField.set(mc.options.gamma(), FULLBRIGHT);
        } catch (Exception ignored) {}
    }

    @Override
    public void onDisable(Minecraft mc) {
        if (valueField == null) return;
        try { valueField.set(mc.options.gamma(), savedGamma); } catch (Exception ignored) {}
    }
}
