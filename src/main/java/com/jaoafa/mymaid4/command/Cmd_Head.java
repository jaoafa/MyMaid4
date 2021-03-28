/*
 * jaoLicense
 *
 * Copyright (c) 2021 jao Minecraft Server
 *
 * The following license applies to this project: jaoLicense
 *
 * Japanese: https://github.com/jaoafa/jao-Minecraft-Server/blob/master/jaoLICENSE.md
 * English: https://github.com/jaoafa/jao-Minecraft-Server/blob/master/jaoLICENSE-en.md
 */

package com.jaoafa.mymaid4.command;

import cloud.commandframework.Command;
import cloud.commandframework.bukkit.parsers.PlayerArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.SkullMeta;

public class Cmd_Head extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "head",
            "頭ブロックを入手します。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "自分の頭ブロックを入手します。")
                .senderType(Player.class)
                .handler(this::giveMyHead)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "指定したプレイヤーの頭ブロックを入手します。")
                .argument(PlayerArgument.of("player"))
                .handler(this::givePlayerHead)
                .build()
        );
    }

    void giveMyHead(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        String name = player.getName();

        ItemStack skull = new ItemStack(Material.LEGACY_SKULL);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skull.setDurability((short) 3);
        skullMeta.setOwningPlayer(player);
        skull.setItemMeta(skullMeta);
        PlayerInventory inv = player.getInventory();
        ItemStack main = inv.getItemInMainHand();

        inv.setItemInMainHand(skull);
        SendMessage(player, details(), "「" + name + "の頭」をメインハンドのアイテムと置きかえました。");

        if(main != null && main.getType() != Material.AIR){
            if(player.getInventory().firstEmpty() == -1){
                player.getLocation().getWorld().dropItem(player.getLocation(), main);
                SendMessage(player, details(), "インベントリがいっぱいだったため、既に持っていたアイテムはあなたの足元にドロップしました。");
            }else{
                inv.addItem(main);
            }
        }
    }

    void givePlayerHead(CommandContext<CommandSender> context) {
        Player player = context.getOrDefault("player", null);
        OfflinePlayer offplayer = Bukkit.getOfflinePlayer(context.getOrDefault("player",""));
        String name = offplayer.getName();
        ItemStack skull = new ItemStack(Material.LEGACY_SKULL);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skull.setDurability((short) 3);
        skullMeta.setOwningPlayer(offplayer);
        skull.setItemMeta(skullMeta);
        PlayerInventory inv = player.getInventory();
        ItemStack main = inv.getItemInMainHand();

        inv.setItemInMainHand(skull);
        SendMessage(player, details(), "「" + name + "の頭」をメインハンドのアイテムと置きかえました。");

        if(main != null && main.getType() != Material.AIR){
            if(player.getInventory().firstEmpty() == -1){
                player.getLocation().getWorld().dropItem(player.getLocation(), main);
                SendMessage(player, details(), "インベントリがいっぱいだったため、既に持っていたアイテムはあなたの足元にドロップしました。");
            }else{
                inv.addItem(main);
            }
        }
    }
}