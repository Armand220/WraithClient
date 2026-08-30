package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;

public class PingDisplay extends Feature {

    public PingDisplay() {
        super("ping", "Ping", Category.HUD, false);
    }

    @Override
    public void renderHud(MatrixStack stack, TextRenderer tr, int sw, int sh) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.getNetworkHandler() == null) return;

        PlayerListEntry entry = client.getNetworkHandler()
            .getPlayerListEntry(client.player.getUuid());
        if (entry == null) return;

        int ping = entry.getLatency();
        int color = ping < 100 ? 0xFF55FF55 : ping < 200 ? 0xFFFFFF55 : 0xFFFF5555;
        String text = ping + " ms";
        tr.drawWithShadow(stack, text, sw - tr.getWidth(text) - 4, 4, color);
    }
}
