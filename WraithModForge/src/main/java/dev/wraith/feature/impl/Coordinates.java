package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class Coordinates extends Feature {
    public Coordinates() { super("coords", "Coordinates", Category.HUD, true); }

    @Override
    public void renderHud(GuiGraphics g, Font font, int sw, int sh) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        String line = String.format("X: %.1f  Y: %.1f  Z: %.1f",
            mc.player.getX(), mc.player.getY(), mc.player.getZ());
        g.drawString(font, line, 4, sh - 24, 0xFFFFFFFF);
    }
}
