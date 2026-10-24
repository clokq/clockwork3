package dev.clockwork.util;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.OptionalDouble;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

/**
 * All version-sensitive world drawing lives here so an API shuffle only breaks one file.
 * "Through walls" uses our own pipelines with the depth test disabled, drawn into our own buffer
 * that ClockworkClient flushes once per frame via {@link #flush()}.
 */
public final class RenderUtil {
    private RenderUtil() {}

    private static final RenderPipeline LINES_XRAY = RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withLocation("pipeline/clockwork_lines_xray")
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .build();
    private static final RenderPipeline FILL_XRAY = RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation("pipeline/clockwork_fill_xray")
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .build();

    private static final RenderType LINES = RenderType.create("clockwork_lines", 1536, false, false, LINES_XRAY,
            RenderType.CompositeState.builder()
                    .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty()))
                    .createCompositeState(false));
    private static final RenderType FILL = RenderType.create("clockwork_fill", 1536, false, true, FILL_XRAY,
            RenderType.CompositeState.builder().createCompositeState(false));

    private static final MultiBufferSource.BufferSource BUF =
            MultiBufferSource.immediate(new ByteBufferBuilder(1 << 18));

    public static void box(PoseStack ps, MultiBufferSource vanilla, AABB b,
                           float r, float g, float bl, float outlineA, float fillA,
                           boolean outline, boolean fill, boolean throughWalls) {
        MultiBufferSource src = throughWalls ? BUF : vanilla;
        if (fill && fillA > 0f)
            filled(ps.last().pose(), src.getBuffer(throughWalls ? FILL : RenderType.debugFilledBox()), b, r, g, bl, fillA);
        if (outline && outlineA > 0f)
            ShapeRenderer.renderLineBox(ps.last(), src.getBuffer(throughWalls ? LINES : RenderType.lines()),
                    b.minX, b.minY, b.minZ, b.maxX, b.maxY, b.maxZ, r, g, bl, outlineA);
    }

    /** Call once per frame after every module has drawn. */
    public static void flush() { BUF.endBatch(FILL); BUF.endBatch(LINES); }

    private static void filled(Matrix4f m, VertexConsumer v, AABB b, float r, float g, float bl, float a) {
        float x1 = (float) b.minX, y1 = (float) b.minY, z1 = (float) b.minZ;
        float x2 = (float) b.maxX, y2 = (float) b.maxY, z2 = (float) b.maxZ;
        quad(m, v, x1,y1,z1, x2,y1,z1, x2,y1,z2, x1,y1,z2, r,g,bl,a);
        quad(m, v, x1,y2,z1, x1,y2,z2, x2,y2,z2, x2,y2,z1, r,g,bl,a);
        quad(m, v, x1,y1,z1, x1,y2,z1, x2,y2,z1, x2,y1,z1, r,g,bl,a);
        quad(m, v, x1,y1,z2, x2,y1,z2, x2,y2,z2, x1,y2,z2, r,g,bl,a);
        quad(m, v, x1,y1,z1, x1,y1,z2, x1,y2,z2, x1,y2,z1, r,g,bl,a);
        quad(m, v, x2,y1,z1, x2,y2,z1, x2,y2,z2, x2,y1,z2, r,g,bl,a);
    }
    private static void quad(Matrix4f m, VertexConsumer v,
                             float ax, float ay, float az, float bx, float by, float bz,
                             float cx, float cy, float cz, float dx, float dy, float dz,
                             float r, float g, float b, float a) {
        v.addVertex(m, ax, ay, az).setColor(r, g, b, a);
        v.addVertex(m, bx, by, bz).setColor(r, g, b, a);
        v.addVertex(m, cx, cy, cz).setColor(r, g, b, a);
        v.addVertex(m, dx, dy, dz).setColor(r, g, b, a);
    }
}
