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
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.lib.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Cmd_VarCmd extends MyMaidLibrary implements CommandPremise {
    final Pattern pattern = Pattern.compile("\\$([a-zA-Z][a-zA-Z0-9_.]{2,})\\$");

    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "varcmd",
            "変数を含むコマンドの変数を置き換え、実行します。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "変数を含むコマンドの変数を置き換え、実行します。")
                .argument(StringArgument.greedy("command"), ArgumentDescription.of("変数を含むコマンド"))
                .handler(this::runCommand)
                .build()
        );
    }

    void runCommand(CommandContext<CommandSender> context) {
        CommandSender sender = context.getSender();
        String command = context.get("command");

        command = Arrays
            .stream(command.split(" "))
            .map(s -> s.startsWith("@") ?
                Bukkit.selectEntities(sender, s)
                    .stream()
                    .map(CommandSender::getName)
                    .collect(Collectors.joining(", ")) :
                s)
            .collect(Collectors.joining(" "));

        VariableManager vm = MyMaidData.getVariableManager();

        Matcher m = pattern.matcher(command);
        while (m.find()) {
            String varName = m.group(1);

            String reserved = replaceReservedVars(varName);
            if (reserved != null) {
                command = command.replaceAll(Pattern.quote(m.group()), reserved);
                continue;
            }

            if (vm.isDefined(varName)) {
                command = command.replaceAll(Pattern.quote(m.group()), vm.getString(varName));
            }
        }
        Bukkit.dispatchCommand(sender, command);
    }

    String replaceReservedVars(String key) {
        switch (key) {
            case "DateTime_Year":
                return new SimpleDateFormat("yyyy").format(new Date());
            case "DateTime_Month":
                return new SimpleDateFormat("MM").format(new Date());
            case "DateTime_Day":
                return new SimpleDateFormat("dd").format(new Date());
            case "DateTime_Hour":
                return new SimpleDateFormat("HH").format(new Date());
            case "DateTime_Minute":
                return new SimpleDateFormat("mm").format(new Date());
            case "DateTime_Second":
                return new SimpleDateFormat("ss").format(new Date());
            case "DateTime_MillSecond":
                return new SimpleDateFormat("S").format(new Date());
            case "PlayerCount":
                return String.valueOf(Bukkit.getServer().getOnlinePlayers().size());
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!key.equals("Damager_" + p.getName())) {
                continue;
            }
            EntityDamageEvent ede = p.getLastDamageCause();
            if (ede == null) {
                continue;
            }
            Entity e = ede.getEntity();
            return e.getName();
        }

        ScoreboardManager sbm = Bukkit.getScoreboardManager();
        Scoreboard sb = sbm.getMainScoreboard();
        for (Objective obj : sb.getObjectives()) {
            Pattern p = Pattern.compile("^Score_" + obj.getName() + "_(.+?)$");
            if (!p.matcher(key).matches()) {
                continue;
            }
            Matcher m = p.matcher(key);

            if (m.find()) {
                return String.valueOf(obj.getScore(m.group(1)).getScore());
            }
        }

        for (Team team : sb.getTeams()) {
            if (!key.equals("TeamCount_" + team.getName())) {
                continue;
            }
            return String.valueOf(team.getEntries().size());
        }
        return null;
    }
}
