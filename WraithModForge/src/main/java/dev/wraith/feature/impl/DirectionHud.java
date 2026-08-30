package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class DirectionHud extends Feature {
    private static final String[] DIRS = {"S","SW","W","NW","N","NE","E","SE"};
    public DirectionHud() { super("direction", "Direction", Category.HUD, false); }

    @Override
    public void renderHud(GuiGraphics g, Font font, int sw, int sh) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        float yaw = mc.player.getYRot() % 360;
        if (yaw < 0) yaw += 360;
        int idx = (int) ((yaw + 22.5f) / 45f) % 8;
        String text = DIRS[idx] + " (" + Math.round(yaw) + "°)";
        g.drawString(font, text, 4, sh - 13, 0xFFAAAAAA);
    }
}
