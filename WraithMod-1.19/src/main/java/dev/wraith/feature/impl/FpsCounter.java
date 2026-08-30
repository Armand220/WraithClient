package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import dev.wraith.util.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;

public class FpsCounter extends Feature {

    public FpsCounter() {
        super("fps", "FPS Counter", Category.HUD, true);
    }

    @Override
    public void renderHud(MatrixStack stack, TextRenderer tr, int sw, int sh) {
        int fps = MinecraftClient.getCurrentFps();
        String text = fps + " FPS";
        int color = fps >= 60 ? 0xFF55FF55 : fps >= 30 ? 0xFFFFFF55 : 0xFFFF5555;
        tr.drawWithShadow(stack, text, 4, 4, color);
    }
}
