package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class SpeedDisplay extends Feature {

    public SpeedDisplay() {
        super("speed", "Speed Display", Category.HUD, false);
        setHudPositionable(340, 4);
    }

    @Override
    public void renderHud(GuiGraphicsExtractor ctx, Font font, int sw, int sh) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        double vx  = client.player.getDeltaMovement().x;
        double vz  = client.player.getDeltaMovement().z;
        double bps = Math.sqrt(vx * vx + vz * vz) * 20.0;
        String text = String.format("%.2f BPS", bps);
        ctx.text(font, text, getHudX(), getHudY(), 0xFFFFFFFF, true);
    }

    @Override public int hudBoxW() { return 62; }
    @Override public int hudBoxH() { return 10; }
}
