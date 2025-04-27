package com.swoopy.lumberjacked.listeners;

import com.swoopy.lumberjacked.Lumberjacked;
import com.swoopy.lumberjacked.files.PlacedBlocks;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TreeChoppedListener implements Listener {
    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent e)
    {
        Player p = e.getPlayer();
        ItemStack itemInHand = p.getInventory().getItemInMainHand();
        Block b = e.getBlock();

        int blockX = b.getLocation().getBlockX();
        int blockY = b.getLocation().getBlockY();
        int blockZ = b.getLocation().getBlockZ();

        String path = blockX + "," + blockY + "," + blockZ;

        boolean isPlayerPlacedBlock = PlacedBlocks.get().isInt(path);

        if (
                Lumberjacked.AXES.contains(itemInHand.getType())
                && Lumberjacked.LOGS.contains(b.getType())
                && !isPlayerPlacedBlock && p.isSneaking()
        ) {
            this.checkNearbyBlocks(b, p);
        } else if (isPlayerPlacedBlock && Lumberjacked.LOGS.contains(b.getType())) {
            PlacedBlocks.get().set(path, null);
        }
    }

        public void checkNearbyBlocks(Block block, Player player) {
        int x = -1;
        int y = -1;
        int z = -1;

        outerLoop:
        for (int k = 0; k < 3; k++) {

            for (int i = 0; i < 3; i++) {

                for (int j = 0; j < 3; j++) {
                    Location center = block.getLocation().clone();

                    center.add(x, y, z);

                    Block nearbyBlock = center.getBlock();

                    if (Lumberjacked.LOGS.contains(nearbyBlock.getType())) {
                        ItemStack tool = player.getInventory().getItemInMainHand();

                        nearbyBlock.breakNaturally(tool);

                        int toolDurability = tool.getType().getMaxDurability();

                        ItemMeta meta = tool.getItemMeta();

                        Damageable dmg = (Damageable) meta;

                        if (dmg != null && dmg.getHealth() <= toolDurability) {

                            dmg.damage(1);

                            tool.setItemMeta((ItemMeta) dmg);

                            player.getInventory().setItemInMainHand(tool);
                        } else {
                            player.getInventory().setItemInMainHand(null);
                            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1F, 1F);
                            break outerLoop;
                        }

                        this.checkNearbyBlocks(nearbyBlock, player);

                    }

                    z++;
                }

                x++;
                z = -1;
            }

            y++;
            x = -1;
            z = -1;
        }
    }
}