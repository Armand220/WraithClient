package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffects;

public class AntiBlind extends Feature {
    public AntiBlind() { super("antiblind", "Anti Blind", Category.VISUAL, false); }

    @Override
    public void tick(Minecraft mc) {
        if (mc.player == null) return;
        if (mc.player.hasEffect(MobEffects.BLINDNESS)) mc.player.removeEffect(MobEffects.BLINDNESS);
        if (mc.player.hasEffect(MobEffects.DARKNESS))  mc.player.removeEffect(MobEffects.DARKNESS);
    }
}
