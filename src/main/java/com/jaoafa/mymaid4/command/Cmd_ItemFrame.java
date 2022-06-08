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

package com.jaoafa.mymaid4.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Rotation;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Cmd_ItemFrame extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "itemframe",
            "アイテムフレームに関する操作を行えます。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "見ているアイテムフレームを透明化します。")
                .literal("invisible")
                .senderType(Player.class)
                .handler(this::setSeeItemFrameInvisible)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "見ているアイテムフレームの土台ブロックがなくなってもアイテムフレームが壊れないようにします。")
                .literal("disable-break", "not-break")
                .senderType(Player.class)
                .handler(this::setItemFrameNotBreak)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "tomapで作成し設置した、見ているアイテムフレームを修正します。")
                .literal("tomap", "fix-tomap")
                .senderType(Player.class)
                .handler(this::fixToMapItemFrame)
                .build()
        );
    }

    void setSeeItemFrameInvisible(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();

        ItemFrame itemFrame = getLookingItemFrame(player);
        if (itemFrame == null) {
            SendMessage(player, details(), "見ているアイテムフレームが見つかりません。");
            return;
        }

        if (!canEdit(player, itemFrame.getLocation())) {
            SendMessage(player, details(), "このアイテムフレームを編集する権限がありません。");
            return;
        }

        if (!itemFrame.isVisible()) {
            SendMessage(player, details(), "見ているアイテムフレームは既に透明化されています。");
            return;
        }

        itemFrame.setVisible(false);
        SendMessage(player, details(), "アイテムフレームを透明化しました。");
    }

    void setItemFrameNotBreak(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();

        ItemFrame itemFrame = getLookingItemFrame(player);
        if (itemFrame == null) {
            SendMessage(player, details(), "見ているアイテムフレームが見つかりません。");
            return;
        }

        if (!canEdit(player, itemFrame.getLocation())) {
            SendMessage(player, details(), "このアイテムフレームを編集する権限がありません。");
            return;
        }

        if (itemFrame.isFixed()) {
            SendMessage(player, details(), "見ているアイテムフレームは既に壊れないように設定されています。");
            return;
        }

        itemFrame.setFixed(true);
        SendMessage(player, details(), "アイテムフレームを壊れないように設定しました。");
    }

    void fixToMapItemFrame(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();

        ItemFrame itemFrame = getLookingItemFrame(player);
        if (itemFrame == null) {
            SendMessage(player, details(), "見ているアイテムフレームが見つかりません。");
            return;
        }

        if (!canEdit(player, itemFrame.getLocation())) {
            SendMessage(player, details(), "このアイテムフレームを編集する権限がありません。");
            return;
        }

        if (itemFrame.getItem().getType() == Material.AIR) {
            SendMessage(player, details(), "アイテムフレームにアイテムが設定されていません。");
            return;
        }

        ItemStack item = itemFrame.getItem();
        ItemMeta meta = item.getItemMeta();
        meta.displayName(null);
        meta.removeItemFlags(ItemFlag.values());
        item.setItemMeta(meta);
        itemFrame.setItem(item);
        itemFrame.setRotation(Rotation.NONE);

        SendMessage(player, details(), "アイテムフレームを修正しました。");
    }

    ItemFrame getLookingItemFrame(Player player) {
        @NotNull List<Entity> entities = player.getNearbyEntities(30, 30, 30);
        ItemFrame looking = null;
        for (Entity e : entities) {
            if (!(e instanceof ItemFrame)) {
                continue;
            }
            if (isEntityLooking(player, e)) {
                looking = (ItemFrame) e;
                break;
            }
        }
        return looking;
    }

    boolean canEdit(Player player, Location loc) {
        Plugin worldGuard = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
        if (worldGuard != null && worldGuard.isEnabled()) {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();

            boolean canBypass = WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(WorldGuardPlugin.inst().wrapPlayer(player), BukkitAdapter.adapt(loc.getWorld()));
            return canBypass || query.testState(BukkitAdapter.adapt(loc), WorldGuardPlugin.inst().wrapPlayer(player), Flags.BUILD);
        }
        return true;
    }
}
