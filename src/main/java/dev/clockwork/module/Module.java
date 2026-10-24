package dev.clockwork.module;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.clockwork.setting.Settings.Setting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;

public abstract class Module {
    protected static final Minecraft mc = Minecraft.getInstance();
    public final String name, description;
    public final Category category;
    public final List<Setting<?>> settings = new ArrayList<>();
    public boolean enabled;
    public boolean expanded; // GUI state

    protected Module(String name, String description, Category category) {
        this.name = name; this.description = description; this.category = category;
    }
    protected <S extends Setting<?>> S add(S s) { settings.add(s); return s; }

    public void toggle() { setEnabled(!enabled); }
    public void setEnabled(boolean e) {
        if (e == enabled) return;
        enabled = e;
        if (e) onEnable(); else onDisable();
    }
    public void onEnable() {}
    public void onDisable() {}
    public void onTick() {}
    /** Called when a setting finished changing (mouse released / toggled). */
    public void onSettingChanged() {}
    /** World-space rendering; the caller has already made the pose stack camera-relative. */
    public void onWorldRender(PoseStack ps, MultiBufferSource buffers, Vec3 cam) {}
}
