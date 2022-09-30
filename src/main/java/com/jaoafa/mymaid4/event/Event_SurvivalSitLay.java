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
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Locale;
import java.util.Set;

public class Event_SurvivalSitLay extends MyMaidLibrary implements Listener, EventPremise {

    @Override
    public String description() {
        return "サバイバルモードなどでsitやlayコマンドを使えないようにします。";
    }

    static final Set<String> targetCommands = Set.of("/sit", "/lay", "/gsit:sit", "/glay:lay");

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage();
        Player player = event.getPlayer();
        if (command.length() == 0) {
            return;
        }
        String[] args = command.split(" ");
        if (!targetCommands.contains(args[0].toLowerCase(Locale.ROOT))) {
            return;
        }
        if (isAMR(player)) {
            return;
        }
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) {
            return;
        }
        player.sendMessage(Component.text().append(
            Component.text("[SitLay] "),
            Component.text("サバイバルモードやアドベンチャーモードでは、このコマンドを実行できません。", NamedTextColor.GREEN)
        ));
        event.setCancelled(true);
    }
}
