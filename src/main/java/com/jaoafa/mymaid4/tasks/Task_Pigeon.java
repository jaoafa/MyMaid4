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

import com.jaoafa.mymaid4.lib.MyMaidData;
import org.bukkit.scheduler.BukkitRunnable;

public class Task_Pigeon extends BukkitRunnable {
    @Override
    public void run() {
        if (MyMaidData.getCarrierPigeon() == null) {
            return;
        }
        MyMaidData.getCarrierPigeon().randomBroadcast();
    }
}
