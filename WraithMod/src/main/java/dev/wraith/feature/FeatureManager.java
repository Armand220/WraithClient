package dev.wraith.feature;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.wraith.feature.impl.*;
import dev.wraith.feature.impl.Clock;
import dev.wraith.feature.impl.ItemDurability;
import dev.wraith.feature.impl.SaturationHUD;
import dev.wraith.feature.impl.EntityHealthBars;
import dev.wraith.feature.impl.ReachDisplay;
import dev.wraith.feature.impl.NoBob;
import dev.wraith.feature.impl.NoHurtCam;
import dev.wraith.feature.impl.AutoGG;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FeatureManager {

    private static final Gson GSON = new Gson();
    private final List<Feature> features = new ArrayList<>();
    private final Path configPath;
    private final Path layoutPath;

    public FeatureManager() {
        configPath = FabricLoader.getInstance().getConfigDir().resolve("wraith.json");
        layoutPath = FabricLoader.getInstance().getConfigDir().resolve("wraith_layout.json");
        register(
            // HUD
            new FpsCounter(),
            new CpsCounter(),
            new Coordinates(),
            new DirectionHud(),
            new ArmorStatus(),
            new Keystrokes(),
            new PotionHUD(),
            new PingDisplay(),
            new SpeedDisplay(),
            new Clock(),
            new ItemDurability(),
            new SaturationHUD(),
            new EntityHealthBars(),
            new ReachDisplay(),
            // Movement
            new ToggleSprint(),
            // Visual
            new Fullbright(),
            new Zoom(),
            new AntiBlind(),
            new AntiNausea(),
            new NoBob(),
            new NoHurtCam(),
            // Utility
            new AutoGG()
        );
        load();
        loadLayout();
    }

    private void register(Feature... fs) {
        for (Feature f : fs) features.add(f);
    }

    public List<Feature> getAll() { return features; }

    public Feature get(String id) {
        return features.stream().filter(f -> f.getId().equals(id)).findFirst().orElse(null);
    }

    public void save() {
        Map<String, Boolean> map = new LinkedHashMap<>();
        for (Feature f : features) map.put(f.getId(), f.isEnabled());
        try { Files.writeString(configPath, GSON.toJson(map)); }
        catch (IOException ignored) {}
    }

    public void saveLayout() {
        Map<String, Map<String, Integer>> map = new LinkedHashMap<>();
        for (Feature f : features) {
            if (!f.hasHudPosition()) continue;
            Map<String, Integer> pos = new LinkedHashMap<>();
            pos.put("x", f.getHudX());
            pos.put("y", f.getHudY());
            map.put(f.getId(), pos);
        }
        try { Files.writeString(layoutPath, GSON.toJson(map)); }
        catch (IOException ignored) {}
    }

    private void load() {
        if (!Files.exists(configPath)) return;
        try {
            String json = Files.readString(configPath);
            Type type = new TypeToken<Map<String, Boolean>>(){}.getType();
            Map<String, Boolean> map = GSON.fromJson(json, type);
            if (map == null) return;
            for (Feature f : features) {
                Boolean val = map.get(f.getId());
                if (val != null) f.setEnabledSilent(val);
            }
        } catch (IOException ignored) {}
    }

    private void loadLayout() {
        if (!Files.exists(layoutPath)) return;
        try {
            String json = Files.readString(layoutPath);
            com.google.gson.reflect.TypeToken<Map<String, Map<String, Integer>>> tt =
                new com.google.gson.reflect.TypeToken<>(){};
            Map<String, Map<String, Integer>> map = GSON.fromJson(json, tt.getType());
            if (map == null) return;
            for (Feature f : features) {
                Map<String, Integer> pos = map.get(f.getId());
                if (pos != null) {
                    Integer x = pos.get("x"), y = pos.get("y");
                    if (x != null && y != null) f.setHudPos(x, y);
                }
            }
        } catch (IOException ignored) {}
    }

    // Call once the Minecraft client is fully initialized to apply loaded state.
    public void applyAll() {
        MinecraftClient client = MinecraftClient.getInstance();
        for (Feature f : features) {
            if (f.isEnabled()) f.onEnable(client);
        }
    }
}
