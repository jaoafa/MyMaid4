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

package com.jaoafa.mymaid4.tasks;

import com.jaoafa.mymaid4.lib.Jail;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class Task_AutoRemoveJailByjaoium extends BukkitRunnable {
    private final Player player;

    public Task_AutoRemoveJailByjaoium(Player player) {
        this.player = player;
    }

    @Override
    public void run() {
        if (!player.isOnline()) {
            return;
        }
        Jail jail = Jail.getInstance(player);
        if (!jail.isStatus()) {
            return;
        }
        String reason = jail.getReason();
        if (reason == null || !reason.equals("jaoium所持")) {
            return;
        }
        jail.removeBan("jaotan");
    }
}