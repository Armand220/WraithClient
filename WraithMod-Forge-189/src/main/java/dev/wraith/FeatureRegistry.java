package dev.wraith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FeatureRegistry {
    public static final Feature FPS           = new Feature("FPS",           true);
    public static final Feature COORDS        = new Feature("Coords",        false);
    public static final Feature TOGGLE_SPRINT = new Feature("Toggle Sprint", false);
    public static final Feature ZOOM          = new Feature("Zoom",          true);

    private static final List<Feature> ALL = new ArrayList<>();

    public static void init() {
        Collections.addAll(ALL, FPS, COORDS, TOGGLE_SPRINT, ZOOM);
    }

    public static List<Feature> getAll() { return Collections.unmodifiableList(ALL); }
}
