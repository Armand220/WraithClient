package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffects;

public class AntiNausea extends Feature {

    public AntiNausea() {
        super("antinausea", "Anti Nausea", Category.VISUAL, false);
    }

    @Override
    public void tick(MinecraftClient client) {
        if (client.player == null) return;
        if (client.player.hasStatusEffect(StatusEffects.NAUSEA))
            client.player.removeStatusEffect(StatusEffects.NAUSEA);
    }
}
