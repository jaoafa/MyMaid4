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

import com.destroystokyo.paper.event.server.ServerExceptionEvent;
import com.destroystokyo.paper.exception.ServerException;
import com.jaoafa.mymaid4.Main;
import com.jaoafa.mymaid4.lib.EventPremise;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class Event_ServerExceptionCatcher extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "サーバで例外エラーが発生した場合、報告します。";
    }

    @EventHandler
    public void onServerException(ServerExceptionEvent event) {
        ServerException exception = event.getException();
        if (Main.getRollbar() == null) {
            return;
        }
        Main.getRollbar().critical(exception, "onServerExceptionEvent");
    }
}
