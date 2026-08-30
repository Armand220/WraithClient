package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.Minecraft;

public class AutoGG extends Feature {

    private boolean wasAlive = true;
    private long    lastSent = 0;

    public AutoGG() {
        super("autogg", "Auto GG", Category.UTILITY, false);
    }

    @Override
    public void tick(Minecraft client) {
        if (client.player == null) return;
        boolean alive = !client.player.isDeadOrDying();
        if (wasAlive && !alive) {
            long now = System.currentTimeMillis();
            if (now - lastSent > 5000) {
                client.player.connection.sendChat("gg");
                lastSent = now;
            }
        }
        wasAlive = alive;
    }
}
