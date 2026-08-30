package dev.wraith.feature;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.wraith.feature.impl.*;
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

    public FeatureManager() {
        configPath = FabricLoader.getInstance().getConfigDir().resolve("wraith.json");
        register(
            new FpsCounter(),
            new CpsCounter(),
            new Coordinates(),
            new DirectionHud(),
            new ArmorStatus(),
            new Keystrokes(),
            new PotionHUD(),
            new PingDisplay(),
            new SpeedDisplay(),
            new ToggleSprint(),
            new NoFall(),
            new Fullbright(),
            new Zoom(),
            new AntiBlind(),
            new AntiNausea()
        );
        load();
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

    public void applyAll() {
        MinecraftClient client = MinecraftClient.getInstance();
        for (Feature f : features) {
            if (f.isEnabled()) f.onEnable(client);
        }
    }
}
