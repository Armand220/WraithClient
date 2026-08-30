package dev.wraith;

import dev.wraith.feature.Feature;
import dev.wraith.feature.FeatureManager;
import dev.wraith.gui.WraithMenuScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

public class WraithModClient implements ClientModInitializer {

    public static FeatureManager FEATURES;
    public static boolean isScreenOpen = false;

    private static KeyMapping menuKey;
    private static boolean menuKeyWasDown = false;

    @Override
    public void onInitializeClient() {
        FEATURES = new FeatureManager();

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> FEATURES.applyAll());

        menuKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.wraith.menu",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            KeyMapping.Category.MISC
        ));

        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            isScreenOpen = true;
            ScreenEvents.remove(screen).register(s -> isScreenOpen = false);
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            boolean menuKeyDown = menuKey.isDown();
            if (menuKeyDown && !menuKeyWasDown) {
                if (!isScreenOpen) {
                    client.setScreenAndShow(new WraithMenuScreen());
                } else if (client.canInterruptScreen()) {
                    client.setScreenAndShow(null);
                }
            }
            menuKeyWasDown = menuKeyDown;

            if (client.player != null) {
                for (Feature f : FEATURES.getAll()) {
                    if (f.isEnabled()) f.tick(client);
                }
            }
        });

        HudElementRegistry.addLast(
            Identifier.fromNamespaceAndPath("wraith", "hud"),
            (GuiGraphicsExtractor ctx, DeltaTracker delta) -> {
                Minecraft client = Minecraft.getInstance();
                if (client.player == null || isScreenOpen) return;

                int sw = client.getWindow().getGuiScaledWidth();
                int sh = client.getWindow().getGuiScaledHeight();

                for (Feature f : FEATURES.getAll()) {
                    if (f.isEnabled()) f.renderHud(ctx, client.font, sw, sh);
                }
            }
        );
    }
}
