package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class FpsCounter extends Feature {
    public FpsCounter() { super("fps", "FPS Counter", Category.HUD, true); }

    @Override
    public void renderHud(GuiGraphics g, Font font, int sw, int sh) {
        int fps = Minecraft.getInstance().getFps();
        String text = fps + " FPS";
        int color = fps >= 60 ? 0xFF55FF55 : fps >= 30 ? 0xFFFFFF55 : 0xFFFF5555;
        g.drawString(font, text, 4, 4, color);
    }
}
