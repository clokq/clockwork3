package dev.clockwork.module.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.clockwork.module.Category;
import dev.clockwork.module.Module;
import dev.clockwork.setting.Settings.*;
import dev.clockwork.util.RenderUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Shared scanning + drawing for block-entity ESP modules. */
public abstract class BlockEsp extends Module {
    protected final Mode mode = add(new Mode("Mode", "Outline", "Filled", "Both"));
    protected final Num range = add(new Num("Range", 96, 16, 256, 8));
    protected final Num fillOpacity = add(new Num("Fill opacity", 0.25, 0.02, 1.0, 0.01));
    protected final Num expand = add(new Num("Box expand", 0.0, 0.0, 0.2, 0.01));
    protected final Bool throughWalls = add(new Bool("Through walls", true));
    protected final Bool fadeWithDistance = add(new Bool("Fade with distance", false));

    private record Hit(BlockPos pos, Color color) {}
    private volatile List<Hit> hits = List.of();
    private int ticks;

    protected BlockEsp(String name, String desc) { super(name, desc, Category.RENDER); }

    /** Return the colour setting for this block entity, or null to ignore it. */
    protected abstract Color colorFor(BlockEntity be);

    @Override public void onTick() {
        if (mc.level == null || mc.player == null) return;
        if (ticks++ % 10 != 0) return;
        List<Hit> out = new ArrayList<>();
        int cr = (range.i() >> 4) + 1, pcx = mc.player.chunkPosition().x, pcz = mc.player.chunkPosition().z;
        double r2 = range.get() * range.get();
        for (int cx = pcx - cr; cx <= pcx + cr; cx++)
            for (int cz = pcz - cr; cz <= pcz + cr; cz++) {
                LevelChunk chunk = mc.level.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) continue;
                for (Map.Entry<BlockPos, BlockEntity> e : chunk.getBlockEntities().entrySet()) {
                    if (e.getKey().distToCenterSqr(mc.player.position()) > r2) continue;
                    Color c = colorFor(e.getValue());
                    if (c != null) out.add(new Hit(e.getKey().immutable(), c));
                }
            }
        hits = out;
    }
    @Override public void onDisable() { hits = List.of(); }

    @Override public void onWorldRender(PoseStack ps, MultiBufferSource buffers, Vec3 cam) {
        boolean outline = !mode.is("Filled"), fill = !mode.is("Outline");
        for (Hit h : hits) {
            double d = Math.sqrt(h.pos.distToCenterSqr(cam));
            float fade = fadeWithDistance.on() ? (float) Math.max(0.15, 1 - d / range.get()) : 1f;
            AABB box = new AABB(h.pos).inflate(expand.get());
            RenderUtil.box(ps, buffers, box, h.color.r(), h.color.g(), h.color.b(),
                    h.color.alpha * fade, fillOpacity.f() * fade, outline, fill, throughWalls.on());
        }
    }
}
