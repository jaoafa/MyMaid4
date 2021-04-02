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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class Event_AntiTargetAllEntityCmd extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "全てのエンティティをターゲットとするコマンドの使用を禁止します。";
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage();
        Player player = event.getPlayer();
        String[] args = command.split(" ");
        if (args.length == 0) {
            return;
        }
        for (String arg : args) {
            if (arg.equalsIgnoreCase("@e")) {
                player.chat("開けてみたいでしょ～？");
                player.chat("うん、みたーい！");
                player.chat("行きますよー！");
                player.chat("はい！");
                player.chat("せーのっ！");
                player.chat("あぁ～！水素の音ォ〜！！");
                player.chat("(私は@eを含むコマンドを実行しました)");
                checkSpam(player);
                event.setCancelled(true);
                return;
            }
        }
    }
}
