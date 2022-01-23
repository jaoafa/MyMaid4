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

import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class NMSManager {
    /**
     * ItemStackの文字列NBTタグを返します
     *
     * @param is ItemStack
     *
     * @return 文字列NBTタグ
     */
    @Nullable
    public static String getNBT(ItemStack is) {
        NBTItem nbtItem = new NBTItem(is);
        return nbtItem.toString();
    }

    /**
     * ItemStackに指定されたNBTタグをつけたItemStackを返します
     *
     * @param is    ItemStack
     * @param key   NBTタグのキー
     * @param value NBTタグの値
     *
     * @return 指定されたNBTタグをつけたItemStack
     */
    @Nullable
    public static ItemStack setNBTString(ItemStack is, String key, String value) {
        NBTItem nbtItem = new NBTItem(is);
        nbtItem.setString(key, value);
        return nbtItem.getItem();
    }

    /**
     * ItemStackに指定されたNBTタグがついているかどうかを判定します
     *
     * @param is  ItemStack
     * @param key NBTタグのキー
     *
     * @return NBTタグがついているか
     */
    public static boolean hasNBT(ItemStack is, String key) {
        NBTItem nbtItem = new NBTItem(is);
        return nbtItem.hasKey(key);
    }

    /**
     * ItemStackから指定されたNBTタグの値を取得します
     *
     * @param is  ItemStack
     * @param key NBTタグのキー
     *
     * @return NBTタグの値
     */
    public static String getNBTString(ItemStack is, String key) {
        NBTItem nbtItem = new NBTItem(is);
        return nbtItem.getString(key);
    }
}
