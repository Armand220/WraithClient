package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EntityHealthBars extends Feature {

    public EntityHealthBars() {
        super("entityhealthbars", "Entity Health Bars", Category.HUD, false);
    }

    @Override
    public void renderHud(DrawContext ctx, TextRenderer tr, int sw, int sh) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.currentScreen != null) return;

        List<LivingEntity> nearby = new ArrayList<>();
        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (entity == mc.player || living.isDead()) continue;
            double dx = entity.getX() - mc.player.getX();
            double dz = entity.getZ() - mc.player.getZ();
            if (dx * dx + dz * dz > 400) continue; // 20 block radius
            nearby.add(living);
        }

        nearby.sort(Comparator.comparingDouble(e -> e.squaredDistanceTo(mc.player)));
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

            String name = e.getType().getName().getString();
            int nameColor = 0xFFCCCCCC;
            int barColor  = pct > 0.5f ? 0xFF55FF55 : pct > 0.25f ? 0xFFFFAA00 : 0xFFFF5555;

            ctx.drawTextWithShadow(tr, name, x, y, nameColor);
            int bx = x;
            int by = y + 9;
            ctx.fill(bx, by, bx + barW, by + barH, 0xAA000000);
            ctx.fill(bx, by, bx + (int)(barW * pct), by + barH, barColor);
            ctx.drawTextWithShadow(tr, String.format("%.0f/%.0f", hp, mhp), bx + barW + 3, by - 1, 0xFF888888);

            y += rowH;
        }
    }
}
