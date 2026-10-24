package dev.clockwork.module.impl;

import dev.clockwork.setting.Settings.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;

public class SpawnerFinder extends BlockEsp {
    private final Color color = add(new Color("Colour", 0.0f, 1.0f));
    public SpawnerFinder() { super("SpawnerFinder", "Highlights monster spawners."); }
    @Override protected Color colorFor(BlockEntity be) { return be instanceof SpawnerBlockEntity ? color : null; }
}
