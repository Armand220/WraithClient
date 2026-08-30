package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.Minecraft;

public class NoHurtCam extends Feature {

    public NoHurtCam() {
        super("nohurtcam", "No Hurt Cam", Category.VISUAL, false);
    }

    @Override
    public void tick(Minecraft client) {
        if (client.player == null) return;
        client.player.hurtTime = 0;
    }
}
