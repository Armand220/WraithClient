package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class SpeedDisplay extends Feature {
    public SpeedDisplay() { super("speed", "Speed Display", Category.HUD, false); }

    @Override
    public void renderHud(GuiGraphics g, Font font, int sw, int sh) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        double vx = mc.player.getDeltaMovement().x;
        double vz = mc.player.getDeltaMovement().z;
        String text = String.format("%.2f BPS", Math.sqrt(vx*vx + vz*vz) * 20.0);
        g.drawString(font, text, sw / 2 - font.width(text) / 2, 4, 0xFFFFFFFF);
    }
}
