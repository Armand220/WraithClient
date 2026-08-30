package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EntityHealthBars extends Feature {

    public EntityHealthBars() {
        super("entityhealthbars", "Entity Health Bars", Category.HUD, false);
    }

    @Override
    public void renderHud(GuiGraphicsExtractor ctx, Font font, int sw, int sh) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || dev.wraith.WraithModClient.isScreenOpen) return;

        List<LivingEntity> nearby = new ArrayList<>();
        mc.level.entitiesForRendering().forEach(entity -> {
            if (!(entity instanceof LivingEntity living)) return;
            if (entity == mc.player || living.isDeadOrDying()) return;
            if (living.distanceToSqr(mc.player) > 400) return;
            nearby.add(living);
        });

        nearby.sort(Comparator.comparingDouble(e -> e.distanceToSqr(mc.player)));
        int max = Math.min(nearby.size(), 8);

        int barW = 50;
        int barH = 4;
        int rowH = 14;
        int x = 4;
        int y = sh / 2 - (max * rowH) / 2;

        for (int i = 0; i < max; i++) {
            LivingEntity e = nearby.get(i);
            float hp  = e.getHealth();
            float mhp = e.getMaxHealth();
            float pct = hp / mhp;

            String name = e.getType().getDescription().getString();
            int barColor = pct > 0.5f ? 0xFF55FF55 : pct > 0.25f ? 0xFFFFAA00 : 0xFFFF5555;

            ctx.text(font, name, x, y, 0xFFCCCCCC, true);
            int by = y + 9;
            ctx.fill(x, by, x + barW, by + barH, 0xAA000000);
            ctx.fill(x, by, x + (int)(barW * pct), by + barH, barColor);
            ctx.text(font, String.format("%.0f/%.0f", hp, mhp), x + barW + 3, by - 1, 0xFF888888, true);
            y += rowH;
        }
    }
}
