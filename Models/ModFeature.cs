namespace WraithClient.Models;

public class ModFeature
{
    public string Id { get; set; } = string.Empty;
    public string Name { get; set; } = string.Empty;
    public string Description { get; set; } = string.Empty;
    public ModCategory Category { get; set; }
    public bool EnabledByDefault { get; set; } = true;
    public bool IsEnabled { get; set; } = true;
    public string Icon { get; set; } = "⚡";
}

public enum ModCategory
{
    Performance,
    HUD,
    Visual,
    Utility,
    PvP
}

public static class DefaultMods
{
    public static List<ModFeature> All => new()
    {
        // Performance
        new() { Id = "fps_boost",       Name = "FPS Boost",          Category = ModCategory.Performance, Icon = "🚀", Description = "Optimizes rendering for higher framerates.", EnabledByDefault = true },
        new() { Id = "entity_culling",  Name = "Entity Culling",     Category = ModCategory.Performance, Icon = "👁", Description = "Skips rendering entities not visible to the camera.", EnabledByDefault = true },
        new() { Id = "chunk_anim",      Name = "Chunk Animator",     Category = ModCategory.Performance, Icon = "🌐", Description = "Smoothly animates chunk pop-in.", EnabledByDefault = false },
        new() { Id = "fast_render",     Name = "Fast Render",        Category = ModCategory.Performance, Icon = "⚡", Description = "Reduces OpenGL overhead during frame rendering.", EnabledByDefault = true },

        // HUD
        new() { Id = "fps_counter",     Name = "FPS Counter",        Category = ModCategory.HUD, Icon = "📊", Description = "Displays your current framerate on screen.", EnabledByDefault = true },
        new() { Id = "cps_counter",     Name = "CPS Counter",        Category = ModCategory.HUD, Icon = "🖱", Description = "Shows left and right clicks per second.", EnabledByDefault = true },
        new() { Id = "keystrokes",      Name = "Keystrokes",         Category = ModCategory.HUD, Icon = "⌨", Description = "Displays WASD, shift, and space key presses.", EnabledByDefault = true },
        new() { Id = "armor_hud",       Name = "Armor HUD",          Category = ModCategory.HUD, Icon = "🛡", Description = "Shows your equipped armor and durability.", EnabledByDefault = true },
        new() { Id = "potion_hud",      Name = "Potion HUD",         Category = ModCategory.HUD, Icon = "🧪", Description = "Displays active potion effects and duration.", EnabledByDefault = true },
        new() { Id = "coords",          Name = "Coordinates",        Category = ModCategory.HUD, Icon = "📍", Description = "Shows X/Y/Z coordinates and direction.", EnabledByDefault = true },
        new() { Id = "ping_display",    Name = "Ping Display",       Category = ModCategory.HUD, Icon = "📶", Description = "Shows your current server ping.", EnabledByDefault = true },
        new() { Id = "clock",           Name = "Clock",              Category = ModCategory.HUD, Icon = "🕐", Description = "Displays current real-world time.", EnabledByDefault = false },
        new() { Id = "speed_display",   Name = "Speed Display",      Category = ModCategory.HUD, Icon = "💨", Description = "Shows your current movement speed.", EnabledByDefault = false },
        new() { Id = "server_address",  Name = "Server Address",     Category = ModCategory.HUD, Icon = "🌐", Description = "Shows the current server IP on screen.", EnabledByDefault = false },

        // Visual
        new() { Id = "custom_crosshair",Name = "Custom Crosshair",   Category = ModCategory.Visual, Icon = "✚", Description = "Replace the default crosshair with a custom one.", EnabledByDefault = true },
        new() { Id = "fullbright",      Name = "Full Bright",        Category = ModCategory.Visual, Icon = "☀", Description = "Maximum brightness/gamma for dark areas.", EnabledByDefault = false },
        new() { Id = "hit_color",       Name = "Hit Color",          Category = ModCategory.Visual, Icon = "🎨", Description = "Customizes the color flash when hitting entities.", EnabledByDefault = true },
        new() { Id = "item_physics",    Name = "Item Physics",       Category = ModCategory.Visual, Icon = "🎲", Description = "Adds realistic physics to dropped items.", EnabledByDefault = false },
        new() { Id = "time_changer",    Name = "Time Changer",       Category = ModCategory.Visual, Icon = "🌅", Description = "Locally change the sky time of day.", EnabledByDefault = false },
        new() { Id = "motion_blur",     Name = "Motion Blur",        Category = ModCategory.Visual, Icon = "🔵", Description = "Adds a subtle motion blur effect.", EnabledByDefault = false },
        new() { Id = "pop_anim",        Name = "Pop Animation",      Category = ModCategory.Visual, Icon = "✨", Description = "Items pop into your hotbar with an animation.", EnabledByDefault = true },

        // Utility
        new() { Id = "auto_sprint",     Name = "Auto Sprint",        Category = ModCategory.Utility, Icon = "🏃", Description = "Automatically holds sprint without pressing the key.", EnabledByDefault = true },
        new() { Id = "toggle_sneak",    Name = "Toggle Sneak",       Category = ModCategory.Utility, Icon = "🦆", Description = "Toggle sneak on/off instead of holding.", EnabledByDefault = false },
        new() { Id = "zoom",            Name = "Zoom",               Category = ModCategory.Utility, Icon = "🔍", Description = "Zoom in with a configurable key (like OptiFine zoom).", EnabledByDefault = true },
        new() { Id = "perspective",     Name = "Freelook",           Category = ModCategory.Utility, Icon = "👀", Description = "Look around freely without moving the camera direction.", EnabledByDefault = true },
        new() { Id = "bossbar_toggle",  Name = "Bossbar Toggle",     Category = ModCategory.Utility, Icon = "🔄", Description = "Hide or show the bossbar.", EnabledByDefault = true },
        new() { Id = "chat_filter",     Name = "Chat Filter",        Category = ModCategory.Utility, Icon = "💬", Description = "Filter unwanted messages from chat.", EnabledByDefault = false },

        // PvP
        new() { Id = "reach_display",   Name = "Reach Display",      Category = ModCategory.PvP, Icon = "📏", Description = "Shows the block/entity reach distance.", EnabledByDefault = false },
        new() { Id = "hitboxes",        Name = "Hitboxes",           Category = ModCategory.PvP, Icon = "📦", Description = "Renders entity hitboxes as colored outlines.", EnabledByDefault = false },
        new() { Id = "block_overlay",   Name = "Block Overlay",      Category = ModCategory.PvP, Icon = "🔲", Description = "Custom color for the selected block outline.", EnabledByDefault = true },
        new() { Id = "ping_spoof",      Name = "Combo Counter",      Category = ModCategory.PvP, Icon = "🔥", Description = "Tracks and displays your current hit combo.", EnabledByDefault = true },
    };
}
