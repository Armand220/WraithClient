package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.MinecraftClient;

public class ToggleSprint extends Feature {

    public ToggleSprint() {
        super("togglesprint", "Toggle Sprint", Category.MOVEMENT, false);
    }

    @Override
    public void tick(MinecraftClient client) {
        if (client.player == null || client.currentScreen != null) return;
        if (!client.player.isSprinting()
                && client.options.forwardKey.isPressed()
                && !client.player.isSneaking()
                && client.player.isOnGround()) {
            client.player.setSprinting(true);
        }
    }
}
