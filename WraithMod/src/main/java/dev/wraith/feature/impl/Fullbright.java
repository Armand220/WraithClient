package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class Fullbright extends Feature {

    private static final double FULLBRIGHT = 16.0;
    // Same reflection workaround as Zoom: find the value field structurally
    // because Loom remaps field names to intermediary but not string literals.
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

    private double savedGamma = 1.0;

    public Fullbright() {
        super("fullbright", "Fullbright", Category.VISUAL, false);
    }

    @Override
    public void onEnable(MinecraftClient client) {
        if (VALUE_FIELD == null) return;
        try {
            savedGamma = client.options.getGamma().getValue();
            VALUE_FIELD.set(client.options.getGamma(), FULLBRIGHT);
        } catch (Exception ignored) {}
    }

    @Override
    public void onDisable(MinecraftClient client) {
        if (VALUE_FIELD == null) return;
        try {
            VALUE_FIELD.set(client.options.getGamma(), savedGamma);
        } catch (Exception ignored) {}
    }
}
