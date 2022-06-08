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
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Tips {
    private static final Path path = Path.of(Main.getJavaPlugin().getDataFolder().getAbsolutePath(), "tips.json");
    private static final Map<String, String> tips = new HashMap<>();

    /**
     * name から Tip を取得する
     *
     * @param name Tip の名前
     *
     * @return Tip (存在しない場合は NULL)
     */
    public static String getTip(String name) {
        if (tips.isEmpty()) {
            loadTips();
        }
        return tips.get(name);
    }

    /**
     * すべての Tip を取得する
     *
     * @return Tip の Map
     */
    public static Map<String, String> getTips() {
        if (tips.isEmpty()) {
            loadTips();
        }
        return tips;
    }

    public static boolean isExist(String name) {
        if (tips.isEmpty()) {
            loadTips();
        }
        return tips.containsKey(name);
    }

    /**
     * Tip を追加する
     *
     * @param name Tip の名前
     * @param text Tip のテキスト
     */
    public static void addTip(String name, String text) {
        tips.put(name, text);
        saveTips();
    }

    /**
     * Tip を削除する
     *
     * @param name Tip の名前
     */
    public static void removeTip(String name) {
        tips.remove(name);
        saveTips();
    }

    private static void loadTips() {
        if (!Files.exists(path)) {
            return;
        }
        tips.clear();
        try {
            JSONObject object = new JSONObject(Files.readString(path));
            for (String key : object.keySet()) {
                tips.put(key, object.getString(key));
            }
        } catch (IOException e) {
            MyMaidLibrary.reportError(Tips.class, e);
        }
    }

    private static void saveTips() {
        JSONObject object = new JSONObject();
        for (String key : tips.keySet()) {
            object.put(key, tips.get(key));
        }
        try {
            Files.writeString(path, object.toString());
        } catch (IOException e) {
            MyMaidLibrary.reportError(Tips.class, e);
        }
    }
}
