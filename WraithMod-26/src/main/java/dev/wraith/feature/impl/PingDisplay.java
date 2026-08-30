package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;

public class PingDisplay extends Feature {

    public PingDisplay() {
        super("ping", "Ping", Category.HUD, false);
        setHudPositionable(700, 4);
    }

    @Override
    public void renderHud(GuiGraphicsExtractor ctx, Font font, int sw, int sh) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.getConnection() == null) return;
        PlayerInfo entry = client.getConnection().getPlayerInfo(client.player.getUUID());
        if (entry == null) return;
        int ping = entry.getLatency();
        int color = ping < 100 ? 0xFF55FF55 : ping < 200 ? 0xFFFFFF55 : 0xFFFF5555;
        ctx.text(font, ping + " ms", getHudX(), getHudY(), color, true);
    }

    @Override public int hudBoxW() { return 50; }
    @Override public int hudBoxH() { return 10; }
}
