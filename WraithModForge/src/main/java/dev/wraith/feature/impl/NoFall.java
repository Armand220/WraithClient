package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class NoFall extends Feature {
    public NoFall() { super("nofall", "No Fall", Category.MOVEMENT, false); }

    @Override
    public void tick(Minecraft mc) {
        if (mc.player == null) return;
        if (!mc.player.hasEffect(MobEffects.SLOW_FALLING)) {
            mc.player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 40, 0, false, false, false));
        }
    }

    @Override
    public void onDisable(Minecraft mc) {
        if (mc.player != null) mc.player.removeEffect(MobEffects.SLOW_FALLING);
    }
}
