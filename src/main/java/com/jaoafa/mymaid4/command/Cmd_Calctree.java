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
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.Main;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class Cmd_Calctree extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "calctree",
            "WorldEditの選択範囲で植木算を行います。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "WorldEditの選択範囲で植木算を行います。")
                .argument(BooleanArgument.optional("placeEdgeTree", true), ArgumentDescription.of("両端に木を置くかどうか。"))
                .senderType(Player.class)
                .handler(this::calcTree)
                .build()
        );
    }

    void calcTree(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        WorldEditPlugin we = Main.getWorldEdit();

        if (we == null) {
            SendMessage(player, details(), "WorldEditプラグインが動作していないため、このコマンドを使用できません。");
            return;
        }

        Region region;
        boolean placeEdgeTree = context.get("placeEdgeTree");
        try {
            World selectionWorld = we.getSession(player).getSelectionWorld();
            region = we.getSession(player).getSelection(selectionWorld);
        } catch (IncompleteRegionException e) {
            SendMessage(player, details(), "WorldEditで範囲を指定してください。");
            return;
        }
        int length = region.getLength();
        int width = region.getWidth();
        if (length <= 5 && width <= 5) {
            SendMessage(player, details(), "狭すぎるので植木算出来ません。");
            return;
        }
        int lengthFinal = Math.max(length, width);
        int currentTreeNum = 2;
        int maxTreeNum = lengthFinal / 2;

        Map<Integer, Integer> result = calc(lengthFinal, currentTreeNum, maxTreeNum, placeEdgeTree);
        //50行以上になりそうなら余りを削る
        if (result.values().size() > 50) {
            result.forEach((k, v) -> {
                if (v != 0) {
                    result.remove(k, v);
                }
            });
        }

        result.forEach((k, v) -> {
            if (v == 0) {
                SendMessage(player, details(), Component.text().append(
                    Component.text(String.format("[余剰無し] 間隔:%s", k), NamedTextColor.GREEN)
                ).build());
            } else {
                SendMessage(player, details(), Component.text().append(
                    Component.text(String.format("[余剰有り] 間隔:%s 余剰:%s", k, v), NamedTextColor.RED)
                ).build());
            }
        });

    }

    Map<Integer, Integer> calc(int length, int currentTreeNum, int maxTreeNum, boolean placeEdgeTree) {
        Map<Integer, Integer> result = new HashMap<>();
        for (; currentTreeNum < maxTreeNum; currentTreeNum++) {
            if (placeEdgeTree) {
                int kankaku;
                int amari = 0;
                kankaku = (length - currentTreeNum) / (currentTreeNum - 1);
                if (length != (kankaku * (currentTreeNum - 1) + currentTreeNum)) {
                    amari = length - (kankaku * (currentTreeNum - 1) + currentTreeNum);
                }
                result.put(kankaku, amari);
            } else {
                int kankaku;
                int amari = 0;
                kankaku = (length - currentTreeNum) / (currentTreeNum + 1);
                if (length != (kankaku * (currentTreeNum + 1) + currentTreeNum)) {
                    amari = length - (kankaku * (currentTreeNum + 1) + currentTreeNum);
                }
                result.put(kankaku, amari);
            }
        }
        return result;
    }
}
