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
import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import com.jaoafa.mymaid4.lib.TeleportAlias;
import org.bukkit.command.CommandSender;

import java.util.Map;

public class Cmd_TpAlias extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "tpalias",
            "テレポートエイリアスに関する操作を行います。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "tpコマンド実行時、プレイヤー名を置き換えるように設定します。")
                .literal("set", "add")
                .argument(StringArgument.of("target"), ArgumentDescription.of("置き換える対象のエイリアス名"))
                .argument(StringArgument.of("replacement"), ArgumentDescription.of("置き換えるエイリアス名"))
                .handler(this::setAlias)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "tpコマンド実行時の置き換え設定を削除します。")
                .literal("remove", "rem", "rm", "del")
                .argument(StringArgument.of("target"), ArgumentDescription.of("置き換える対象のエイリアス名"))
                .handler(this::removeAlias)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "tpコマンド実行時の置き換え設定一覧を表示します。")
                .literal("list", "ls", "status")
                .handler(this::listAlias)
                .build()
        );
    }

    void setAlias(CommandContext<CommandSender> context) {
        String target = context.get("target");
        String replacement = context.get("replacement");
        if (TeleportAlias.getAlias().containsKey(target)) {
            SendMessage(context.getSender(), details(), "既に登録されているエイリアスです。");
            return;
        }
        boolean result = TeleportAlias.setAlias(target, replacement);
        SendMessage(context.getSender(), details(), "追加に" + (result ? "成功" : "失敗") + "しました。");
    }

    void removeAlias(CommandContext<CommandSender> context) {
        String target = context.get("target");
        if (!TeleportAlias.getAlias().containsKey(target)) {
            SendMessage(context.getSender(), details(), "指定されたエイリアスは登録されていません。");
            return;
        }
        boolean result = TeleportAlias.removeAlias(target);
        SendMessage(context.getSender(), details(), "削除に" + (result ? "成功" : "失敗") + "しました。");
    }

    void listAlias(CommandContext<CommandSender> context) {
        SendMessage(context.getSender(), details(), "----- TeleportAlias List -----");
        for (Map.Entry<String, String> one : TeleportAlias.getAlias().entrySet()) {
            SendMessage(context.getSender(), details(), one.getKey() + " -> " + one.getValue());
        }
    }
}
