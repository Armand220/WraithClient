package dev.wraith;

public class Feature {
    private final String id;
    private final String name;
    private boolean enabled;

    public Feature(String id, String name, boolean defaultEnabled) {
        this.id      = id;
        this.name    = name;
        this.enabled = defaultEnabled;
    }

    public String  getId()      { return id; }
    public String  getName()    { return name; }
    public boolean isEnabled()  { return enabled; }
    public void    toggle()     { enabled = !enabled; onToggle(); }
    public void    setEnabled(boolean v) { enabled = v; }

    protected void onToggle() {}
}
