package com.jaoafa.mymaid4.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidData;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Cmd_Ded extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "ded",
            "最後に死亡した場所にテレポートします。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "最後に死亡した場所にテレポートします。")
                .senderType(Player.class)
                .handler(this::tpLastDed)
                .build()
        );
    }

    void tpLastDed(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
            SendMessage(player, details(), "Dedコマンドはサバイバル・アドベンチャーモードでは利用できません。");
            SendMessage(player, details(), "クリエイティブモードに切り替えてから実行してください。");
            SendMessage(player, details(), ChatColor.RED + "" + ChatColor.BOLD + "=== [!] 警告 ===");
            SendMessage(player, details(), ChatColor.RED + "" + ChatColor.BOLD + "PvP等での「/ded」コマンドの利用は原則禁止です！");
            SendMessage(player, details(), ChatColor.RED + "" + ChatColor.BOLD + "多く使用すると迷惑行為として認識される場合もあります！");

            return;
        }
        if (!MyMaidData.getLastded().containsKey(player.getName())) {
            SendMessage(player, details(), "最後に死亡した場所が見つかりませんでした。");
        } else {
            Location location = MyMaidData.getLastded().get(player.getName());
            player.teleport(location);
            String locationDescription = String.format("( X:%s Y:%s Z:%s )", location.getBlockX(), location.getBlockY(), location.getBlockZ());
            SendMessage(player, details(), "最終死亡場所" + ChatColor.BOLD + locationDescription + ChatColor.RESET + "にテレポートしました。");
            SendMessage(player, details(), ChatColor.RED + "" + ChatColor.BOLD + "=== [!] 警告 ===");
            SendMessage(player, details(), ChatColor.RED + "" + ChatColor.BOLD + "PvP等での「/ded」コマンドの利用は原則禁止です！");
            SendMessage(player, details(), ChatColor.RED + "" + ChatColor.BOLD + "多く使用すると迷惑行為として認識される場合もあります！");
        }
    }
}
