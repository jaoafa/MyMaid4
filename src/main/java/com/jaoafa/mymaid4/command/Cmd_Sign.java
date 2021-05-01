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
import cloud.commandframework.arguments.standard.BooleanArgument;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidData;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class Cmd_Sign extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "sign",
            "看板を編集します。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "看板編集モードをオン・オフします。")
                .senderType(Player.class)
                .literal("editmode")
                .argument(BooleanArgument.optional("changeTo"), ArgumentDescription.of("オン・オフのいずれか (未指定の場合トグル)"))
                .handler(this::editMode)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "看板の指定行のテキストを置き換えします。")
                .senderType(Player.class)
                .literal("set")
                .argument(IntegerArgument.<CommandSender>newBuilder("line").withMin(1).withMax(4), ArgumentDescription.of("編集する行"))
                .argument(StringArgument.of("text"), ArgumentDescription.of("置き換えるテキスト"))
                .handler(this::setText)
                .build()
        );
    }

    void editMode(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        Boolean changeTo = context.getOrDefault("changeTo", null);
        if (changeTo == null) {
            changeTo = !MyMaidData.isSignEditing(player.getUniqueId());
        }
        MyMaidData.setSignEditing(player.getUniqueId(), changeTo);
        SendMessage(player, details(), "看板編集モードを" + (changeTo ? "オン" : "オフ") + "にしました。");
        if (changeTo)
            SendMessage(player, details(), "棒で右クリックすると看板を編集できます。");
    }

    void setText(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        int line = context.get("line");
        String text = context.get("text");

        Location loc = MyMaidData.getSelectedSign(player.getUniqueId());
        if (loc == null) {
            SendMessage(player, details(), "看板が選択されていません！棒で編集する看板を選択してください。");
            return;
        }

        Block block = loc.getBlock();
        if (!isSign(block.getType())) {
            SendMessage(player, details(), "選択されているブロックは看板ではないようです。");
            return;
        }

        Plugin worldGuard = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
        if (worldGuard != null && worldGuard.isEnabled()) {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();

            boolean canBypass = WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(WorldGuardPlugin.inst().wrapPlayer(player), BukkitAdapter.adapt(loc.getWorld()));
            if (!canBypass && !query.testState(BukkitAdapter.adapt(loc), WorldGuardPlugin.inst().wrapPlayer(player), Flags.BUILD)) {
                SendMessage(player, details(), "あなたはこの看板を編集できません。");
                return;
            }
        }

        if (line < 0 || line > 4) {
            SendMessage(player, details(), "行は1から4の間で指定してください。");
            return;
        }

        Sign sign = (Sign) block.getState();
        sign.line(line - 1, Component.text(text));
        sign.update();
        SendMessage(player, details(), "編集しました。");
    }

}
