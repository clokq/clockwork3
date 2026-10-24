package dev.clockwork.setting;

import com.google.gson.JsonObject;
import java.util.List;

/** Every configurable value a module can expose. The GUI renders these generically. */
public final class Settings {
    private Settings() {}

    public abstract static class Setting<T> {
        public final String name;
        public T value;
        protected Setting(String name, T value) { this.name = name; this.value = value; }
        public abstract void save(JsonObject o);
        public abstract void load(JsonObject o);
    }

    public static class Bool extends Setting<Boolean> {
        public Bool(String n, boolean v) { super(n, v); }
        public boolean on() { return value; }
        public void save(JsonObject o) { o.addProperty(name, value); }
        public void load(JsonObject o) { if (o.has(name)) value = o.get(name).getAsBoolean(); }
    }

    public static class Num extends Setting<Double> {
        public final double min, max, step;
        public Num(String n, double v, double min, double max, double step) { super(n, v); this.min = min; this.max = max; this.step = step; }
        public double get() { return value; }
        public float f() { return value.floatValue(); }
        public int i() { return (int) Math.round(value); }
        public void set(double v) {
            v = Math.max(min, Math.min(max, v));
            value = step > 0 ? Math.round(v / step) * step : v;
        }
        public void save(JsonObject o) { o.addProperty(name, value); }
        public void load(JsonObject o) { if (o.has(name)) set(o.get(name).getAsDouble()); }
    }

    public static class Mode extends Setting<String> {
        public final List<String> modes;
        public Mode(String n, String... modes) { super(n, modes[0]); this.modes = List.of(modes); }
        public boolean is(String m) { return value.equals(m); }
        public void cycle(int dir) {
            int i = (modes.indexOf(value) + dir + modes.size()) % modes.size();
            value = modes.get(i);
        }
        public void save(JsonObject o) { o.addProperty(name, value); }
        public void load(JsonObject o) { if (o.has(name) && modes.contains(o.get(name).getAsString())) value = o.get(name).getAsString(); }
    }

    /** Hue + saturation + brightness + alpha (alpha is the "opacity" slider). Optional rainbow cycling. */
    public static class Color extends Setting<Float> {
        public float sat = 0.85f, bri = 1f, alpha = 0.9f;
        public boolean rainbow = false;
        public Color(String n, float hue, float alpha) { super(n, hue); this.alpha = alpha; }
        public int rgb() {
            float h = rainbow ? (System.currentTimeMillis() % 6000L) / 6000f : value;
            return java.awt.Color.HSBtoRGB(h, sat, bri) & 0xFFFFFF;
        }
        public int argb() { return rgb() | ((int) (alpha * 255) << 24); }
        public float r() { return (rgb() >> 16 & 255) / 255f; }
        public float g() { return (rgb() >> 8 & 255) / 255f; }
        public float b() { return (rgb() & 255) / 255f; }
        public void save(JsonObject o) {
            JsonObject c = new JsonObject();
            c.addProperty("h", value); c.addProperty("s", sat); c.addProperty("b", bri);
            c.addProperty("a", alpha); c.addProperty("rainbow", rainbow);
            o.add(name, c);
        }
        public void load(JsonObject o) {
            if (!o.has(name)) return;
            JsonObject c = o.getAsJsonObject(name);
            value = c.get("h").getAsFloat(); sat = c.get("s").getAsFloat(); bri = c.get("b").getAsFloat();
            alpha = c.get("a").getAsFloat(); rainbow = c.get("rainbow").getAsBoolean();
        }
    }
}
