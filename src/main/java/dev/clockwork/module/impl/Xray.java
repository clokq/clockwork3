package dev.clockwork.module.impl;

import dev.clockwork.module.Category;
import dev.clockwork.module.Module;
import dev.clockwork.setting.Settings.*;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Hides every block face except the selected ones. Hooked from BlockMixin. */
public class Xray extends Module {
    public static Xray INSTANCE;
    private final Mode mode = add(new Mode("Mode", "Ores", "Ores + Storage", "Custom"));
    private final Bool coal = add(new Bool("Coal", true)), iron = add(new Bool("Iron", true)),
            copper = add(new Bool("Copper", true)), gold = add(new Bool("Gold", true)),
            redstone = add(new Bool("Redstone", true)), lapis = add(new Bool("Lapis", true)),
            diamond = add(new Bool("Diamond", true)), emerald = add(new Bool("Emerald", true)),
            quartz = add(new Bool("Nether quartz", true)), debris = add(new Bool("Ancient debris", true)),
            fluids = add(new Bool("Show lava/water", true)), spawners = add(new Bool("Spawners", true));

    public Xray() { super("Xray", "Only renders the blocks you choose.", Category.RENDER); INSTANCE = this; }

    @Override public void onEnable()  { rebuild(); }
    @Override public void onDisable() { rebuild(); }
    @Override public void onSettingChanged() { rebuild(); }
    private void rebuild() { if (mc.levelRenderer != null) mc.levelRenderer.allChanged(); }

    public boolean visible(BlockState s) {
        if (fluids.on() && !s.getFluidState().isEmpty()) return true;
        if (spawners.on() && s.is(Blocks.SPAWNER)) return true;
        if (mode.is("Ores + Storage") && (s.is(Blocks.CHEST) || s.is(Blocks.TRAPPED_CHEST) || s.is(Blocks.BARREL)
                || s.is(BlockTags.SHULKER_BOXES) || s.is(Blocks.DROPPER) || s.is(Blocks.DISPENSER)
                || s.is(Blocks.HOPPER) || s.is(Blocks.ENDER_CHEST))) return true;
        return (coal.on() && s.is(BlockTags.COAL_ORES)) || (iron.on() && s.is(BlockTags.IRON_ORES))
            || (copper.on() && s.is(BlockTags.COPPER_ORES)) || (gold.on() && s.is(BlockTags.GOLD_ORES))
            || (redstone.on() && s.is(BlockTags.REDSTONE_ORES)) || (lapis.on() && s.is(BlockTags.LAPIS_ORES))
            || (diamond.on() && s.is(BlockTags.DIAMOND_ORES)) || (emerald.on() && s.is(BlockTags.EMERALD_ORES))
            || (quartz.on() && s.is(Blocks.NETHER_QUARTZ_ORE)) || (debris.on() && s.is(Blocks.ANCIENT_DEBRIS));
    }
}
