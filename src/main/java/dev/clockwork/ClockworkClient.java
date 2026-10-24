package dev.clockwork;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.clockwork.gui.ClickGui;
import dev.clockwork.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public class ClockworkClient implements ClientModInitializer {
    public static final String ID = "clockwork";
    public static final Identifier LOGO = Identifier.fromNamespaceAndPath(ID, "logo.png");
    private boolean wasDown;

    @Override public void onInitializeClient() {
        ModuleManager.init();

        // Clock sun/moon: bundled resource pack, enabled by default (toggle it in the Resource Packs screen).
        ResourceLoader.registerBuiltinPack(Identifier.fromNamespaceAndPath(ID, "clockwork_sky"),
                FabricLoader.getInstance().getModContainer(ID).orElseThrow(),
                Component.literal("Clockwork Sky"), PackActivationType.DEFAULT_ENABLED);

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            boolean down = InputConstants.isKeyDown(mc.getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT);
            if (down && !wasDown && mc.screen == null && mc.level != null) mc.setScreen(new ClickGui());
            wasDown = down;
            ModuleManager.tick();
        });

        WorldRenderEvents.AFTER_ENTITIES.register(ctx -> {
            Minecraft mc = Minecraft.getInstance();
            Vec3 cam = mc.gameRenderer.getMainCamera().position();
            PoseStack ps = ctx.matrices();
            ps.pushPose();
            ps.translate(-cam.x, -cam.y, -cam.z);
            ModuleManager.render(ps, ctx.consumers(), cam);
            dev.clockwork.util.RenderUtil.flush();
            ps.popPose();
        });
    }
}
