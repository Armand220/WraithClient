package dev.wraith;

public class Feature {
    private final String name;
    private boolean enabled;

    public Feature(String name, boolean defaultEnabled) {
        this.name    = name;
        this.enabled = defaultEnabled;
    }

    public String getName()  { return name; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean v) { enabled = v; }
    public void toggle() { enabled = !enabled; }
}
