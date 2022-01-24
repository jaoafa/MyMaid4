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

package com.jaoafa.mymaid4.lib;

import cloud.commandframework.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MyMaidCommand {
    public static class Detail {
        private final String name;
        private final List<String> aliases;
        private final String description;

        public Detail(String name, String description) {
            this.name = name;
            this.aliases = new ArrayList<>();
            this.description = description;
        }

        public Detail(String name, List<String> aliases, String description) {
            this.name = name;
            this.aliases = aliases;
            this.description = description;
        }

        /**
         * コマンド名を返します
         *
         * @return コマンド名
         */
        public String getName() {
            return name;
        }

        /**
         * コマンドのエイリアスを返します
         *
         * @return コマンドのエイリアス
         */
        public List<String> getAliases() {
            return aliases;
        }

        /**
         * コマンドの説明を返します。
         *
         * @return コマンドの説明
         */
        public String getDescription() {
            return description;
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    public static class Cmd {
        private final Command<CommandSender>[] commands;

        @SafeVarargs
        public Cmd(Command<CommandSender>... commands) {
            this.commands = commands;
        }

        /**
         * Commandリストを返します
         *
         * @return Commandリスト
         */
        public List<Command<CommandSender>> getCommands() {
            return Arrays.asList(this.commands);
        }
    }
}
