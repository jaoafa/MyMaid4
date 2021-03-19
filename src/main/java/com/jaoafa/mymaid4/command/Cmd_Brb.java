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

public class Cmd_Brb extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail getDetails() {
        return new MyMaidCommand.Detail(
            "brb",
            "バリアブロックを入手します。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "バリアブロックをコマンド実行者のメインハンドのアイテムと置き換えます。")
                .handler(this::giveBarrier)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "バリアブロックを指定したプレイヤーのメインハンドのアイテムと置き換えます。")
                .senderType(Player.class)
                .argument(PlayerArgument.of("player"))
                .handler(this::giveBarrierToPlayer)
                .build()
        );
    }

    void giveBarrier(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        ItemStack is = new ItemStack(Material.BARRIER);

        PlayerInventory inv = player.getInventory();
        ItemStack main = inv.getItemInMainHand();

        inv.setItemInMainHand(is);
        SendMessage(player, getDetails(), "バリアブロックをメインハンドのアイテムと置きかえました。");

        if (main.getType() != Material.AIR) {
            if (player.getInventory().firstEmpty() == -1) {
                player.getLocation().getWorld().dropItem(player.getLocation(), main);
                SendMessage(player, getDetails(), "インベントリがいっぱいだったため、既に持っていたアイテムはあなたの足元にドロップしました。");
            } else {
                inv.addItem(main);
            }
        }
    }

    void giveBarrierToPlayer(CommandContext<CommandSender> context) {
        Player player = context.getOrDefault("player", null);
        if (player == null) {
            SendMessage(context.getSender(), getDetails(), "プレイヤーは指定されていないか存在しません。");
            return;
        }

        ItemStack is = new ItemStack(Material.BARRIER);

        PlayerInventory inv = player.getInventory();
        ItemStack main = inv.getItemInMainHand();

        inv.setItemInMainHand(is);
        SendMessage(context.getSender(), getDetails(), "バリアブロックをプレイヤー「" + player.getName() + "」のメインハンドのアイテムと置きかえました。");

        if (main.getType() != Material.AIR) {
            if (player.getInventory().firstEmpty() == -1) {
                player.getLocation().getWorld().dropItem(player.getLocation(), main);
                SendMessage(player, getDetails(), "インベントリがいっぱいだったため、既に持っていたアイテムはあなたの足元にドロップしました。");
            } else {
                inv.addItem(main);
            }
        }
    }
}
