package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffects;

public class AntiNausea extends Feature {

    public AntiNausea() {
        super("antinausea", "Anti Nausea", Category.VISUAL, false);
    }

    @Override
    public void tick(Minecraft client) {
        if (client.player == null) return;
        if (client.player.hasEffect(MobEffects.NAUSEA))
            client.player.removeEffect(MobEffects.NAUSEA);
    }
}
