package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;

import java.time.Instant;

public class AutoGG extends Feature {

    private boolean wasAlive = true;
    private long    lastSent = 0;

    public AutoGG() {
        super("autogg", "Auto GG", Category.UTILITY, false);
    }

    @Override
    public void tick(MinecraftClient client) {
        if (client.player == null) return;
        boolean alive = !client.player.isDead();
        if (wasAlive && !alive) {
            long now = System.currentTimeMillis();
            // Debounce: don't spam if respawning quickly
            if (now - lastSent > 5000) {
                client.player.networkHandler.sendChatMessage("gg");
                lastSent = now;
            }
        }
        wasAlive = alive;
    }
}
