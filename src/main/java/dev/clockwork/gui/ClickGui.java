package dev.clockwork.gui;

import dev.clockwork.ClockworkClient;
import dev.clockwork.module.Category;
import dev.clockwork.module.Module;
import dev.clockwork.module.ModuleManager;
import dev.clockwork.setting.Settings.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/** Right-Shift GUI. Left-click a module = toggle, right-click = expand its settings. Scroll to pan. */
public class ClickGui extends Screen {
    // brass-and-ink theme
    private static final int BG = 0xE61A1612, HEADER = 0xF0241D14, ROW = 0xCC2A2218, ROW_HOVER = 0xCC3A2F20,
            ACCENT = 0xFFE8B64C, ACCENT_DIM = 0xFF8A6A2A, TEXT = 0xFFF2E6C8, SUBTEXT = 0xFFA89874, TRACK = 0xFF120F0B;
    private static final int PANEL_W = 150, ROW_H = 17, SET_H = 15, GAP = 10, HEAD_H = 26;

    private record Hit(int x, int y, int w, int h, Consumer<MouseButtonEvent> click, Consumer<Double> drag) {
        boolean in(double mx, double my) { return mx >= x && mx < x + w && my >= y && my < y + h; }
    }
    private final List<Hit> hits = new ArrayList<>();
    private Hit dragging;
    private Module dirty;
    private int scroll;
    private long opened = System.currentTimeMillis();

    public ClickGui() { super(Component.literal("Clockwork")); }
    @Override public boolean isPauseScreen() { return false; }

    @Override public void render(GuiGraphics g, int mx, int my, float dt) {
        hits.clear();
        float fade = Math.min(1f, (System.currentTimeMillis() - opened) / 180f);
        g.fill(0, 0, width, height, ((int) (110 * fade) << 24));
        Category[] cats = Category.values();
        int totalW = cats.length * PANEL_W + (cats.length - 1) * GAP;
        int x = (width - totalW) / 2;
        for (Category c : cats) { panel(g, c, x, 28 + scroll + (int) ((1 - fade) * -12), mx, my); x += PANEL_W + GAP; }
        // logo + title
        g.blit(RenderPipelines.GUI_TEXTURED, ClockworkClient.LOGO, 8, 6, 0, 0, 20, 20, 256, 256);
        g.drawString(font, "CLOCKWORK", 32, 11, ACCENT, true);
        g.drawString(font, "RShift to close", 32, 21, SUBTEXT, false);
    }

    private void panel(GuiGraphics g, Category cat, int x, int y, int mx, int my) {
        List<Module> mods = ModuleManager.in(cat);
        int h = HEAD_H;
        for (Module m : mods) { h += ROW_H; if (m.expanded) h += settingsHeight(m); }
        rounded(g, x, y, PANEL_W, h + 4, BG);
        rounded(g, x, y, PANEL_W, HEAD_H, HEADER);
        g.drawCenteredString(font, cat.label.toUpperCase(), x + PANEL_W / 2, y + 9, ACCENT);
        g.fill(x + 10, y + HEAD_H - 1, x + PANEL_W - 10, y + HEAD_H, ACCENT_DIM);

        int cy = y + HEAD_H;
        for (Module m : mods) {
            boolean hover = mx >= x && mx < x + PANEL_W && my >= cy && my < cy + ROW_H;
            g.fill(x + 3, cy, x + PANEL_W - 3, cy + ROW_H, hover ? ROW_HOVER : ROW);
            if (m.enabled) g.fill(x + 3, cy, x + 5, cy + ROW_H, ACCENT);
            g.drawString(font, m.name, x + 11, cy + 5, m.enabled ? ACCENT : TEXT, false);
            pill(g, x + PANEL_W - 26, cy + 4, m.enabled);
            if (!m.settings.isEmpty()) g.drawString(font, m.expanded ? "v" : ">", x + PANEL_W - 38, cy + 5, SUBTEXT, false);
            final Module mod = m;
            hits.add(new Hit(x, cy, PANEL_W, ROW_H, e -> {
                if (e.button() == 0) { mod.toggle(); }
                else if (e.button() == 1 && !mod.settings.isEmpty()) mod.expanded = !mod.expanded;
            }, null));
            if (hover) g.drawString(font, m.description, Math.min(mx + 10, width - font.width(m.description) - 4), my - 12, SUBTEXT, true);
            cy += ROW_H;
            if (m.expanded) {
                for (var s : m.settings) cy = setting(g, m, s, x, cy);
                g.fill(x + 3, cy, x + PANEL_W - 3, cy + 1, ACCENT_DIM);
            }
        }
    }

    private int settingsHeight(Module m) {
        int h = 1;
        for (var s : m.settings) h += s instanceof Color ? SET_H * 3 : SET_H;
        return h;
    }

    private int setting(GuiGraphics g, Module m, Object s, int x, int y) {
        int l = x + 8, w = PANEL_W - 16;
        if (s instanceof Bool b) {
            g.drawString(font, b.name, l, y + 4, TEXT, false);
            pill(g, x + PANEL_W - 26, y + 3, b.on());
            hits.add(new Hit(x, y, PANEL_W, SET_H, e -> { b.value = !b.value; m.onSettingChanged(); }, null));
        } else if (s instanceof Mode md) {
            g.drawString(font, md.name, l, y + 4, SUBTEXT, false);
            String v = "< " + md.value + " >";
            g.drawString(font, v, x + PANEL_W - 8 - font.width(v), y + 4, ACCENT, false);
            hits.add(new Hit(x, y, PANEL_W, SET_H, e -> { md.cycle(e.button() == 1 ? -1 : 1); m.onSettingChanged(); }, null));
        } else if (s instanceof Num n) {
            slider(g, m, n.name, String.format(n.step >= 1 ? "%.0f" : "%.2f", n.get()),
                    (n.get() - n.min) / (n.max - n.min), l, y, w, f -> n.set(n.min + f * (n.max - n.min)));
        } else if (s instanceof Color c) {
            g.drawString(font, c.name + (c.rainbow ? " (rainbow)" : ""), l, y + 3, TEXT, false);
            int by = y + 12;
            for (int i = 0; i < w; i++) {   // hue strip
                int rgb = java.awt.Color.HSBtoRGB(i / (float) w, c.sat, c.bri) | 0xFF000000;
                g.fill(l + i, by, l + i + 1, by + 4, rgb);
            }
            int hx = l + (int) (c.value * (w - 1));
            g.fill(hx - 1, by - 2, hx + 2, by + 6, 0xFFFFFFFF);
            hits.add(new Hit(l, by - 3, w, 10, e -> { if (e.button() == 1) { c.rainbow = !c.rainbow; m.onSettingChanged(); } else c.value = clamp((float) ((e.x() - l) / w)); },
                    mx -> c.value = clamp((float) ((mx - l) / w))));
            slider(g, m, "Opacity", (int) (c.alpha * 100) + "%", c.alpha, l, y + SET_H, w, f -> c.alpha = (float) f);
            slider(g, m, "Brightness", (int) (c.bri * 100) + "%", c.bri, l, y + SET_H * 2, w, f -> c.bri = (float) Math.max(0.15, f));
            return y + SET_H * 3;
        }
        return y + SET_H;
    }

    private void slider(GuiGraphics g, Module m, String name, String val, double frac, int l, int y, int w, Consumer<Double> set) {
        frac = Math.max(0, Math.min(1, frac));
        g.drawString(font, name, l, y + 2, SUBTEXT, false);
        g.drawString(font, val, l + w - font.width(val), y + 2, TEXT, false);
        g.fill(l, y + 11, l + w, y + 13, TRACK);
        g.fill(l, y + 11, l + (int) (w * frac), y + 13, ACCENT);
        int kx = l + (int) (w * frac);
        g.fill(kx - 1, y + 9, kx + 2, y + 15, TEXT);
        Consumer<Double> apply = mx -> { set.accept(Math.max(0, Math.min(1, (mx - l) / w))); };
        hits.add(new Hit(l, y + 7, w, 9, e -> apply.accept(e.x()), apply));
    }

    private void pill(GuiGraphics g, int x, int y, boolean on) {
        g.fill(x, y + 1, x + 20, y + 9, on ? ACCENT_DIM : TRACK);
        g.fill(x + 1, y, x + 19, y + 10, on ? ACCENT_DIM : TRACK);
        int kx = on ? x + 11 : x + 2;
        g.fill(kx, y + 1, kx + 7, y + 8, on ? ACCENT : SUBTEXT);
    }
    private void rounded(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x + 2, y, x + w - 2, y + h, c);
        g.fill(x, y + 2, x + w, y + h - 2, c);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, c);
    }
    private static float clamp(float f) { return Math.max(0f, Math.min(1f, f)); }

    @Override public boolean mouseClicked(MouseButtonEvent e, boolean dbl) {
        for (int i = hits.size() - 1; i >= 0; i--) {
            Hit h = hits.get(i);
            if (h.in(e.x(), e.y())) {
                h.click.accept(e);
                if (h.drag != null && e.button() == 0) dragging = h;
                dirty = null;
                for (Module m : ModuleManager.MODULES) if (m.expanded && m.enabled) dirty = m;
                return true;
            }
        }
        return super.mouseClicked(e, dbl);
    }
    @Override public boolean mouseDragged(MouseButtonEvent e, double dx, double dy) {
        if (dragging != null) { dragging.drag.accept(e.x()); return true; }
        return super.mouseDragged(e, dx, dy);
    }
    @Override public boolean mouseReleased(MouseButtonEvent e) {
        if (dragging != null) { dragging = null; for (Module m : ModuleManager.MODULES) if (m.enabled) m.onSettingChanged(); }
        return super.mouseReleased(e);
    }
    @Override public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        scroll = Math.min(0, scroll + (int) (sy * 16));
        return true;
    }
    @Override public boolean keyPressed(KeyEvent e) {
        if (e.key() == GLFW.GLFW_KEY_RIGHT_SHIFT) { onClose(); return true; }
        return super.keyPressed(e);
    }
    @Override public void removed() { ModuleManager.save(); }
}
