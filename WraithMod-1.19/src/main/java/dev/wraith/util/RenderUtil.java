package dev.wraith.util;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;

// Exposes DrawableHelper.fill (protected static) for use outside Screen subclasses.
public final class RenderUtil extends DrawableHelper {
    private RenderUtil() {}

    public static void fill(MatrixStack stack, int x1, int y1, int x2, int y2, int color) {
        DrawableHelper.fill(stack, x1, y1, x2, y2, color);
    }
}
