/*
 * jaoLicense
 *
 * Copyright (c) 2022 jao Minecraft Server
 *
 * The following license applies to this project: jaoLicense
 *
 * Japanese: https://github.com/jaoafa/jao-Minecraft-Server/blob/master/jaoLICENSE.md
 * English: https://github.com/jaoafa/jao-Minecraft-Server/blob/master/jaoLICENSE-en.md
 */

package com.jaoafa.mymaid4.event;

import com.jaoafa.mymaid4.lib.Blacklist;
import com.jaoafa.mymaid4.lib.EventPremise;
import com.jaoafa.mymaid4.lib.MyMaidData;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import com.sk89q.worldguard.bukkit.event.block.BreakBlockEvent;
import com.sk89q.worldguard.bukkit.event.block.PlaceBlockEvent;
import com.sk89q.worldguard.bukkit.event.block.UseBlockEvent;
import com.sk89q.worldguard.bukkit.event.entity.DestroyEntityEvent;
import com.sk89q.worldguard.bukkit.event.entity.SpawnEntityEvent;
import com.sk89q.worldguard.bukkit.event.inventory.UseItemEvent;
import com.sk89q.worldguard.bukkit.util.Materials;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Event_BlackList extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "アイテム・ブロックブラックリストに関する処理を行います。";
    }

    // References: https://github.com/EngineHub/WorldGuard/blob/master/worldguard-bukkit/src/main/java/com/sk89q/worldguard/bukkit/listener/BlacklistListener.java

    @EventHandler(ignoreCancelled = true)
    public void onBreakBlock(final BreakBlockEvent event) {
        final Player player = event.getCause().getFirstPlayer();

        if (player == null) {
            return;
        }

        event.filter(target -> checkBlacklist(Blacklist.BlacklistEvent.BREAK, target.getBlock().getType(), player, target.toBlockLocation()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlaceBlock(final PlaceBlockEvent event) {
        Player player = event.getCause().getFirstPlayer();

        if (player == null) {
            return;
        }

        event.filter(target -> checkBlacklist(Blacklist.BlacklistEvent.PLACE, target.getBlock().getType(), player, target.toBlockLocation()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onUseBlock(final UseBlockEvent event) {
        Player player = event.getCause().getFirstPlayer();

        if (player == null) {
            return;
        }

        event.filter(target -> checkBlacklist(Blacklist.BlacklistEvent.INTERACT, target.getBlock().getType(), player, target.toBlockLocation()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpawnEntity(SpawnEntityEvent event) {
        Player player = event.getCause().getFirstPlayer();

        if (player == null) {
            return;
        }

        Material material = Materials.getRelatedMaterial(event.getEffectiveType());
        if (material == null) {
            return;
        }

        boolean bool = checkBlacklist(Blacklist.BlacklistEvent.USE, material, player, event.getTarget());
        if (!bool) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDestroyEntity(DestroyEntityEvent event) {
        Player player = event.getCause().getFirstPlayer();

        if (player == null) {
            return;
        }

        Entity target = event.getEntity();

        if (target instanceof Item item) {

            Material material = Materials.getRelatedMaterial(item.getType());
            if (material == null) {
                return;
            }

            boolean bool = checkBlacklist(Blacklist.BlacklistEvent.ACQUIRE, material, player, event.getTarget());
            if (!bool) event.setCancelled(true);
        }

        Material material = Materials.getRelatedMaterial(target.getType());
        if (material == null) {
            return;
        }
        boolean bool = checkBlacklist(Blacklist.BlacklistEvent.BREAK, material, player, event.getTarget());
        if (!bool) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onUseItem(UseItemEvent event) {
        Player player = event.getCause().getFirstPlayer();

        if (player == null) {
            return;
        }

        ItemStack target = event.getItemStack();

        boolean bool = checkBlacklist(Blacklist.BlacklistEvent.USE, target.getType(), player, player.getLocation());
        if (!bool) event.setCancelled(true);

        if (Materials.isArmor(target.getType()) &&
            !checkBlacklist(Blacklist.BlacklistEvent.EQUIP, target.getType(), player, player.getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Item item = event.getItemDrop();
        Material material = Materials.getRelatedMaterial(item.getType());
        if (material == null) {
            return;
        }

        boolean bool = checkBlacklist(Blacklist.BlacklistEvent.DROP, material, player, player.getLocation());
        if (!bool) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity entity = event.getWhoClicked();
        if (!(entity instanceof Player)) return;
        Inventory inventory = event.getInventory();
        ItemStack item = event.getCurrentItem();

        if (item != null && inventory.getHolder() != null) {
            Player player = (Player) entity;

            boolean bool = checkBlacklist(Blacklist.BlacklistEvent.ACQUIRE, item.getType(), player, player.getLocation());
            if (!bool) {
                event.setCancelled(true);

                if (inventory.getHolder().equals(player)) {
                    event.setCurrentItem(null);
                }
            }

            ItemStack equipped = checkEquipped(event);
            if (equipped != null &&
                !checkBlacklist(Blacklist.BlacklistEvent.EQUIP, item.getType(), player, player.getLocation())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        HumanEntity entity = event.getWhoClicked();
        if (!(entity instanceof Player)) return;
        if (event.getInventory().getType() != InventoryType.PLAYER
            && event.getInventory().getType() != InventoryType.CRAFTING) return;
        if (event.getRawSlots().stream().anyMatch(i -> i >= 5 && i <= 8)) { // dropped on armor slots
            Player player = (Player) entity;

            boolean bool = checkBlacklist(Blacklist.BlacklistEvent.EQUIP, event.getOldCursor().getType(), player, player.getLocation());
            if (!bool) {
                event.setCancelled(true);
            }
        }
    }

    private ItemStack checkEquipped(InventoryClickEvent event) {
        final Inventory clickedInventory = event.getClickedInventory();
        if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
            switch (event.getAction()) {
                case PLACE_ONE:
                case PLACE_SOME:
                case PLACE_ALL:
                case SWAP_WITH_CURSOR:
                    final ItemStack cursor = event.getCursor();
                    if (cursor != null) {
                        return cursor;
                    }
                case HOTBAR_SWAP:
                    if (event.getClick() == ClickType.SWAP_OFFHAND) {
                        return clickedInventory == null ? null : ((PlayerInventory) clickedInventory).getItemInOffHand();
                    }
                    return clickedInventory == null ? null : clickedInventory.getItem(event.getHotbarButton());
                default:
                    break;
            }
        } else if (clickedInventory != null && clickedInventory.getType() == InventoryType.PLAYER
            && event.getView().getTopInventory().getType() == InventoryType.PLAYER
            && event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            return event.getCurrentItem();
        }
        return null;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryCreative(InventoryCreativeEvent event) {
        HumanEntity entity = event.getWhoClicked();
        ItemStack item = event.getCursor();

        if (item.getType() != Material.AIR && entity instanceof Player player) {

            boolean bool = checkBlacklist(Blacklist.BlacklistEvent.ACQUIRE, item.getType(), player, entity.getLocation());
            if (!bool) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        Inventory inventory = player.getInventory();
        ItemStack item = inventory.getItem(event.getNewSlot());

        if (item == null) {
            return;
        }

        boolean bool = checkBlacklist(Blacklist.BlacklistEvent.ACQUIRE, item.getType(), player, player.getLocation());
        if (!bool) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockDispenseArmor(BlockDispenseArmorEvent event) {
        if (!(event.getTargetEntity() instanceof Player player)) return;
        ItemStack stack = event.getItem();

        boolean bool = checkBlacklist(Blacklist.BlacklistEvent.EQUIP, stack.getType(), player, player.getLocation());
        if (!bool) {
            event.setCancelled(true);
        }
    }

    /**
     * ブラックリストをチェックする
     *
     * @param blacklistEvent blacklistEvent
     * @param material       material
     * @param player         player
     * @param loc            loc
     *
     * @return 操作許可する場合はtrue
     */
    boolean checkBlacklist(Blacklist.BlacklistEvent blacklistEvent, Material material, Player player, Location loc) {
        Blacklist blacklist = MyMaidData.getBlacklist();
        if (blacklist == null) {
            return true;
        }

        Set<Blacklist.BlacklistItem> items = blacklist.getItems().stream()
            .filter(item -> item.isEventActive(blacklistEvent))
            .collect(Collectors.toSet());

        Optional<Blacklist.BlacklistItem> optItem = items.stream()
            .filter(blacklistItem -> material == blacklistItem.getMaterial())
            .findFirst();
        if (optItem.isEmpty()) {
            return true;
        }

        Blacklist.BlacklistItem item = optItem.get();
        String group = getPermissionMainGroup(player);
        if (item.getIgnoreGroups().stream()
            .map(String::toLowerCase)
            .anyMatch(s -> s.equalsIgnoreCase(group))) {
            return true;
        }

        if (item.getIgnorePerms() != null && player.hasPermission(item.getIgnorePerms())) {
            return true;
        }

        Blacklist.BlacklistAction[] actions = item.getEventActions(blacklistEvent);
        boolean ret = true;
        for (Blacklist.BlacklistAction action : actions) {
            ret = !action.getHandler().execute(new Blacklist.BlacklistAction.BlacklistContext(item,
                blacklistEvent,
                player,
                loc)) && ret;
        }
        return ret;
    }
}