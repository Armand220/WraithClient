package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.MinecraftClient;

public class NoHurtCam extends Feature {

    public NoHurtCam() {
        super("nohurtcam", "No Hurt Cam", Category.VISUAL, false);
    }

    @Override
    public void tick(MinecraftClient client) {
        if (client.player == null) return;
        // Zeroing hurtTime prevents the camera tilt that happens after taking damage.
        client.player.hurtTime = 0;
    }
}
