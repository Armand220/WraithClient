package dev.wraith;

import dev.wraith.feature.Feature;
import dev.wraith.feature.FeatureManager;
import dev.wraith.gui.WraithMenuScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class WraithModClient implements ClientModInitializer {

    public static FeatureManager FEATURES;
    private static KeyBinding menuKey;
    private static boolean menuKeyWasDown = false;

    @Override
    public void onInitializeClient() {
        FEATURES = new FeatureManager();

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> FEATURES.applyAll());

        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.wraith.menu",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            "key.categories.misc"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            boolean menuKeyDown = menuKey.isPressed();
            if (menuKeyDown && !menuKeyWasDown) {
                if (client.currentScreen == null) {
                    client.setScreen(new WraithMenuScreen());
                } else if (client.currentScreen instanceof WraithMenuScreen) {
                    client.setScreen(null);
                }
            }
            menuKeyWasDown = menuKeyDown;

            if (client.player != null) {
                for (Feature f : FEATURES.getAll()) {
                    if (f.isEnabled()) f.tick(client);
                }
            }
        });

        // 1.16-1.19 HudRenderCallback takes (MatrixStack, float tickDelta)
        HudRenderCallback.EVENT.register((stack, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.currentScreen != null) return;

            int sw = client.getWindow().getScaledWidth();
            int sh = client.getWindow().getScaledHeight();

            for (Feature f : FEATURES.getAll()) {
                if (f.isEnabled()) f.renderHud(stack, client.textRenderer, sw, sh);
            }
        });
    }
}
