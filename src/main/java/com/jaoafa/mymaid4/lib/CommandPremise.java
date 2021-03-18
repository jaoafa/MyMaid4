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

package com.jaoafa.mymaid4.lib;

import cloud.commandframework.Command;
import org.bukkit.command.CommandSender;

public interface CommandPremise {
    /**
     * コマンドに関する情報を指定・返却します。
     *
     * @return コマンドの使い方
     */
    MyMaidCommand.Detail getDetails();

    /**
     * コマンドを登録します。
     */
    MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder);
}
