package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class SpeedDisplay extends Feature {

    public SpeedDisplay() {
        super("speed", "Speed Display", Category.HUD, false);
        setHudPositionable(340, 4);
    }

    @Override
    public void renderHud(DrawContext ctx, TextRenderer tr, int sw, int sh) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        double vx  = client.player.getVelocity().x;
        double vz  = client.player.getVelocity().z;
        double bps = Math.sqrt(vx * vx + vz * vz) * 20.0;

        String text = String.format("%.2f BPS", bps);
        ctx.drawTextWithShadow(tr, text, getHudX(), getHudY(), 0xFFFFFFFF);
    }

    @Override public int hudBoxW() { return 62; }
    @Override public int hudBoxH() { return 10; }
}
