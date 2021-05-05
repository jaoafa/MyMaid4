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

package com.jaoafa.mymaid4.event;

import com.jaoafa.mymaid4.lib.EventPremise;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class Event_LookingBlock extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "「LookingBlock」が含まれるコマンドが実行された際に自動で見ているブロックのIDに置き換えます。";
    }

    @EventHandler
    public void onSetHandFacingCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage();
        Player player = event.getPlayer();
        if (!command.contains("lookb")) {
            return; //LookingBlock以外
        }
        Block lookingBlock = player.getTargetBlock(15);
        if (isAMRV(player)) {
            if (lookingBlock == null) {
                player.sendMessage("[LookB] あなたは何も見ていません！");
                return;
            }
            event.setMessage(command.replace("lookb", lookingBlock.getBlockData().getMaterial().getKey().getKey()));
        }
    }
}
