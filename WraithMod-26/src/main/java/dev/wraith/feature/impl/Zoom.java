package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;

import java.lang.reflect.Field;

public class Zoom extends Feature {

    private static final int ZOOM_FOV = 10;
    private static Field fovValueField;
    static {
        try {
            fovValueField = OptionInstance.class.getDeclaredField("value");
            fovValueField.setAccessible(true);
        } catch (Exception ignored) {}
    }

    private int savedFov = 70;

    public Zoom() {
        super("zoom", "Zoom", Category.VISUAL, false);
    }

    @Override
    public void onEnable(Minecraft client) {
        if (fovValueField == null) return;
        try {
            Object v = client.options.fov().get();
            savedFov = v instanceof Number ? ((Number) v).intValue() : 70;
            fovValueField.set(client.options.fov(), ZOOM_FOV);
        } catch (Exception ignored) {}
    }

    @Override
    public void onDisable(Minecraft client) {
        if (fovValueField == null) return;
        try {
            fovValueField.set(client.options.fov(), savedFov);
        } catch (Exception ignored) {}
    }
}
