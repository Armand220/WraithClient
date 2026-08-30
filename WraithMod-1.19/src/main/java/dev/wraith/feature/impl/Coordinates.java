package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;

public class Coordinates extends Feature {

    public Coordinates() {
        super("coords", "Coordinates", Category.HUD, true);
    }

    @Override
    public void renderHud(MatrixStack stack, TextRenderer tr, int sw, int sh) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        double x = client.player.getX();
        double y = client.player.getY();
        double z = client.player.getZ();

        String line = String.format("X: %.1f  Y: %.1f  Z: %.1f", x, y, z);
        tr.drawWithShadow(stack, line, 4, sh - 24, 0xFFFFFFFF);
    }
}
