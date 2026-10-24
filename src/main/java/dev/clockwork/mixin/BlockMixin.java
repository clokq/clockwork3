package dev.clockwork.mixin;

import dev.clockwork.module.impl.Xray;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public abstract class BlockMixin {
    @Inject(method = "shouldRenderFace", at = @At("HEAD"), cancellable = true, require = 0)
    private static void clockwork$xray(BlockState state, BlockState neighbor, Direction side,
                                       CallbackInfoReturnable<Boolean> cir) {
        Xray x = Xray.INSTANCE;
        if (x != null && x.enabled) cir.setReturnValue(x.visible(state));
    }
}
