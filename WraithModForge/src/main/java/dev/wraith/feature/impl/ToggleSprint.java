package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.Minecraft;

public class ToggleSprint extends Feature {
    public ToggleSprint() { super("togglesprint", "Toggle Sprint", Category.MOVEMENT, false); }

    @Override
    public void tick(Minecraft mc) {
        if (mc.player == null || mc.screen != null) return;
        if (!mc.player.isSprinting()
                && mc.options.keyUp.isDown()
                && !mc.player.isCrouching()
                && mc.player.onGround()) {
            mc.player.setSprinting(true);
        }
    }
}
