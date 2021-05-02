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
import com.jaoafa.mymaid4.lib.*;
import org.bukkit.command.CommandSender;

public class Cmd_MyMaid4 extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "mymaid4",
            "MyMaid4基本コマンド"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .literal("reload-blacklist", ArgumentDescription.of("ブラックリストを再読み込みします。"))
                .handler(context -> {
                    MyMaidData.setBlacklist(new Blacklist());
                    SendMessage(context.getSender(), details(), "ブラックリストを再読み込みしました。");
                })
                .build()
        );
    }
}
