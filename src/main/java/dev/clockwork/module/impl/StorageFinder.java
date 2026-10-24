package dev.clockwork.module.impl;

import dev.clockwork.setting.Settings.*;
import net.minecraft.world.level.block.entity.*;

public class StorageFinder extends BlockEsp {
    private final Bool chests   = add(new Bool("Chests", true));
    private final Bool barrels  = add(new Bool("Barrels", true));
    private final Bool shulkers = add(new Bool("Shulkers", true));
    private final Bool droppers = add(new Bool("Droppers", true));
    private final Bool hoppers  = add(new Bool("Hoppers", false));
    private final Color chestColor   = add(new Color("Chest colour", 0.09f, 0.9f));
    private final Color barrelColor  = add(new Color("Barrel colour", 0.06f, 0.9f));
    private final Color shulkerColor = add(new Color("Shulker colour", 0.78f, 0.9f));
    private final Color dropperColor = add(new Color("Dropper colour", 0.55f, 0.9f));
    private final Color hopperColor  = add(new Color("Hopper colour", 0.0f, 0.9f));

    public StorageFinder() { super("StorageFinder", "Highlights chests, shulkers and droppers."); }

    @Override protected Color colorFor(BlockEntity be) {
        if (be instanceof ChestBlockEntity)       return chests.on()   ? chestColor   : null; // incl. trapped
        if (be instanceof BarrelBlockEntity)      return barrels.on()  ? barrelColor  : null;
        if (be instanceof ShulkerBoxBlockEntity)  return shulkers.on() ? shulkerColor : null;
        if (be instanceof DropperBlockEntity)     return droppers.on() ? dropperColor : null; // before dispenser
        if (be instanceof HopperBlockEntity)      return hoppers.on()  ? hopperColor  : null;
        return null;
    }
}
