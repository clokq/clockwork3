package dev.clockwork.module.impl;

import dev.clockwork.module.Category;
import dev.clockwork.module.Module;
import dev.clockwork.setting.Settings.*;

/** Sky tint, read by ClientLevelMixin. (The clock sun/moon come from the bundled resource pack.) */
public class SkyModule extends Module {
    public static SkyModule INSTANCE;
    public final Mode mode = add(new Mode("Mode", "Custom", "Rainbow", "Dusk", "Midnight"));
    public final Color color = add(new Color("Sky colour", 0.58f, 1.0f));
    public final Num strength = add(new Num("Strength", 1.0, 0.0, 1.0, 0.01));
    public final Num rainbowSpeed = add(new Num("Rainbow speed", 1.0, 0.1, 5.0, 0.1));

    public SkyModule() { super("Sky", "Customise the sky colour.", Category.WORLD); INSTANCE = this; }

    @Override public void onSettingChanged() {}

    /** ARGB-less RGB int for the sky, blended with the vanilla colour. */
    public int apply(int vanillaRgb) {
        int target = switch (mode.value) {
            case "Rainbow" -> java.awt.Color.HSBtoRGB((System.currentTimeMillis() * rainbowSpeed.f() % 10000L) / 10000f, 0.7f, 1f) & 0xFFFFFF;
            case "Dusk" -> 0xE8844A;
            case "Midnight" -> 0x050818;
            default -> color.rgb();
        };
        float k = strength.f();
        int r = (int) (((vanillaRgb >> 16) & 255) * (1 - k) + ((target >> 16) & 255) * k);
        int g = (int) (((vanillaRgb >> 8) & 255) * (1 - k) + ((target >> 8) & 255) * k);
        int b = (int) ((vanillaRgb & 255) * (1 - k) + (target & 255) * k);
        return (r << 16) | (g << 8) | b;
    }
}
