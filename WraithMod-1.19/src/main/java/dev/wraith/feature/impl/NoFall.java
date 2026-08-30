package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class NoFall extends Feature {

    public NoFall() {
        super("nofall", "No Fall", Category.MOVEMENT, false);
    }

    @Override
    public void tick(MinecraftClient client) {
        if (client.player == null) return;
        if (!client.player.hasStatusEffect(StatusEffects.SLOW_FALLING)) {
            client.player.addStatusEffect(
                new StatusEffectInstance(StatusEffects.SLOW_FALLING, 40, 0, false, false, false));
        }
    }

    @Override
    public void onDisable(MinecraftClient client) {
        if (client.player != null)
            client.player.removeStatusEffect(StatusEffects.SLOW_FALLING);
    }
}
