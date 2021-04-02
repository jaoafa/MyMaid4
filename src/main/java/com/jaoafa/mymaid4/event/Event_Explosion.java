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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;

public class Event_Explosion extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "TNTなどの爆発時に額縁や絵画などが壊れないようにします。";
    }

    @EventHandler
    public void onHangingDamageByTNT(HangingBreakEvent event) {
        if (event.getCause() != RemoveCause.EXPLOSION) {
            return;
        }
        event.setCancelled(true);
    }
}