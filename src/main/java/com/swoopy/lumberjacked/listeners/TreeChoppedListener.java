package com.swoopy.lumberjacked.listeners;

import com.swoopy.lumberjacked.Lumberjacked;
import com.swoopy.lumberjacked.files.PlacedBlocks;
import java.util.LinkedList;
import java.util.Queue;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Sound;
import java.util.Random;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;


public class TreeChoppedListener implements Listener {
    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent e)
    {
        Player p = e.getPlayer();
        Block b = e.getBlock();

        if(Lumberjacked.LOGS.contains(b.getType()) && !this.isPlayerPlacedBlock(b)) {
            Block baseBlock = findTreeBase(b);
            this.checkNearbyBlocks(b, p);
            this.replantSapling(baseBlock);
        }
    }

    private final Random random = new Random();

    private void damageTool(Player player, ItemStack tool) {
        if (tool == null || !tool.getType().isItem()) return;
        if (!(tool.getType().getMaxDurability() > 0)) return; // Only damage tools

        ItemMeta meta = tool.getItemMeta();
        if (!(meta instanceof Damageable damageable)) return;

        int unbreakingLevel = tool.getEnchantmentLevel(Enchantment.UNBREAKING);

        // Simulate durability loss considering Unbreaking enchant
        boolean shouldTakeDamage = true;
        if (unbreakingLevel > 0) {
            shouldTakeDamage = random.nextInt(unbreakingLevel + 1) == 0; // Vanilla Minecraft logic
        }

        if (shouldTakeDamage) {
            int newDamage = damageable.getDamage() + 1;
            if (newDamage >= tool.getType().getMaxDurability()) {
                // Break the tool
                player.getInventory().setItemInMainHand(null);
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0F, 1.0F);
            } else {
                damageable.setDamage(newDamage);
                tool.setItemMeta((ItemMeta) damageable);
            }
        }
    }


    public void checkNearbyBlocks(Block startBlock, Player player) {
        Queue<Block> queue = new LinkedList<>();
        Set<Location> visitedLogs = new HashSet<>(); // Store logs we broke
        queue.add(startBlock);

        ItemStack tool = player.getInventory().getItemInMainHand();

        int processedBlocks = 0;
        int maxBlocks = 150; // Limit for safety

        while (!queue.isEmpty() && processedBlocks < maxBlocks) {
            Block current = queue.poll();
            if (current == null) continue;

            if (!Lumberjacked.LOGS.contains(current.getType())) continue;

            current.breakNaturally(tool);
            damageTool(player, tool);
            visitedLogs.add(current.getLocation());
            processedBlocks++;

            // Explore nearby blocks
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;

                        Block neighbor = current.getLocation().clone().add(dx, dy, dz).getBlock();
                        if (neighbor == null) continue;

                        if (Lumberjacked.LOGS.contains(neighbor.getType()) && !visitedLogs.contains(neighbor.getLocation())) {
                            queue.add(neighbor);
                        }
                    }
                }
            }
        }

        // Now that all logs are broken, clean up nearby leaves
        removeNearbyLeaves(visitedLogs, player);
    }

    private void removeNearbyLeaves(Set<Location> logLocations, Player player) {
        ItemStack tool = player.getInventory().getItemInMainHand();

        for (Location logLoc : logLocations) {
            for (int dx = -3; dx <= 3; dx++) {
                for (int dy = -3; dy <= 3; dy++) {
                    for (int dz = -3; dz <= 3; dz++) {
                        Location leafLoc = logLoc.clone().add(dx, dy, dz);
                        Block leafBlock = leafLoc.getBlock();

                        if (leafBlock != null && Lumberjacked.LEAVES.contains(leafBlock.getType())) {
                            leafBlock.breakNaturally(tool);
                            damageTool(player, tool);
                        }
                    }
                }
            }
        }
    }

    private Block findTreeBase(Block startBlock) {
        Block current = startBlock;
        Material logType = startBlock.getType();

        while (true) {
            Block below = current.getRelative(0, -1, 0);
            if (below.getType() == logType) {
                current = below;
            } else {
                break;
            }
        }
        return current;
    }

    private void replantSapling(Block baseBlock) {
        Material saplingMaterial = getSaplingForLog(baseBlock.getType());
        if (saplingMaterial == null) return; // Not a valid tree type

        // Delay a little to avoid replacing too early
        new BukkitRunnable() {
            @Override
            public void run() {
                Block blockBelow = baseBlock.getLocation().getBlock();
                if (blockBelow.getType() == Material.AIR || blockBelow.isPassable()) {
                    baseBlock.setType(saplingMaterial);
                }
            }
        }.runTaskLater(Lumberjacked.getInstance(), 5L); // 5 ticks delay (~0.25 seconds)
    }

    private Material getSaplingForLog(Material logMaterial) {
        return switch (logMaterial) {
            case OAK_LOG -> Material.OAK_SAPLING;
            case SPRUCE_LOG -> Material.SPRUCE_SAPLING;
            case BIRCH_LOG -> Material.BIRCH_SAPLING;
            case JUNGLE_LOG -> Material.JUNGLE_SAPLING;
            case ACACIA_LOG -> Material.ACACIA_SAPLING;
            case DARK_OAK_LOG -> Material.DARK_OAK_SAPLING;
            case MANGROVE_LOG -> Material.MANGROVE_PROPAGULE; // Mangrove uses propagule instead of sapling
            case CHERRY_LOG -> Material.CHERRY_SAPLING;
            default -> null;
        };
    }


    private boolean isPlayerPlacedBlock(Block b)
    {
        int blockX = b.getLocation().getBlockX();
        int blockY = b.getLocation().getBlockY();
        int blockZ = b.getLocation().getBlockZ();

        String path = blockX + "," + blockY + "," + blockZ;
        return PlacedBlocks.get().isInt(path);
    }

}