package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

public class ArmorStatus extends Feature {

    private static final EquipmentSlot[] SLOTS  = {
        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };
    private static final String[] LABELS = {"H", "C", "L", "B"};

    public ArmorStatus() {
        super("armor", "Armor Status", Category.HUD, true);
        setHudPositionable(680, 120);
    }

    @Override
    public void renderHud(DrawContext ctx, TextRenderer tr, int sw, int sh) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        int baseY = getHudY();
        int baseX = getHudX();
        for (int i = 0; i < SLOTS.length; i++) {
            ItemStack stack = client.player.getEquippedStack(SLOTS[i]);
            if (stack.isEmpty()) continue;

            int maxDur = stack.getMaxDamage();
            if (maxDur == 0) continue;
            int dur = maxDur - stack.getDamage();
            float pct = (float) dur / maxDur;

            int color = pct > 0.5f ? 0xFF55FF55 : pct > 0.25f ? 0xFFFFFF55 : 0xFFFF5555;
            String text = LABELS[i] + ": " + dur + "/" + maxDur;
            ctx.drawTextWithShadow(tr, text, baseX, baseY + i * 10, color);
        }
    }

    @Override public int hudBoxW() { return 80; }
    @Override public int hudBoxH() { return 40; }
}
