package com.jaoafa.mymaid4.command;

import cloud.commandframework.Command;
import cloud.commandframework.bukkit.parsers.PlayerArgument;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Cmd_Dedbull extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "dedbull",
            "プレイヤーに暗視効果を付与します。すでに付与されている場合は削除します。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "プレイヤーに暗視効果を付与します。すでに付与されている場合は削除します。")
                .senderType(Player.class)
                .handler(this::addDedbull)
                .build()
        );
    }

    void addDedbull(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)){
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            SendMessage(player, details(), "Dedbullの効果を除去しました。");
        }else {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1, false, false));
            SendMessage(player, details(), "Dedbullを飲みました。");
        }

    }
}
