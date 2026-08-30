package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffects;

public class AntiNausea extends Feature {
    public AntiNausea() { super("antinausea", "Anti Nausea", Category.VISUAL, false); }

    @Override
    public void tick(Minecraft mc) {
        if (mc.player == null) return;
        if (mc.player.hasEffect(MobEffects.CONFUSION)) mc.player.removeEffect(MobEffects.CONFUSION);
    }
}
