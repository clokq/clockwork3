package dev.clockwork.module.impl;

import dev.clockwork.module.Category;
import dev.clockwork.module.Module;
import dev.clockwork.setting.Settings.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

/** Tells you when a spectator is sitting on you or someone is staring at you. */
public class AntiVanish extends Module {
    private final Mode notify = add(new Mode("Notify", "Chat", "Action bar", "Both"));
    private final Bool spectators = add(new Bool("Detect spectators", true));
    private final Bool looking = add(new Bool("Detect looking", true));
    private final Num spectateDist = add(new Num("Spectator distance", 2.5, 1, 16, 0.5));
    private final Num lookRange = add(new Num("Look range", 24, 4, 128, 1));
    private final Num lookAngle = add(new Num("Look angle", 12, 2, 45, 1));
    private final Num cooldown = add(new Num("Cooldown (s)", 10, 1, 120, 1));
    private final Bool tabListOnly = add(new Bool("Tab-list spectators nearby", true));

    private final Map<String, Long> last = new HashMap<>();

    public AntiVanish() { super("AntiVanish", "Alerts on spectators and players looking at you.", Category.PLAYER); }

    @Override public void onTick() {
        if (mc.player == null || mc.level == null || mc.getConnection() == null) return;
        Vec3 me = mc.player.getEyePosition();
        for (AbstractClientPlayer p : mc.level.players()) {
            if (p == mc.player || p == mc.getCameraEntity()) continue;
            PlayerInfo info = mc.getConnection().getPlayerInfo(p.getUUID());
            boolean spec = info != null && info.getGameMode() == GameType.SPECTATOR;
            double dist = p.position().distanceTo(mc.player.position());
            if (spectators.on() && (spec || p.isSpectator()) && dist <= spectateDist.get()) {
                alert(p.getUUID(), p.getName().getString() + " is spectating you");
                continue;
            }
            if (looking.on() && !spec && dist <= lookRange.get()) {
                Vec3 to = me.subtract(p.getEyePosition()).normalize();
                double ang = Math.toDegrees(Math.acos(Math.max(-1, Math.min(1, p.getLookAngle().dot(to)))));
                if (ang <= lookAngle.get() && p.hasLineOfSight(mc.player))
                    alert(p.getUUID(), p.getName().getString() + " is looking at you");
            }
        }
        if (tabListOnly.on()) {
            // spectator on the tab list whose entity is near you but isn't a normal visible player
            for (PlayerInfo info : mc.getConnection().getOnlinePlayers()) {
                if (info.getGameMode() != GameType.SPECTATOR || info.getProfile().id().equals(mc.player.getUUID())) continue;
                AbstractClientPlayer e = null;
                for (AbstractClientPlayer p : mc.level.players()) if (p.getUUID().equals(info.getProfile().id())) e = p;
                if (e != null && e.position().distanceTo(mc.player.position()) <= spectateDist.get())
                    alert(info.getProfile().id(), info.getProfile().name() + " is spectating you");
            }
        }
    }

    private void alert(UUID id, String msg) {
        String key = id + msg;
        long now = System.currentTimeMillis();
        if (now - last.getOrDefault(key, 0L) < cooldown.get() * 1000) return;
        last.put(key, now);
        Component c = Component.literal("[Clockwork] ").withStyle(ChatFormatting.GOLD)
                .append(Component.literal(msg).withStyle(ChatFormatting.WHITE));
        if (!notify.is("Action bar")) mc.player.displayClientMessage(c, false);
        if (!notify.is("Chat")) mc.player.displayClientMessage(c, true);
    }
}
