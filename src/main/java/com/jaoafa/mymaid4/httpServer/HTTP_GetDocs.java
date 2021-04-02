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

package com.jaoafa.mymaid4.httpServer;

import com.jaoafa.mymaid4.Main;
import com.jaoafa.mymaid4.lib.MyMaidData;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class HTTP_GetDocs implements HttpHandler {
    public void handle(HttpExchange t) throws IOException {
        Headers resHeaders = t.getResponseHeaders();
        resHeaders.set("Content-Type", "application/json");
        resHeaders.set("Last-Modified",
            ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.RFC_1123_DATE_TIME));

        PluginDescriptionFile desc = Main.getJavaPlugin().getDescription();
        String nowVer = desc.getVersion();
        resHeaders.set("Server", "MyMaid Server (" + nowVer + ")");

        long contentLength = MyMaidData.getGetDocsData().toString().getBytes(StandardCharsets.UTF_8).length;
        t.sendResponseHeaders(200, contentLength);

        OutputStream os = t.getResponseBody();
        os.write(MyMaidData.getGetDocsData().toString().getBytes(StandardCharsets.UTF_8));
        os.close();
    }
}
