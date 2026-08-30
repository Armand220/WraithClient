package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;

import java.lang.reflect.Field;

public class Zoom extends Feature {

    private static final int ZOOM_FOV = 10;

    private static Field gammaValueField;
    static {
        try {
            gammaValueField = SimpleOption.class.getDeclaredField("value");
            gammaValueField.setAccessible(true);
        } catch (Exception ignored) {}
    }

    private int savedFov = 70;

    public Zoom() {
        super("zoom", "Zoom", Category.VISUAL, false);
    }

    @Override
    public void onEnable(MinecraftClient client) {
        if (gammaValueField == null) return;
        try {
            savedFov = client.options.getFov().getValue();
            gammaValueField.set(client.options.getFov(), ZOOM_FOV);
        } catch (Exception ignored) {}
    }

    @Override
    public void onDisable(MinecraftClient client) {
        if (gammaValueField == null) return;
        try {
            gammaValueField.set(client.options.getFov(), savedFov);
        } catch (Exception ignored) {}
    }
}
