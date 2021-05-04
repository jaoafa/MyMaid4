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

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.BooleanArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
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
                .argument(BooleanArgument.optional("placeEdgeTree",true))
                .senderType(Player.class)
                .handler(this::calcTree)
                .build()
        );
    }

    void calcTree(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        WorldEditPlugin we = getWorldEdit();
        Region region = null;
        boolean placeEdgeTree = context.getOrDefault("placeEdgeTree",true);
        try {
            World selectionWorld = we.getSession(player).getSelectionWorld();
            region = we.getSession(player).getSelection(selectionWorld);

        } catch (IncompleteRegionException e) {
            SendMessage(player, details(), "WorldEditで範囲を指定してください。");
            return;
        }
        int height = region.getHeight();
        int width = region.getWidth();
        if (height <= 3 && width <= 3) {
            SendMessage(player, details(), "狭すぎるので植木算出来ません。");
            return;
        }
        if (height != 1 && width != 1) {
            SendMessage(player, details(), "一列で選択してください。");
            return;
        }
        int length = height*width;
        int currentTreeNum = 2;
        int maxTreeNum = length/2;

        Map<Integer, Integer> result = calc(length,currentTreeNum,maxTreeNum,placeEdgeTree);
        //50行以上になりそうなら余りを削る
        if (result.values().size()>50){
            result.forEach((k,v) -> {
                if (v!=0){
                    result.remove(k,v);
                }
            });
        }

        result.forEach((k,v) ->{
            if (v == 0){
                SendMessage(player, details(), Component.text().append(
                    Component.text(String.format("[余剰無し] 間隔:%s",k),NamedTextColor.GREEN)
                ).build());
            }else {
                SendMessage(player, details(), Component.text().append(
                    Component.text(String.format("[余剰有り] 間隔:%s 余剰:%s",k,v),NamedTextColor.RED)
                ).build());
            }
        });

    }

    Map<Integer,Integer> calc(int length, int currentTreeNum,int maxTreeNum, boolean placeEdgeTree) {
        Map<Integer,Integer> result = new HashMap<>();
        for(;currentTreeNum<maxTreeNum;currentTreeNum++){
            if (placeEdgeTree){
                int kankaku;
                int amari;
                kankaku = length/(currentTreeNum-1);
                amari = length-(kankaku*currentTreeNum+1+currentTreeNum);
                result.put(kankaku,amari);
            }else {
                int kankaku;
                int amari;
                kankaku = length/(currentTreeNum+1);
                amari = length-(kankaku*(currentTreeNum+1)+currentTreeNum);
                result.put(kankaku,amari);
            }
        }
        return result;
    }
}
