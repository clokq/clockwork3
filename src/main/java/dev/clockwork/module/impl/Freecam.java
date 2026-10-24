package dev.clockwork.module.impl;

import dev.clockwork.module.Category;
import dev.clockwork.module.Module;
import dev.clockwork.setting.Settings.*;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.phys.Vec3;

public class Freecam extends Module {
    public static boolean active;
    private final Num speed = add(new Num("Speed", 1.0, 0.1, 5.0, 0.1));
    private final Num vertical = add(new Num("Vertical speed", 0.8, 0.1, 5.0, 0.1));
    private final Mode mode = add(new Mode("Mode", "Free", "Tethered"));
    private final Num tether = add(new Num("Tether radius", 32, 4, 128, 1));
    private final Bool smooth = add(new Bool("Smooth", true));

    private RemotePlayer cam;
    private Vec3 origin, vel = Vec3.ZERO;

    public Freecam() { super("Freecam", "Detach the camera from your body.", Category.PLAYER); }

    @Override public void onEnable() {
        if (mc.player == null || mc.level == null) { enabled = false; return; }
        cam = new RemotePlayer(mc.level, mc.player.getGameProfile());
        cam.copyPosition(mc.player);
        cam.setYHeadRot(mc.player.getYHeadRot());
        origin = mc.player.position();
        vel = Vec3.ZERO;
        active = true;
        mc.setCameraEntity(cam);
    }
    @Override public void onDisable() {
        active = false;
        if (mc.player != null) mc.setCameraEntity(mc.player);
        cam = null;
    }
    @Override public void onTick() {
        if (cam == null || mc.player == null) return;
        // your real body stays put (KeyboardInputMixin zeroes its input); the mouse still turns the camera
        cam.setYRot(mc.player.getYRot()); cam.setXRot(mc.player.getXRot());
        cam.setYHeadRot(mc.player.getYRot());
        var o = mc.options;
        double f = (o.keyUp.isDown() ? 1 : 0) - (o.keyDown.isDown() ? 1 : 0);
        double s = (o.keyLeft.isDown() ? 1 : 0) - (o.keyRight.isDown() ? 1 : 0);
        double v = (o.keyJump.isDown() ? 1 : 0) - (o.keyShift.isDown() ? 1 : 0);
        double yaw = Math.toRadians(cam.getYRot());
        Vec3 want = new Vec3(
                -Math.sin(yaw) * f + Math.cos(yaw) * s, v * vertical.get() / speed.get(),
                 Math.cos(yaw) * f + Math.sin(yaw) * s).scale(speed.get());
        vel = smooth.on() ? vel.lerp(want, 0.35) : want;
        Vec3 next = cam.position().add(vel);
        if (mode.is("Tethered") && next.distanceTo(origin) > tether.get()) return;
        cam.setOldPosAndRot();
        cam.setPos(next);
    }
}
