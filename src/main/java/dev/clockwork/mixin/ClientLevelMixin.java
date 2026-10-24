package dev.clockwork.mixin;

import dev.clockwork.module.impl.SkyModule;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
    // NOTE: 26.x moved sky colour towards environment attributes; if this target no longer exists the
    // mixin is skipped (require = 0). Retarget at wherever SKY_COLOR is resolved in your 26.2 jar.
    @Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true, require = 0)
    private void clockwork$sky(Vec3 pos, float partial, CallbackInfoReturnable<Integer> cir) {
        SkyModule s = SkyModule.INSTANCE;
        if (s != null && s.enabled) cir.setReturnValue(0xFF000000 | s.apply(cir.getReturnValue() & 0xFFFFFF));
    }
}
