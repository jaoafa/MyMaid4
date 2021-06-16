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

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.bukkit.parsers.PlayerArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.Main;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidData;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Cmd_Show extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "show",
            "姿を見えるようにします。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "姿を見えるようにします。")
                .senderType(Player.class)
                .handler(this::liftHid)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "指定したプレイヤーの姿を見えるようにします。")
                .argument(PlayerArgument.of("target"), ArgumentDescription.of("対象のプレイヤー"))
                .handler(this::liftHidOther)
                .build()
        );
    }

    void liftHid(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        if (!isAMR(player)) {
            SendMessage(player, details(), "あなたの権限ではこのコマンドを実行することができません！");
            return;
        }

        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            p.showPlayer(Main.getJavaPlugin(), player);
        }
        if (MyMaidData.isHid(player.getUniqueId())) {
            MyMaidData.removeHid(player.getUniqueId());
        }
        SendMessage(player, details(), "あなたは他のプレイヤーから見えるようになりました。");
    }

    void liftHidOther(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        Player target = context.get("target");
        if (!isAMR(player)) {
            SendMessage(player, details(), "あなたの権限ではこのコマンドを実行することができません！");
            return;
        }
        if (!isAMR(target)) {
            SendMessage(player, details(), "対象のプレイヤーの権限がRegular以上でないため、実行できません。");
            return;
        }

        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            p.showPlayer(Main.getJavaPlugin(), target);
        }
        if (MyMaidData.isHid(target.getUniqueId())) {
            MyMaidData.removeHid(target.getUniqueId());
        }
        SendMessage(target, details(), "あなたは他のプレイヤーから見えるようになりました。");
        SendMessage(player, details(), "プレイヤー「" + target.getName() + "」をから見えるようにしました。");
    }
}
