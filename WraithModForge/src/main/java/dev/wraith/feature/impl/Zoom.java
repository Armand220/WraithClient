package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;

import java.lang.reflect.Field;

public class Zoom extends Feature {
    private static final int ZOOM_FOV = 10;
    private int savedFov = 70;
    private static Field valueField;
    static {
        try {
            valueField = OptionInstance.class.getDeclaredField("value");
            valueField.setAccessible(true);
        } catch (Exception ignored) {}
    }

    public Zoom() { super("zoom", "Zoom", Category.VISUAL, false); }

    @Override
    public void onEnable(Minecraft mc) {
        if (valueField == null) return;
        try {
            savedFov = (int) valueField.get(mc.options.fov());
            valueField.set(mc.options.fov(), ZOOM_FOV);
        } catch (Exception ignored) {}
    }

    @Override
    public void onDisable(Minecraft mc) {
        if (valueField == null) return;
        try { valueField.set(mc.options.fov(), savedFov); } catch (Exception ignored) {}
    }
}
