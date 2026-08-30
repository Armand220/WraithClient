package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class FpsCounter extends Feature {

    public FpsCounter() {
        super("fps", "FPS Counter", Category.HUD, true);
        setHudPositionable(4, 4);
    }

    @Override
    public void renderHud(GuiGraphicsExtractor ctx, Font font, int sw, int sh) {
        int fps = Minecraft.getInstance().getFps();
        String text = fps + " FPS";
        int color = fps >= 60 ? 0xFF55FF55 : fps >= 30 ? 0xFFFFFF55 : 0xFFFF5555;
        ctx.text(font, text, getHudX(), getHudY(), color, true);
    }

    @Override public int hudBoxW() { return 55; }
    @Override public int hudBoxH() { return 10; }
}
