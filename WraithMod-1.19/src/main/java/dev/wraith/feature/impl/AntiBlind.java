package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffects;

public class AntiBlind extends Feature {

    public AntiBlind() {
        super("antiblind", "Anti Blind", Category.VISUAL, false);
    }

    @Override
    public void tick(MinecraftClient client) {
        if (client.player == null) return;
        if (client.player.hasStatusEffect(StatusEffects.BLINDNESS))
            client.player.removeStatusEffect(StatusEffects.BLINDNESS);
        if (client.player.hasStatusEffect(StatusEffects.DARKNESS))
            client.player.removeStatusEffect(StatusEffects.DARKNESS);
    }
}
