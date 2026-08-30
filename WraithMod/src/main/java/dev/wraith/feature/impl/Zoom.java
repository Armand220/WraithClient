package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class Zoom extends Feature {

    private static final int ZOOM_FOV = 10;
    // Loom remaps field names to intermediary at build time but does NOT remap
    // string literals, so getDeclaredField("value") fails at runtime.
    // Instead we find the field by structural inspection: the only private,
    // non-static, non-final Object field in SimpleOption is the value field.
    private static final Field VALUE_FIELD = findValueField();

    private static Field findValueField() {
        for (Field f : SimpleOption.class.getDeclaredFields()) {
            int m = f.getModifiers();
            if (Modifier.isPrivate(m) && !Modifier.isStatic(m) && !Modifier.isFinal(m)
                    && f.getType() == Object.class) {
                f.setAccessible(true);
                return f;
            }
        }
        return null;
    }

    private int savedFov = 70;

    public Zoom() {
        super("zoom", "Zoom", Category.VISUAL, false);
    }

    @Override
    public void onEnable(MinecraftClient client) {
        if (VALUE_FIELD == null) return;
        try {
            savedFov = client.options.getFov().getValue();
            VALUE_FIELD.set(client.options.getFov(), ZOOM_FOV);
        } catch (Exception ignored) {}
    }

    @Override
    public void onDisable(MinecraftClient client) {
        if (VALUE_FIELD == null) return;
        try {
            VALUE_FIELD.set(client.options.getFov(), savedFov);
        } catch (Exception ignored) {}
    }
}
