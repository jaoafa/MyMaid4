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
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapAPI;

public class Event_SpectatorDynmapHide extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "スペクテイターモードの際、Dynmapから姿を隠します。";
    }

    @EventHandler(priority = EventPriority.MONITOR,
                  ignoreCancelled = true)
    public void onChangedGameMode(PlayerGameModeChangeEvent event) {
        if (isDisabledPlugin("dynmap")) {
            return;
        }

        if (event.getNewGameMode() == GameMode.SPECTATOR) {
            getDynmapAPI().setPlayerVisiblity(event.getPlayer(), false);
        } else if (event.getPlayer().getGameMode() == GameMode.SPECTATOR) {
            getDynmapAPI().setPlayerVisiblity(event.getPlayer(), true);
        }
    }

    /**
     * DynmapAPIを返す
     *
     * @return DynmapAPI
     */
    DynmapAPI getDynmapAPI() {
        Plugin dynmap = Bukkit.getPluginManager().getPlugin("dynmap");
        return (DynmapAPI) dynmap;
    }
}
