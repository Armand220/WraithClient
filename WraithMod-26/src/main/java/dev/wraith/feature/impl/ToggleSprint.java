package dev.wraith.feature.impl;

import dev.wraith.WraithModClient;
import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.Minecraft;

public class ToggleSprint extends Feature {

    public ToggleSprint() {
        super("togglesprint", "Toggle Sprint", Category.MOVEMENT, false);
    }

    @Override
    public void tick(Minecraft client) {
        if (client.player == null || WraithModClient.isScreenOpen) return;
        if (!client.player.isSprinting()
                && client.options.keyUp.isDown()
                && !client.player.isShiftKeyDown()
                && client.player.onGround()) {
            client.player.setSprinting(true);
        }
    }
}
