package dev.clockwork.module;

import com.google.gson.*;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.clockwork.module.impl.*;
import dev.clockwork.setting.Settings.Setting;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.*;
import java.util.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;

public final class ModuleManager {
    public static final List<Module> MODULES = new ArrayList<>();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("clockwork.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void init() {
        MODULES.add(new StorageFinder());
        MODULES.add(new SpawnerFinder());
        MODULES.add(new Xray());
        MODULES.add(new Freecam());
        MODULES.add(new AntiVanish());
        MODULES.add(new SkyModule());
        load();
    }
    @SuppressWarnings("unchecked")
    public static <T extends Module> T get(Class<T> c) {
        for (Module m : MODULES) if (c.isInstance(m)) return (T) m;
        throw new IllegalStateException(c.getName());
    }
    public static List<Module> in(Category c) { return MODULES.stream().filter(m -> m.category == c).toList(); }

    public static void tick() { for (Module m : MODULES) if (m.enabled) m.onTick(); }
    public static void render(PoseStack ps, MultiBufferSource b, Vec3 cam) {
        for (Module m : MODULES) if (m.enabled) m.onWorldRender(ps, b, cam);
    }

    public static void save() {
        JsonObject root = new JsonObject();
        for (Module m : MODULES) {
            JsonObject o = new JsonObject();
            o.addProperty("enabled", m.enabled);
            for (Setting<?> s : m.settings) s.save(o);
            root.add(m.name, o);
        }
        try (Writer w = Files.newBufferedWriter(FILE)) { GSON.toJson(root, w); } catch (Exception ignored) {}
    }
    private static void load() {
        if (!Files.exists(FILE)) return;
        try (Reader r = Files.newBufferedReader(FILE)) {
            JsonObject root = JsonParser.parseReader(r).getAsJsonObject();
            for (Module m : MODULES) {
                if (!root.has(m.name)) continue;
                JsonObject o = root.getAsJsonObject(m.name);
                for (Setting<?> s : m.settings) s.load(o);
                if (o.has("enabled") && o.get("enabled").getAsBoolean()) m.setEnabled(true);
            }
        } catch (Exception ignored) {}
    }
}
