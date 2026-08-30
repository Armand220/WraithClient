package dev.wraith;

import java.util.ArrayList;
import java.util.List;

public class FeatureRegistry {
    private static final List<Feature> FEATURES = new ArrayList<>();

    public static final Feature FPS          = new Feature("fps",          "FPS Counter",    true);
    public static final Feature COORDS       = new Feature("coords",       "Coordinates",    true);
    public static final Feature TOGGLE_SPRINT = new Feature("togglesprint", "Toggle Sprint",  true);
    public static final Feature FULLBRIGHT   = new Feature("fullbright",   "Fullbright",     false);
    public static final Feature ZOOM         = new Feature("zoom",         "Zoom",           true);

    public static void init() {
        FEATURES.add(FPS);
        FEATURES.add(COORDS);
        FEATURES.add(TOGGLE_SPRINT);
        FEATURES.add(FULLBRIGHT);
        FEATURES.add(ZOOM);
    }

    public static List<Feature> getAll() { return FEATURES; }
}
