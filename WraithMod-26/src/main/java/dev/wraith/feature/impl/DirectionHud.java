package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class DirectionHud extends Feature {

    private static final String[] DIRS = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};

    public DirectionHud() {
        super("direction", "Direction", Category.HUD, false);
        setHudPositionable(4, 170);
    }

    @Override
    public void renderHud(GuiGraphicsExtractor ctx, Font font, int sw, int sh) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        float yaw = client.player.getYRot() % 360;
        if (yaw < 0) yaw += 360;
        int idx = (int) ((yaw + 22.5f) / 45f) % 8;
        String text = DIRS[idx] + " (" + Math.round(yaw) + "°)";
        ctx.text(font, text, getHudX(), getHudY(), 0xFFAAAAAA, true);
    }

    @Override public int hudBoxW() { return 80; }
    @Override public int hudBoxH() { return 10; }
}
