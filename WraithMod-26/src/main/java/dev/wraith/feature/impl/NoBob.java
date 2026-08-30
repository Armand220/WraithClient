package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.Minecraft;

public class NoBob extends Feature {

    private boolean savedBob = true;

    public NoBob() {
        super("nobob", "No Bob", Category.VISUAL, false);
    }

    @Override
    public void onEnable(Minecraft client) {
        savedBob = client.options.bobView().get();
        client.options.bobView().set(false);
        client.options.save();
    }

    @Override
    public void onDisable(Minecraft client) {
        client.options.bobView().set(savedBob);
        client.options.save();
    }
}
