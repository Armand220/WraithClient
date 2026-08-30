package dev.wraith.gui;

import dev.wraith.WraithModClient;
import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WraithMenuScreen extends Screen {

    // ── Layout ─────────────────────────────────────────────────────────────────
    private static final int W       = 800;
    private static final int H       = 490;
    private static final int TOP_H   = 44;    // top bar
    private static final int TAB_H   = 42;    // tab strip
    private static final int MARGIN  = 14;    // content margin
    private static final int COLS    = 3;
    private static final int CARD_GX = 8;     // horizontal card gap
    private static final int CARD_GY = 8;     // vertical card gap
    private static final int CARD_H  = 136;   // card height

    // ── Palette ────────────────────────────────────────────────────────────────
    // Panel layers — world stays visible through the content body
    private static final int C_TOP_BAR   = 0xEE070707;
    private static final int C_TAB_STRIP = 0xE8101010;
    private static final int C_BODY      = 0xC00D0D0D;  // 75% opaque — world visible between cards
    private static final int C_BORDER    = 0x25FFFFFF;
    private static final int C_SEP       = 0xFF1A1A1A;

    // Cards — fully opaque so text is crisp
    private static final int C_CARD      = 0xFF191919;
    private static final int C_CARD_HOV  = 0xFF222222;
    private static final int C_PREVIEW   = 0xFF121212;
    private static final int C_CLIP_BODY = 0xFF0D0D0D;  // matches C_BODY rgb for corner clips

    // Enable / disable button
    private static final int C_BTN_ON    = 0xFF1B5E20;  // dark green fill
    private static final int C_BTN_ON_H  = 0xFF2E7D32;  // green hover
    private static final int C_BTN_ON_T  = 0xFF81C784;  // green text
    private static final int C_BTN_OFF   = 0xFF161616;
    private static final int C_BTN_OFF_T = 0xFF484848;

    // Category tab pills
    private static final int C_TAB_ACT   = 0xFF2C2C2C;
    private static final int C_TAB_ACT_T = 0xFFEEEEEE;
    private static final int C_TAB_T     = 0xFF545454;
    private static final int C_TAB_HOV_T = 0xFF909090;

    // Text
    private static final int C_T_NAME  = 0xFFCCCCCC;
    private static final int C_T_DESC  = 0xFF555555;
    private static final int C_T_GHOST = 0xFF1E1E1E;

    // ── Animation ──────────────────────────────────────────────────────────────
    private float openAnim    = 0f;
    private float tabFade     = 1f;
    private Category pendingTab = null;
    private float scrollSmooth  = 0f;
    private int   scrollTarget  = 0;
    private int   maxScroll     = 0;
    private long  lastTime      = -1L;

    // per-id hover (0..1) and per-feature toggle knob position (0..1)
    private final Map<String, Float> hov = new HashMap<>();
    private final Map<String, Float> tog = new HashMap<>();

    private Category tab  = Category.HUD;
    private int      cardW = 0; // computed in render

    public WraithMenuScreen() { super(Component.literal("Wraith")); }

    @Override public boolean isPauseScreen() { return false; }

    @Override
    public void init() {
        super.init();
        openAnim = 0f;
        lastTime = -1L;
        WraithModClient.FEATURES.getAll().forEach(f ->
            tog.put(f.getId(), f.isEnabled() ? 1f : 0f));
    }

    // ── Render ─────────────────────────────────────────────────────────────────

    @Override
    public void extractRenderState(GuiGraphicsExtractor ctx, int mx, int my, float delta) {
        long now = System.currentTimeMillis();
        float dt = lastTime < 0L ? 0.016f : clamp01((now - lastTime) / 50f);
        lastTime = now;

        openAnim = lerp(openAnim, 1f, clamp01(0.55f * dt));

        if (pendingTab != null) {
            tabFade = lerp(tabFade, 0f, clamp01(0.60f * dt));
            if (tabFade < 0.04f) {
                tab = pendingTab; pendingTab = null;
                tabFade = 0f; scrollTarget = 0; scrollSmooth = 0f;
            }
        } else {
            tabFade = lerp(tabFade, 1f, clamp01(0.45f * dt));
        }

        scrollSmooth = lerp(scrollSmooth, scrollTarget, clamp01(0.30f * dt));
        int scroll = (int) scrollSmooth;

        float ease = easeOut(openAnim);
        int slideIn = (int)((1f - ease) * 20);
        int px = (width  - W) / 2;
        int py = (height - H) / 2 + slideIn;

        // card width computed from panel width
        int gridW = W - 2 * MARGIN;
        cardW = (gridW - (COLS - 1) * CARD_GX) / COLS;  // ≈ 252px

        // ── Drop shadow ────────────────────────────────────────────────────────
        ctx.fill(px + 10, py + 10, px + W + 10, py + H + 10, alpha(0, (int)(0x55 * ease)));
        ctx.fill(px + 5,  py + 5,  px + W + 5,  py + H + 5,  alpha(0, (int)(0x33 * ease)));

        // ── Top bar ────────────────────────────────────────────────────────────
        ctx.fill(px, py, px + W, py + TOP_H, recolor(C_TOP_BAR, (int)(0xEE * ease)));

        // ── Tab strip ──────────────────────────────────────────────────────────
        ctx.fill(px, py + TOP_H, px + W, py + TOP_H + TAB_H,
            recolor(C_TAB_STRIP, (int)(0xE8 * ease)));

        // ── Content body ───────────────────────────────────────────────────────
        ctx.fill(px, py + TOP_H + TAB_H, px + W, py + H,
            recolor(C_BODY, (int)(0xC0 * ease)));

        // ── Outer border ───────────────────────────────────────────────────────
        ctx.fill(px,         py,         px + W, py + 1,     C_BORDER);
        ctx.fill(px,         py + H - 1, px + W, py + H,     C_BORDER);
        ctx.fill(px,         py,         px + 1, py + H,     C_BORDER);
        ctx.fill(px + W - 1, py,         px + W, py + H,     C_BORDER);

        // ── Dividers ───────────────────────────────────────────────────────────
        ctx.fill(px, py + TOP_H,          px + W, py + TOP_H + 1,          C_SEP);
        ctx.fill(px, py + TOP_H + TAB_H,  px + W, py + TOP_H + TAB_H + 1, C_SEP);

        // ── Top bar content ────────────────────────────────────────────────────
        ctx.text(font, "WRAITH", px + 16, py + (TOP_H - 9) / 2, C_T_NAME, false);

        // Layout editor button
        String layLabel = "Edit Layout";
        int layW = font.width(layLabel) + 16;
        int clX = px + W - 34, clY = py + (TOP_H - 20) / 2;
        int layX = clX - layW - 8, layY = clY;
        boolean layHov = mx >= layX && mx < layX + layW && my >= layY && my < layY + 20;
        ctx.fill(layX, layY, layX + layW, layY + 20, layHov ? 0xFF1A2A1A : 0xFF141414);
        roundCorners(ctx, layX, layY, layW, 20, recolor(C_TOP_BAR, (int)(0xEE * ease)));
        ctx.text(font, layLabel, layX + 8, layY + (20 - 9) / 2,
            layHov ? 0xFF88FF88 : 0xFF555555, false);

        // Close [×]
        boolean cHov = mx >= clX && mx < clX + 20 && my >= clY && my < clY + 20;
        ctx.fill(clX, clY, clX + 20, clY + 20, cHov ? 0xFF3D1010 : 0xFF1C1C1C);
        roundCorners(ctx, clX, clY, 20, 20, recolor(C_TOP_BAR, (int)(0xEE * ease)));
        int xtw = font.width("x");
        ctx.text(font, "x", clX + (20 - xtw) / 2, clY + (20 - 9) / 2,
            cHov ? 0xFFFF7070 : 0xFF606060, false);

        // ── Tab pills ──────────────────────────────────────────────────────────
        int tpY  = py + TOP_H + 9;
        int tpH  = TAB_H - 18;  // pill height
        int tpX  = px + 16;
        for (Category c : Category.values()) {
            boolean act = (c == tab && pendingTab == null);
            int lw = font.width(c.label);
            int tw = lw + 22;

            boolean hovTab = mx >= tpX && mx < tpX + tw && my >= tpY && my < tpY + tpH;
            String key = "t_" + c.name();
            float ha = lerp(hov.getOrDefault(key, 0f), (hovTab && !act) ? 1f : 0f, clamp01(0.42f * dt));
            hov.put(key, ha);

            if (act) {
                ctx.fill(tpX, tpY, tpX + tw, tpY + tpH, C_TAB_ACT);
                roundCorners(ctx, tpX, tpY, tw, tpH, recolor(C_TAB_STRIP, (int)(0xE8 * ease)));
            } else if (ha > 0.002f) {
                ctx.fill(tpX, tpY, tpX + tw, tpY + tpH, alpha(0xFFFFFF, (int)(0x12 * ha)));
                roundCorners(ctx, tpX, tpY, tw, tpH, recolor(C_TAB_STRIP, (int)(0xE8 * ease)));
            }

            int tc = act ? C_TAB_ACT_T : lerpColor(C_TAB_T, C_TAB_HOV_T, ha);
            ctx.text(font, c.label, tpX + 11, tpY + (tpH - 9) / 2, tc, false);

            tpX += tw + 6;
        }

        // ── Card grid ──────────────────────────────────────────────────────────
        int contentTop = py + TOP_H + TAB_H + 2;
        int contentBot = py + H - 8;
        int contentH   = contentBot - contentTop;
        int fadeA = iclamp((int)(0xFF * tabFade), 0, 255);
        int slY   = (int)((1f - easeOut(tabFade)) * 14);

        List<Feature> list = WraithModClient.FEATURES.getAll().stream()
            .filter(f -> f.getCategory() == tab).collect(Collectors.toList());
        int rows       = (list.size() + COLS - 1) / COLS;
        int totalGridH = rows * CARD_H + Math.max(0, rows - 1) * CARD_GY;
        maxScroll  = Math.max(0, totalGridH - contentH + 16);
        scrollTarget = iclamp(scrollTarget, 0, maxScroll);

        ctx.enableScissor(px + MARGIN, contentTop, px + W - MARGIN, contentBot);

        for (int i = 0; i < list.size(); i++) {
            int col = i % COLS, row = i / COLS;
            int cx  = px + MARGIN + col * (cardW + CARD_GX);
            int cy  = contentTop - scroll + row * (CARD_H + CARD_GY) + slY;
            if (cy + CARD_H > contentTop && cy < contentBot)
                drawCard(ctx, list.get(i), cx, cy, cardW, mx, my, dt, fadeA);
        }

        ctx.disableScissor();

        // Gradient fade at list bottom
        for (int i = 0; i < 24; i++) {
            int a = (int)(0xD8 * (1f - (float) i / 24f));
            ctx.fill(px + MARGIN, contentBot - 24 + i, px + W - MARGIN, contentBot - 23 + i,
                alpha(C_CLIP_BODY & 0xFFFFFF, a));
        }

        // Scrollbar
        if (maxScroll > 0) {
            int sbx = px + W - 6;
            int thH = Math.max(28, contentH * contentH / (contentH + maxScroll));
            int thY = contentTop + (int)((float)(contentH - thH) * scrollTarget / maxScroll);
            ctx.fill(sbx, contentTop + 8, sbx + 3, contentBot - 8, 0xFF1C1C1C);
            ctx.fill(sbx, thY,            sbx + 3, thY + thH,       0xFF3A3A3A);
        }

        // Version stamp
        ctx.text(font, "v1.0", px + W - font.width("v1.0") - 14,
            py + (TOP_H - 9) / 2, C_T_GHOST, false);

        super.extractRenderState(ctx, mx, my, delta);
    }

    // ── Card ───────────────────────────────────────────────────────────────────

    private void drawCard(GuiGraphicsExtractor ctx, Feature f,
                          int x, int y, int w, int mx, int my,
                          float dt, int fadeA) {
        boolean on      = f.isEnabled();
        boolean hovered = mx >= x && mx < x + w && my >= y && my < y + CARD_H;
        String  id      = f.getId();

        float ha = lerp(hov.getOrDefault(id, 0f), hovered ? 1f : 0f, clamp01(0.38f * dt));
        hov.put(id, ha);
        float ta = lerp(tog.getOrDefault(id, on ? 1f : 0f), on ? 1f : 0f, clamp01(0.44f * dt));
        tog.put(id, ta);

        // Card body
        int cardBg = lerpColor(C_CARD, C_CARD_HOV, ha);
        ctx.fill(x, y, x + w, y + CARD_H, recolor(cardBg, fadeA));
        roundCorners(ctx, x, y, w, CARD_H, recolor(C_CLIP_BODY, fadeA));

        // Feature name (top)
        ctx.text(font, f.getName(), x + 10, y + 11,
            recolor(C_T_NAME, fadeA), false);

        // Preview area
        int pvX = x + 8, pvY = y + 27, pvW = w - 16, pvH = CARD_H - 60;
        ctx.fill(pvX, pvY, pvX + pvW, pvY + pvH, recolor(C_PREVIEW, fadeA));
        roundCorners(ctx, pvX, pvY, pvW, pvH, recolor(cardBg, fadeA));

        // Preview content — abbreviation badge + description
        String icn = icon(id);
        // Badge pill for abbreviation
        int badgeW = font.width(icn) + 12;
        int badgeX = pvX + (pvW - badgeW) / 2;
        int badgeY = pvY + (pvH - 9) / 2 - 8;
        ctx.fill(badgeX, badgeY, badgeX + badgeW, badgeY + 15, 0xFF1E1E1E);
        roundCorners(ctx, badgeX, badgeY, badgeW, 15, recolor(C_PREVIEW, fadeA));
        ctx.text(font, icn, badgeX + 6, badgeY + 3,
            recolor(lerpColor(0xFF404040, 0xFF909090, ta), fadeA), false);

        // Short description below badge
        String dsc = desc(id);
        if (!dsc.isEmpty()) {
            int dtw = font.width(dsc);
            // Truncate if too wide
            String display = dtw > pvW - 12 ? dsc.substring(0, Math.max(0, (pvW - 24) / 6)) + "..." : dsc;
            int ddtw = font.width(display);
            ctx.text(font, display, pvX + (pvW - ddtw) / 2, badgeY + 19,
                recolor(C_T_DESC, fadeA), false);
        }

        // ── Enable / disable button (full width, card bottom) ─────────────────
        int btnX = x + 8, btnY = y + CARD_H - 27, btnW = w - 16, btnH = 20;

        // Button hover — only when mouse is over the button
        boolean btnHov = hovered && my >= btnY && my < btnY + btnH;
        float bha = lerp(hov.getOrDefault(id + "_b", 0f), btnHov ? 1f : 0f, clamp01(0.40f * dt));
        hov.put(id + "_b", bha);

        int btnBg  = lerpColor(
            lerpColor(C_BTN_OFF, C_BTN_ON, ta),
            lerpColor(C_BTN_OFF, C_BTN_ON_H, ta), bha);
        int btnTxt = lerpColor(C_BTN_OFF_T, C_BTN_ON_T, ta);
        ctx.fill(btnX, btnY, btnX + btnW, btnY + btnH, recolor(btnBg, fadeA));
        roundCorners(ctx, btnX, btnY, btnW, btnH, recolor(cardBg, fadeA));

        String btnLabel = on ? "Enabled" : "Disabled";
        int btw = font.width(btnLabel);
        ctx.text(font, btnLabel, btnX + (btnW - btw) / 2, btnY + (btnH - 9) / 2,
            recolor(btnTxt, fadeA), false);
    }


    // ── Corner-clip helpers ─────────────────────────────────────────────────────

    // 4-px radius step-approximation
    private static void roundCorners(GuiGraphicsExtractor ctx, int x, int y, int w, int h, int cc) {
        // top-left
        ctx.fill(x,     y,     x + 4, y + 1, cc);
        ctx.fill(x,     y + 1, x + 2, y + 2, cc);
        ctx.fill(x,     y + 2, x + 1, y + 3, cc);
        // top-right
        ctx.fill(x+w-4, y,     x + w, y + 1, cc);
        ctx.fill(x+w-2, y + 1, x + w, y + 2, cc);
        ctx.fill(x+w-1, y + 2, x + w, y + 3, cc);
        // bottom-left
        ctx.fill(x,     y+h-1, x + 4, y + h, cc);
        ctx.fill(x,     y+h-2, x + 2, y+h-1, cc);
        ctx.fill(x,     y+h-3, x + 1, y+h-2, cc);
        // bottom-right
        ctx.fill(x+w-4, y+h-1, x + w, y + h, cc);
        ctx.fill(x+w-2, y+h-2, x + w, y+h-1, cc);
        ctx.fill(x+w-1, y+h-3, x + w, y+h-2, cc);
    }

    // ── Input ──────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean handled) {
        if (event.button() != 0) return super.mouseClicked(event, handled);
        int mx = (int) event.x(), my = (int) event.y();
        int px = (width - W) / 2, py = (height - H) / 2;

        // Close button
        int clX = px + W - 34, clY = py + (TOP_H - 20) / 2;
        if (mx >= clX && mx < clX + 20 && my >= clY && my < clY + 20) {
            this.minecraft.setScreenAndShow(null); return true;
        }

        // Edit Layout button
        String layLabel = "Edit Layout";
        int layW = font.width(layLabel) + 16;
        int clX2 = px + W - 34, clY2 = py + (TOP_H - 20) / 2;
        int layX = clX2 - layW - 8;
        if (mx >= layX && mx < layX + layW && my >= clY2 && my < clY2 + 20) {
            this.minecraft.setScreenAndShow(new WraithHudEditorScreen());
            return true;
        }

        // Tab pills
        int tpY = py + TOP_H + 9, tpH = TAB_H - 18, tpX = px + 16;
        for (Category c : Category.values()) {
            int tw = font.width(c.label) + 22;
            if (mx >= tpX && mx < tpX + tw && my >= tpY && my < tpY + tpH) {
                if (c != tab && pendingTab == null) { pendingTab = c; tabFade = 1f; }
                return true;
            }
            tpX += tw + 6;
        }

        // Feature cards
        List<Feature> list = WraithModClient.FEATURES.getAll().stream()
            .filter(f -> f.getCategory() == tab).collect(Collectors.toList());
        int contentTop = py + TOP_H + TAB_H + 2;
        int scroll = (int) scrollSmooth;
        for (int i = 0; i < list.size(); i++) {
            int col = i % COLS, row = i / COLS;
            int cx = px + MARGIN + col * (cardW + CARD_GX);
            int cy = contentTop - scroll + row * (CARD_H + CARD_GY);
            if (mx >= cx && mx < cx + cardW && my >= cy && my < cy + CARD_H) {
                list.get(i).setEnabled(!list.get(i).isEnabled());
                WraithModClient.FEATURES.save();
                return true;
            }
        }
        return super.mouseClicked(event, handled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double h, double v) {
        scrollTarget -= (int)(v * 22);
        scrollTarget = iclamp(scrollTarget, 0, maxScroll);
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_RIGHT_SHIFT || event.key() == GLFW.GLFW_KEY_ESCAPE) {
            this.minecraft.setScreenAndShow(null); return true;
        }
        return super.keyPressed(event);
    }

    // ── Math / color utilities ─────────────────────────────────────────────────

    private static float lerp(float a, float b, float t) { return a + (b - a) * t; }
    private static float clamp01(float v)                { return Math.max(0f, Math.min(1f, v)); }
    private static float easeOut(float t)                { return 1f - (1f - t) * (1f - t); }
    private static int   iclamp(int v, int lo, int hi)   { return Math.max(lo, Math.min(hi, v)); }

    private static int lerpColor(int a, int b, float t) {
        int aa = (a >> 24) & 0xFF, ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int ba = (b >> 24) & 0xFF, br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        return ((aa + (int)((ba - aa) * t)) << 24)
             | ((ar + (int)((br - ar) * t)) << 16)
             | ((ag + (int)((bg - ag) * t)) <<  8)
             |  (ab + (int)((bb - ab) * t));
    }

    private static int recolor(int c, int a) {
        return (c & 0x00FFFFFF) | (iclamp(a, 0, 255) << 24);
    }

    private static int alpha(int rgb, int a) {
        return (iclamp(a, 0, 255) << 24) | (rgb & 0x00FFFFFF);
    }

    // ── Feature metadata ───────────────────────────────────────────────────────

    private static String icon(String id) {
        return switch (id) {
            case "fps"              -> "FPS";
            case "cps"              -> "CPS";
            case "coords"           -> "XYZ";
            case "direction"        -> "DIR";
            case "armor"            -> "ARM";
            case "keystrokes"       -> "KEY";
            case "potionhud"        -> "POT";
            case "ping"             -> "PIN";
            case "speed"            -> "SPD";
            case "clock"            -> "CLK";
            case "itemdurability"   -> "DUR";
            case "saturationhud"    -> "SAT";
            case "entityhealthbars" -> "ENT";
            case "reachdisplay"     -> "RCH";
            case "togglesprint"     -> "SPR";
            case "fullbright"       -> "FBR";
            case "zoom"             -> "ZOM";
            case "antiblind"        -> "ABL";
            case "antinausea"       -> "ANS";
            case "nobob"            -> "BOB";
            case "nohurtcam"        -> "HRT";
            case "autogg"           -> " GG";
            default                 -> "???";
        };
    }

    private static String desc(String id) {
        return switch (id) {
            case "fps"              -> "Frames per second";
            case "cps"              -> "Clicks per second";
            case "coords"           -> "XYZ on screen";
            case "direction"        -> "Compass direction";
            case "armor"            -> "Armor durability";
            case "keystrokes"       -> "Key press display";
            case "potionhud"        -> "Active potions";
            case "ping"             -> "Server latency";
            case "speed"            -> "Movement speed";
            case "clock"            -> "Real-world clock";
            case "itemdurability"   -> "Item durability";
            case "saturationhud"    -> "Food saturation";
            case "entityhealthbars" -> "Entity health";
            case "reachdisplay"     -> "Target distance";
            case "togglesprint"     -> "Auto sprint";
            case "fullbright"       -> "No darkness";
            case "zoom"             -> "Hold C to zoom";
            case "antiblind"        -> "Remove blindness";
            case "antinausea"       -> "Remove nausea";
            case "nobob"            -> "No view bobbing";
            case "nohurtcam"        -> "No hurt shake";
            case "autogg"           -> "Auto GG on death";
            default                 -> "";
        };
    }
}
