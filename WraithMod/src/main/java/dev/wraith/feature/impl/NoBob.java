package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.MinecraftClient;

public class NoBob extends Feature {

    private boolean savedBob = true;

    public NoBob() {
        super("nobob", "No Bob", Category.VISUAL, false);
    }

    @Override
    public void onEnable(MinecraftClient client) {
        savedBob = client.options.getBobView().getValue();
        client.options.getBobView().setValue(false);
        client.options.write();
    }

    @Override
    public void onDisable(MinecraftClient client) {
        client.options.getBobView().setValue(savedBob);
        client.options.write();
    }
}
