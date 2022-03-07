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

import com.jaoafa.mymaid4.Main;
import com.jaoafa.mymaid4.lib.EventPremise;
import com.jaoafa.mymaid4.lib.MyMaidData;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import com.jaoafa.mymaid4.lib.NMSManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

public class Event_AntiToolbar extends MyMaidLibrary implements Listener, EventPremise {
    final Pattern damagePattern = Pattern.compile("\\{Damage:[0-9]+}");
    final Map<UUID, ItemStack> pickupItems = new HashMap<>();
    final static boolean isCollectCreativeItems = false;

    @Override
    public String description() {
        return "ツールバーの利用を制限します。";
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryCreative(InventoryCreativeEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (isAMR(player)) {
            return;
        }

        // マイクラバージョン変更時用、クリエイティブアイテム集積処理
        if (isCollectCreativeItems) {
            Path path = Path.of("creative-items.tsv");
            Path path_all = Path.of("creative-all-items.tsv");
            try {
                String prevString = "";
                if (Files.exists(path)) {
                    prevString = Files.readString(path);
                }
                String allString = "";
                if (Files.exists(path_all)) {
                    allString = Files.readString(path_all);
                }
                ItemStack is = event.getCursor();
                Material material = is.getType();
                String nbt = NMSManager.getNBT(is);
                if (nbt == null) {
                    return;
                }
                if (allString.contains(material + "\t" + nbt)) {
                    return;
                }
                if (!nbt.equals("{}")) {
                    prevString += material + "\t" + nbt + "\n";
                    player.sendMessage(Component.text(material + " " + nbt, NamedTextColor.YELLOW));
                } else {
                    player.sendMessage(Component.text(material + " " + nbt, NamedTextColor.GREEN));
                }
                allString += material + "\t" + nbt + "\n";
                Files.writeString(path, prevString);
                Files.writeString(path_all, allString);

            } catch (IOException e) {
                MyMaidLibrary.reportError(getClass(), e);
            }
            return;
        }

        if (MyMaidData.getCreativeInventoryWithNBTs().isEmpty()) {
            return;
        }

        if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
            pickupItems.put(player.getUniqueId(), event.getCurrentItem());
        }
        if (event.getCursor().getType() == Material.AIR) {
            return;
        }

        boolean isDeny = isDenyItemStack(event.getCursor());
        ItemStack is = event.getCursor();

        if (!isDeny) {
            return;
        }

        if (isExistsInventory(player.getInventory(), is)) {
            return;
        }

        if (pickupItems.containsKey(player.getUniqueId()) &&
            pickupItems.get(player.getUniqueId()).equals(is) &&
            pickupItems.get(player.getUniqueId()).getItemMeta().equals(is.getItemMeta())) {
            return;
        }

        if (is.getType() == Material.SPLASH_POTION &&
            is.getType() == Material.LINGERING_POTION &&
            isjaoium(((PotionMeta) is.getItemMeta()).getCustomEffects())) {
            // jaoiumだったらどうせ処罰されることになるので
            return;
        }

        if (is.getType() == Material.FILLED_MAP) {
            return; // #533 埋めてあるマップは除外
        }

        event.setCurrentItem(null);
        event.setCursor(new ItemStack(Material.AIR));
        player.sendMessage(Component.text().append(
            Component.text("[AntiToolbar] "),
            Component.text("ツールバーからの取得と思われるアイテムが見つかったため、規制しました。この事象は報告されます。", NamedTextColor.RED)
        ));
        try {
            saveToolbarItem(player, is);
        } catch (IOException e) {
            MyMaidLibrary.reportError(getClass(), e);
        }
        event.setCancelled(true);
    }

    boolean isDenyItemStack(ItemStack is) {
        Material material = is.getType();
        String nbt = NMSManager.getNBT(is);
        if (nbt == null) {
            return false;
        }

        Map<Material, List<String>> creativeInventoryWithNBTs = MyMaidData.getCreativeInventoryWithNBTs();
        if (!creativeInventoryWithNBTs.containsKey(material)) {
            if (damagePattern.matcher(nbt).matches()) {
                return false; // ダメージ値のみの場合
            }
            return !nbt.equals("{}");
        }
        List<String> registeredNBT = creativeInventoryWithNBTs.get(material);
        if (damagePattern.matcher(nbt).matches()) return false;
        return !registeredNBT.contains(nbt);
    }

    void saveToolbarItem(Player player, ItemStack is) throws IOException {
        String nbt = NMSManager.getNBT(is);
        Path path = Paths.get(Main.getJavaPlugin().getDataFolder().getAbsolutePath(), "toolbar-items.tsv");
        List<String> lines = Files.exists(path) ? Files.readAllLines(path) : new ArrayList<>();
        String messageNonTime = player.getName() + "\t" + is.getType().name() + "\t" + nbt;
        if (lines.stream().anyMatch(s -> s.startsWith(messageNonTime))) {
            return;
        }
        lines.add(messageNonTime + "\t" + sdfFormat(new Date()));
        Files.write(path, lines, StandardCharsets.UTF_8);
    }

    boolean isExistsInventory(Inventory inv, @NotNull ItemStack is) {
        return Arrays
            .stream(inv.getContents())
            .filter(Objects::nonNull)
            .anyMatch(item ->
                item.getType() == is.getType() && item.getItemMeta() != null && item.getItemMeta().equals(is.getItemMeta())
            );
    }
}
