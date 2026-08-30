package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;

public class PingDisplay extends Feature {

    public PingDisplay() {
        super("ping", "Ping", Category.HUD, false);
        setHudPositionable(700, 4);
    }

    @Override
    public void renderHud(DrawContext ctx, TextRenderer tr, int sw, int sh) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.getNetworkHandler() == null) return;

        PlayerListEntry entry = client.getNetworkHandler()
            .getPlayerListEntry(client.player.getUuid());
        if (entry == null) return;

        int ping = entry.getLatency();
        int color = ping < 100 ? 0xFF55FF55 : ping < 200 ? 0xFFFFFF55 : 0xFFFF5555;
        String text = ping + " ms";
        ctx.drawTextWithShadow(tr, text, getHudX(), getHudY(), color);
    }

    @Override public int hudBoxW() { return 50; }
    @Override public int hudBoxH() { return 10; }
}
