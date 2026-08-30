package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffects;

public class AntiBlind extends Feature {

    public AntiBlind() {
        super("antiblind", "Anti Blind", Category.VISUAL, false);
    }

    @Override
    public void tick(Minecraft client) {
        if (client.player == null) return;
        if (client.player.hasEffect(MobEffects.BLINDNESS))
            client.player.removeEffect(MobEffects.BLINDNESS);
        if (client.player.hasEffect(MobEffects.DARKNESS))
            client.player.removeEffect(MobEffects.DARKNESS);
    }
}
