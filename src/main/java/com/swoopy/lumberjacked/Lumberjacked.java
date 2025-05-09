package com.swoopy.lumberjacked;

import com.google.common.collect.ImmutableList;
import com.swoopy.lumberjacked.files.PlacedBlocks;
import com.swoopy.lumberjacked.listeners.TreeChoppedListener;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

public final class Lumberjacked extends JavaPlugin {

    private static Lumberjacked instance;

    public static Lumberjacked getInstance() {
        return instance;
    }

    public static final ImmutableList<Material> LOGS = ImmutableList.of(
            Material.ACACIA_LOG,
            Material.BIRCH_LOG,
            Material.JUNGLE_LOG,
            Material.OAK_LOG,
            Material.MANGROVE_LOG,
            Material.SPRUCE_LOG,
            Material.DARK_OAK_LOG,
            Material.CHERRY_LOG,
            Material.WARPED_STEM,
            Material.CRIMSON_STEM
    );

    public static ImmutableList<Material> LEAVES = ImmutableList.of(
            Material.ACACIA_LEAVES,
            Material.BIRCH_LEAVES,
            Material.JUNGLE_LEAVES,
            Material.OAK_LEAVES,
            Material.MANGROVE_LEAVES,
            Material.SPRUCE_LEAVES,
            Material.DARK_OAK_LEAVES,
            Material.CHERRY_LEAVES
    );

    public static final ImmutableList<Material> AXES = ImmutableList.of(
            Material.DIAMOND_AXE,
            Material.GOLDEN_AXE,
            Material.IRON_AXE,
            Material.STONE_AXE,
            Material.NETHERITE_AXE,
            Material.WOODEN_AXE
    );

    @Override
    public void onEnable() {
        instance = this;
        this.getServer().getPluginManager().registerEvents(new TreeChoppedListener(), this);
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();

        PlacedBlocks.setup();
        PlacedBlocks.get().options().copyDefaults(true);
        PlacedBlocks.save();
    }

    @Override
    public void onDisable() {
        instance = null;
        // Plugin shutdown logic
    }
}
