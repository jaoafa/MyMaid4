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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class Event_GameModeCmd extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "gamemodeコマンドが実行された際に、gコマンドを勧めます。";
    }

    @EventHandler
    public void onGameModeCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage();
        Player player = event.getPlayer();
        String[] args = command.split(" ");
        if (args.length == 0) {
            return; // 本来発生しないと思うけど
        }
        if (!args[0].equalsIgnoreCase("/gamemode")
            && !args[0].equalsIgnoreCase("/minecraft:gamemode")) {
            return; // gamemodeコマンド以外
        }
        if (isAMRV(player)) {
            player.sendMessage(Component.text().append(
                Component.text("[GameMode]"),
                Component.space(),
                Component.text("ゲームモードを変更するには g コマンドがお勧めです。数字で変更もできますよ。", NamedTextColor.GREEN)
            ));
        }
        if (args.length < 3) {
            return; // 2以内
        }
        // /gamemode <mode> [player]
        if (isAMR(player)) {
            return;
        }
        player.sendMessage(Component.text().append(
            Component.text("[GameMode]"),
            Component.space(),
            Component.text("あなたの権限では他のユーザーのゲームモードを変更することはできません。", NamedTextColor.GREEN)
        ));
        player.sendMessage(Component.text().append(
            Component.text("[GameMode]"),
            Component.space(),
            Component.text("自身のゲームモードを変更する場合はプレイヤー名を入れずに入力してください。", NamedTextColor.GREEN)
        ));
        event.setCancelled(true);
    }
}
