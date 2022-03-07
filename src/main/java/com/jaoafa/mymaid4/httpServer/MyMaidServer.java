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

package com.jaoafa.mymaid4.httpServer;

import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import com.sun.net.httpserver.HttpServer;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class MyMaidServer extends BukkitRunnable {
    static HttpServer server;

    public static void stopServer() {
        server.stop(0);
    }

    @Override
    public void run() {
        int port = 31001;
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", exc -> {
                final String response = "{\"status\":true,\"message\":\"Hello world.\"}";
                exc.sendResponseHeaders(200, response.length());
                OutputStream os = exc.getResponseBody();
                os.write(response.getBytes());
                os.close();
            });
            server.createContext("/votefill", new HTTP_VoteFill());
            server.createContext("/docs", new HTTP_GetDocs());
            server.start();
        } catch (IOException e) {
            MyMaidLibrary.reportError(getClass(), e);
        }
    }
}
