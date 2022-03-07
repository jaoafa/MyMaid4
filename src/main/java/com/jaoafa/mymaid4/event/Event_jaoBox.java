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

import com.jaoafa.mymaid4.Main;
import com.jaoafa.mymaid4.command.Cmd_jaoBox;
import com.jaoafa.mymaid4.lib.EventPremise;
import com.jaoafa.mymaid4.lib.MyMaidData;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import com.jaoafa.mymaid4.lib.NMSManager;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Event_jaoBox extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "jaoBoxに関するイベントを管理します。";
    }

    @EventHandler
    public void onRegisterInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (event.getView().title() == Cmd_jaoBox.viewerTitleComponent) {
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1.0F, 1.0F);
            return;
        }
        if (event.getView().title() != Cmd_jaoBox.registerTitleComponent) {
            return;
        }

        Inventory inventory = event.getInventory();
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("items", inventory.getContents());
        try {
            yaml.save(Cmd_jaoBox.file);
        } catch (IOException e) {
            MyMaidLibrary.reportError(getClass(), e);
        }

        List<ItemStack> oldItems = MyMaidData.getBoxPrevious(player.getUniqueId());
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack oldItem = oldItems.get(i);
            ItemStack newItem = inventory.getItem(i);

            if (Objects.equals(oldItem, newItem)) {
                continue;
            }

            logging(player, i, oldItem, newItem);
        }

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CHEST_LOCKED, 1.0F, 1.0F);
        event.getPlayer().sendMessage("[jaoBox] " + ChatColor.GREEN + "jaoBoxを更新しました。");
    }

    private void logging(Player player, int slot, @Nullable ItemStack oldItem, @Nullable ItemStack newItem) {
        Path jaoBoxLogPath = Path.of(Main.getJavaPlugin().getDataFolder().getPath(), "jaoBoxLog.tsv");
        String message = String.join("\t",
            player.getName(),
            player.getUniqueId().toString(),
            Integer.toString(slot),
            oldItem != null ? oldItem.getType().toString() : null,
            oldItem != null ? NMSManager.getNBT(oldItem) : null,
            newItem != null ? newItem.getType().toString() : null,
            newItem != null ? NMSManager.getNBT(newItem) : null
        );
        try {
            Files.writeString(jaoBoxLogPath, message + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            MyMaidLibrary.reportError(getClass(), e);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClickBox(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        Block block = event.getClickedBlock();

        if (action != Action.RIGHT_CLICK_BLOCK || block == null || block.getType() != Material.CHEST) {
            return;
        }

        org.bukkit.block.Sign sign = getNearSign(block);
        if (sign == null) {
            return;
        }
        if (sign.lines().stream().noneMatch(line -> PlainTextComponentSerializer.plainText().serialize(line).equals("[jaoBox]"))) {
            return;
        }

        event.setCancelled(true);
        Cmd_jaoBox.openBox(player, Cmd_jaoBox.viewerTitleComponent);
    }

    private org.bukkit.block.Sign getNearSign(Block block) {
        List<Material> signs = Arrays
            .stream(Material.values())
            .filter(material -> material.data == Sign.class || material.data == WallSign.class)
            .toList();
        for (BlockFace face : BlockFace.values()) {
            if (signs.contains(block.getRelative(face).getType())) {
                return (org.bukkit.block.Sign) block.getRelative(face).getState();
            }
        }
        return null;
    }
}