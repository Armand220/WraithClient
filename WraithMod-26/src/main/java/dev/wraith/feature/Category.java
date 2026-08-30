package dev.wraith.feature;

public enum Category {
    HUD("HUD"),
    MOVEMENT("Movement"),
    VISUAL("Visual"),
    COMBAT("Combat"),
    UTILITY("Utility");

    public final String label;

    Category(String label) {
        this.label = label;
    }
}
