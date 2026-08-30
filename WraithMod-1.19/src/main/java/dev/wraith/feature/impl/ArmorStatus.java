package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

public class ArmorStatus extends Feature {

    private static final EquipmentSlot[] SLOTS = {
        EquipmentSlot.HEAD,
        EquipmentSlot.CHEST,
        EquipmentSlot.LEGS,
        EquipmentSlot.FEET
    };
    private static final String[] LABELS = {"H", "C", "L", "B"};

    public ArmorStatus() {
        super("armor", "Armor Status", Category.HUD, true);
    }

    @Override
    public void renderHud(MatrixStack stack, TextRenderer tr, int sw, int sh) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        int y = sh - 50;
        for (int i = 0; i < SLOTS.length; i++) {
            ItemStack item = client.player.getEquippedStack(SLOTS[i]);
            if (item.isEmpty()) continue;

            int maxDur = item.getMaxDamage();
            if (maxDur == 0) continue;
            int dur = maxDur - item.getDamage();
            float pct = (float) dur / maxDur;

            int color = pct > 0.5f ? 0xFF55FF55 : pct > 0.25f ? 0xFFFFFF55 : 0xFFFF5555;
            String text = LABELS[i] + ": " + dur + "/" + maxDur;
            tr.drawWithShadow(stack, text, sw - 80, y + i * 10, color);
        }
    }
}
