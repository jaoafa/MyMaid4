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

package com.jaoafa.mymaid4.lib;

import com.jaoafa.mymaid4.Main;

import java.sql.*;

public class MySQLDBManager {
    private final String hostname;
    private final int port;
    private final String password;
    private final String username;
    private final String database;
    Connection conn = null;
    private long WAIT_TIMEOUT = -1;
    private long LAST_PACKET = -1;

    public MySQLDBManager(String hostname, int port,
                          String username, String password, String database) throws ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver");
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.password = password;
        this.database = database;
    }

    public Connection getConnection() throws SQLException {
        if (conn != null && !conn.isClosed() && conn.isValid(5)) {
            if (WAIT_TIMEOUT != -1 && LAST_PACKET != -1) {
                long diff = (System.currentTimeMillis() - LAST_PACKET) / 1000;
                if (diff < WAIT_TIMEOUT) {
                    return conn;
                } else {
                    Main.getMyMaidLogger().info("MySQL TIMEOUT! WAIT_TIMEOUT: " + WAIT_TIMEOUT + " / DIFF: " + diff);
                }
            }
            LAST_PACKET = System.currentTimeMillis();
            return conn;
        }
        String jdbcUrl = String.format("jdbc:mysql://%s:%d/%s?autoReconnect=true&useUnicode=true&characterEncoding=utf8", this.hostname, this.port, this.database);
        conn = DriverManager.getConnection(jdbcUrl, this.username, this.password);
        if (WAIT_TIMEOUT == -1) {
            WAIT_TIMEOUT = getWaitTimeout();
        }
        LAST_PACKET = System.currentTimeMillis();
        conn.setHoldability(ResultSet.CLOSE_CURSORS_AT_COMMIT);
        return conn;
    }

    long getWaitTimeout() {
        try {
            Connection conn = getConnection();
            PreparedStatement statement = conn.prepareStatement("show variables like 'wait_timeout'");
            ResultSet res = statement.executeQuery();
            if (res.next()) {
                WAIT_TIMEOUT = res.getInt("Value");
                Main.getMyMaidLogger().info("MySQL WAIT_TIMEOUT: " + WAIT_TIMEOUT);
            } else {
                WAIT_TIMEOUT = -1;
            }
            statement.close();
        } catch (SQLException e) {
            WAIT_TIMEOUT = -1;
        }
        return WAIT_TIMEOUT;
    }
}
