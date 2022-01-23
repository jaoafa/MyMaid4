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

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.standard.EnumArgument;
import cloud.commandframework.bukkit.parsers.OfflinePlayerArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import com.jaoafa.mymaid4.lib.TpDeny;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class Cmd_TpDeny extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "tpdeny",
            "TpDeny(特定ユーザーからのテレポート拒否)の設定をします。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(cloud.commandframework.Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "TpDenyにプレイヤーを追加し、以降のテレポートを拒否します。")
                .senderType(Player.class)
                .literal("add")
                .argument(OfflinePlayerArgument.of("target"),
                    ArgumentDescription.of("拒否する対象プレイヤー"))
                .handler(this::addTpDeny)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "TpDenyからプレイヤーを解除し、以降のテレポートを許可します。")
                .senderType(Player.class)
                .literal("remove")
                .argument(OfflinePlayerArgument.of("target"),
                    ArgumentDescription.of("解除する対象プレイヤー"))
                .handler(this::removeTpDeny)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "テレポートを拒否した場合に通知するかどうかを設定します。")
                .senderType(Player.class)
                .literal("notify")
                .argument(OfflinePlayerArgument.of("target"),
                    ArgumentDescription.of("通知設定する対象プレイヤー"))
                .argument(EnumArgument.of(TpDenyNotify.class, "changeTo"),
                    ArgumentDescription.of("通知をするか"))
                .handler(this::notifyTpDeny)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "現在TpDenyに追加されている(テレポートを拒否されている)プレイヤーの一覧を表示します。")
                .senderType(Player.class)
                .literal("list")
                .handler(this::listTpDeny)
                .build()
        );
    }

    void addTpDeny(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        OfflinePlayer target = context.getOrDefault("target", null);
        if (target == null) {
            SendMessage(player, details(), "targetが指定されていません。");
            return;
        }
        TpDeny tpdeny = new TpDeny(player);
        if (tpdeny.isTpDeny(target)) {
            SendMessage(player, details(), "指定されたプレイヤーからのテレポートは既に拒否しています。");
            return;
        }
        boolean bool = tpdeny.addDeny(target);
        SendMessage(player, details(), "指定されたプレイヤー「" + target.getName() + "」のテレポート拒否に" + (bool ? "成功" : "失敗") + "しました。");
    }

    void removeTpDeny(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        OfflinePlayer target = context.getOrDefault("target", null);
        if (target == null) {
            SendMessage(player, details(), "targetが指定されていません。");
            return;
        }
        TpDeny tpdeny = new TpDeny(player);
        if (!tpdeny.isTpDeny(target)) {
            SendMessage(player, details(), "指定されたプレイヤーからのテレポートを拒否していません。");
            return;
        }
        boolean bool = tpdeny.disableDeny(target);
        SendMessage(player, details(), "指定されたプレイヤー「" + target.getName() + "」のテレポート拒否解除に" + (bool ? "成功" : "失敗") + "しました。");
    }

    void notifyTpDeny(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        OfflinePlayer target = context.getOrDefault("target", null);
        if (target == null) {
            SendMessage(player, details(), "targetが指定されていません。");
            return;
        }
        TpDenyNotify changeTo = context.getOrDefault("changeTo", null);
        TpDeny tpdeny = new TpDeny(player);
        if (!tpdeny.isTpDeny(target)) {
            SendMessage(player, details(), "指定されたプレイヤーからのテレポートを拒否していません。");
            return;
        }
        boolean notifySetting = changeTo == TpDenyNotify.On;
        boolean bool = tpdeny.setNotify(target, notifySetting);
        SendMessage(player, details(), "指定されたプレイヤー「" + target.getName() + "」からのテレポート通知設定を" + (notifySetting ? "オン" : "オフ") + "にすることに" + (bool ? "成功" : "失敗") + "しました。");
    }

    void listTpDeny(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        TpDeny tpdeny = new TpDeny(player);
        // TODO リスト表示綺麗にしてもいいかも
        List<TpDeny.TpDenyData> denys = tpdeny.getDenys();
        SendMessage(player, details(), "TpDeny list / count: " + denys.size());
        for (TpDeny.TpDenyData deny : denys) {
            SendMessage(player, details(), "[" + deny.id() + "] " + deny.target().getName() + " / created_at: " + MyMaidLibrary.sdfFormat(deny.created_at()) + " / updated_at: " + MyMaidLibrary.sdfFormat(deny.updated_at()));
        }
    }

    enum TpDenyNotify {
        On,
        Off
    }
}
