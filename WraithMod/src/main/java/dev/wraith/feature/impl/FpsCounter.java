package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class FpsCounter extends Feature {

    public FpsCounter() {
        super("fps", "FPS Counter", Category.HUD, true);
        setHudPositionable(4, 4);
    }

    @Override
    public void renderHud(DrawContext ctx, TextRenderer tr, int sw, int sh) {
        int fps = MinecraftClient.getInstance().getCurrentFps();
        String text = fps + " FPS";
        int color = fps >= 60 ? 0xFF55FF55 : fps >= 30 ? 0xFFFFFF55 : 0xFFFF5555;
        ctx.drawTextWithShadow(tr, text, getHudX(), getHudY(), color);
    }

    @Override public int hudBoxW() { return 55; }
    @Override public int hudBoxH() { return 10; }
}
