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

package com.jaoafa.mymaid4.Lib;

import com.jaoafa.mymaid4.lib.MyMaidConfig;
import com.jaoafa.mymaid4.lib.MyMaidData;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

public class ErrorReporter {
    public static void report(Throwable exception) {
        exception.printStackTrace();
        if (MyMaidData.getReportChannel() == null) {
            System.out.println("Main.ReportChannel == null error.");
            return;
        }
        MyMaidConfig myMaidConfig = new MyMaidConfig();
        if (myMaidConfig.getJDA() == null) {
            System.out.println("Main.getClient() == null error.");
            return;
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);

        try {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("MyMaid3 Discord Error Reporter");
            builder.setColor(Color.RED);
            builder.addField("StackTrace", "```" + sw.toString() + "```", false);
            builder.addField("Message", "```" + exception.getMessage() + "```", false);
            builder.addField("Cause", "```" + exception.getCause() + "```", false);
            builder.setTimestamp(Instant.now());
            MyMaidData.getReportChannel().sendMessage(builder.build()).queue();
        } catch (Exception e) {
            String text = "MyMaid3 Discord Error Reporter (" + MyMaidLibrary.sdfFormat(new Date()) + ")\n"
                + "---------- StackTrace ----------\n"
                + sw.toString() + "\n"
                + "---------- Message ----------\n"
                + exception.getMessage() + "\n"
                + "---------- Cause ----------\n"
                + exception.getCause();
            InputStream stream = new ByteArrayInputStream(
                text.getBytes(StandardCharsets.UTF_8));
            MyMaidData.getReportChannel().sendFile(stream, "Mainreport" + System.currentTimeMillis() + ".txt").queue();
        }
    }
}