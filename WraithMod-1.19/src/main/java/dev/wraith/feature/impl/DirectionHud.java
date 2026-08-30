package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;

public class DirectionHud extends Feature {

    private static final String[] DIRS = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};

    public DirectionHud() {
        super("direction", "Direction", Category.HUD, false);
    }

    @Override
    public void renderHud(MatrixStack stack, TextRenderer tr, int sw, int sh) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        float yaw = client.player.getYaw() % 360;
        if (yaw < 0) yaw += 360;
        int idx = (int) ((yaw + 22.5f) / 45f) % 8;

        String text = DIRS[idx] + " (" + Math.round(yaw) + "°)";
        tr.drawWithShadow(stack, text, 4, sh - 13, 0xFFAAAAAA);
    }
}
