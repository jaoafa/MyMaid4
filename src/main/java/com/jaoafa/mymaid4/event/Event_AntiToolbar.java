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
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

/**
 * ツールバーの利用を制限します。
 * <p>
 * ・クリエイティブモードでクリエイティブインベントリやツールバーからアイテムをインベントリに追加する際に {@link InventoryCreativeEvent} が発生することを利用して制限します。
 * ・この機能は Admin, Moderator, Regular には適用されません。
 * ・クリエイティブインベントリに存在するアイテムの一覧はデータフォルダにある creative-items.tsv で定義します。これが未定義の場合はこの機能は動作しません。
 * → このファイルはフラグ isCollectCreativeItems を True にし、クリエイティブインベントリにあるアイテムをすべて入手（ドロップ）することで出力できます。
 * <p>
 * ・クリエイティブインベントリのアイテムと {@link InventoryCreativeEvent#getCursor()} が合致する場合、除外します。
 * ・InventoryCreativeEvent 発生時、{@link InventoryCreativeEvent#getCurrentItem()} が NULL ではなく AIR ではない場合はアイテムを持ち上げたものとして Map に記録します。
 * → さらに、次のイベント発生時に記録したアイテムと {@link InventoryCreativeEvent#getCursor()} が合致する場合はツールバーからの取得とみなしません。
 * → これにより、一度持ち上げて別のスロットに移動させた場合にツールバーとして判定されなくなります。(#429, #913)
 * ・jaoiumに該当するアイテムは処罰対象としてその後処理されるため、この機能では当該アイテムを削除しません。
 * ・ImageOnMapのマップをマウス中クリックでコピーするとツールバーとして誤認されてしまう問題を回避するため、{@link Material#FILLED_MAP} のアイテムはすべて除外します。(#533)
 * <p>
 * 以上のフローを経て、除外されなかったアイテムはすべてツールバーとして判定し、当該アイテムを削除します。
 * また当該アイテムの情報をデータフォルダの toolbar-items.tsv に記録します。
 */
public class Event_AntiToolbar extends MyMaidLibrary implements Listener, EventPremise {
    final Pattern damagePattern = Pattern.compile("\\{Damage:\\d+}");
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
        ItemStack is = event.getCursor();

        boolean isPickupItem = pickupItems.containsKey(player.getUniqueId()) &&
            pickupItems.get(player.getUniqueId()).equals(is) &&
            (pickupItems.get(player.getUniqueId()).getItemMeta().equals(is.getItemMeta()) ||
                isAllowPlayerHead(pickupItems.get(player.getUniqueId()), is));

        if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
            pickupItems.put(player.getUniqueId(), event.getCurrentItem());
        }

        if (isPickupItem) {
            return;
        }

        if (is.getType() == Material.AIR) {
            return;
        }

        boolean isDeny = isDenyItemStack(event.getCursor());

        if (!isDeny) {
            return;
        }

        if (isExistsInventory(player.getInventory(), is)) {
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
                item.getType() == is.getType() &&
                    item.getItemMeta() != null &&
                    (isAllowPlayerHead(item, is) || item.getItemMeta().equals(is.getItemMeta()))
            );
    }

    @SuppressWarnings("deprecation")
    boolean isAllowPlayerHead(ItemStack is1, ItemStack is2) {
        if (is1.getType() != Material.PLAYER_HEAD || is2.getType() != Material.PLAYER_HEAD) {
            return false;
        }

        PlayerProfile profile1 = ((SkullMeta) is1.getItemMeta()).getOwnerProfile();
        PlayerProfile profile2 = ((SkullMeta) is2.getItemMeta()).getOwnerProfile();

        if (profile1 == null || profile2 == null) {
            return false;
        }

        return (profile1.getName() != null && profile2.getName() != null && profile1.getName().equals(profile2.getName())) ||
            (profile1.getUniqueId() != null && profile2.getUniqueId() != null && profile1.getUniqueId().equals(profile2.getUniqueId())) ||
            profile1.getTextures().equals(profile2.getTextures());
    }
}
