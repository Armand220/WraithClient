package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;

public class SpeedDisplay extends Feature {

    public SpeedDisplay() {
        super("speed", "Speed Display", Category.HUD, false);
    }

    @Override
    public void renderHud(MatrixStack stack, TextRenderer tr, int sw, int sh) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        double vx  = client.player.getVelocity().x;
        double vz  = client.player.getVelocity().z;
        double bps = Math.sqrt(vx * vx + vz * vz) * 20.0;

        String text = String.format("%.2f BPS", bps);
        tr.drawWithShadow(stack, text, sw / 2 - tr.getWidth(text) / 2, 4, 0xFFFFFFFF);
    }
}
