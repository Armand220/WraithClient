package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;

public class PingDisplay extends Feature {
    public PingDisplay() { super("ping", "Ping", Category.HUD, false); }

    @Override
    public void renderHud(GuiGraphics g, Font font, int sw, int sh) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) return;
        PlayerInfo info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
        if (info == null) return;
        int ping = info.getLatency();
        int color = ping < 100 ? 0xFF55FF55 : ping < 200 ? 0xFFFFFF55 : 0xFFFF5555;
        String text = ping + " ms";
        g.drawString(font, text, sw - font.width(text) - 4, 4, color);
    }
}
