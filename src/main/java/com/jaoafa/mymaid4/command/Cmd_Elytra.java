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
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class Cmd_Elytra extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "elytra",
            "プレイヤーにエリトラと花火を付与します。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "プレイヤーにエリトラと花火を付与します。")
                .senderType(Player.class)
                .handler(this::giveElytra)
                .build()
        );
    }

    void giveElytra(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        ItemStack elytra = new ItemStack(Material.ELYTRA);
        ItemStack fireworks = new ItemStack(Material.FIREWORK_ROCKET, 64);

        PlayerInventory inv = player.getInventory();
        ItemStack offhand = inv.getItemInOffHand();

        boolean changed = false;

        if (offhand.getType() != fireworks.getType()) {
            inv.setItemInOffHand(fireworks);
            SendMessage(player, details(), "花火をオフハンドのアイテムと置きかえました。");
            changed = true;

            if (offhand.getType() != Material.AIR) {
                if (player.getInventory().firstEmpty() == -1) {
                    player.getLocation().getWorld().dropItem(player.getLocation(), offhand);
                    SendMessage(player, details(), "インベントリがいっぱいだったため、既にオフハンドに持っていたアイテムはあなたの足元にドロップしました。");
                } else {
                    inv.addItem(offhand);
                }
            }
        }

        ItemStack chestplate = inv.getChestplate();

        if (chestplate == null || chestplate.getType() != elytra.getType()) {
            inv.setChestplate(elytra);
            SendMessage(player, details(), "エリトラを装備しました。");
            changed = true;

            if (chestplate != null && chestplate.getType() != Material.AIR) {
                if (player.getInventory().firstEmpty() == -1) {
                    player.getLocation().getWorld().dropItem(player.getLocation(), chestplate);
                    SendMessage(player, details(), "インベントリがいっぱいだったため、既に胴体につけていたアイテムはあなたの足元にドロップしました。");
                } else {
                    inv.addItem(chestplate);
                }
            }
        }

        if (!changed) {
            SendMessage(player, details(), "エリトラと花火は既に装備しています。");
        }
    }
}
