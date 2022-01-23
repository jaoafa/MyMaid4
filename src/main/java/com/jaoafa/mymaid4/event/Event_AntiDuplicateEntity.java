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

package com.jaoafa.mymaid4.event;

import com.jaoafa.mymaid4.lib.EventPremise;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class Event_AntiDuplicateEntity extends MyMaidLibrary implements Listener, EventPremise {
    @EventHandler
    public static void onCommand(PlayerCommandPreprocessEvent event) {
        if (isAMR(event.getPlayer())) return;

        String[] limitedCmd = new String[]{"//copy", "//paste", "//stack"};
        String cmd = event.getMessage();

        for (String cmdStart : limitedCmd) {
            if (cmd.startsWith(cmdStart) && cmd.contains("-e")) {
                event.getPlayer().sendMessage(Component.text("[AntiDuplicateEntity] あなたの権限ではエンティティを複製することが出来ません！", NamedTextColor.RED));
                event.setCancelled(true);
                return;
            }
        }
    }

    @Override
    public String description() {
        return "Default・Verified権限グループのプレイヤーによるエンティティのコピーを制限します。";
    }
}
