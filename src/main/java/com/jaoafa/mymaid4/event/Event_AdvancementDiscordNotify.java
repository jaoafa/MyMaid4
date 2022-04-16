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
import net.dv8tion.jda.api.EmbedBuilder;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.*;
import java.util.Locale;
import java.util.regex.Pattern;

public class Event_AdvancementDiscordNotify extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "進捗を達成した際にDiscord#server-chatに日本語進捗名で通知します。";
    }

    @EventHandler
    public void onDone(PlayerAdvancementDoneEvent event) {
        if (MyMaidData.getServerChatChannel() == null) {
            return;
        }
        new BukkitRunnable() {
            public void run() {
                Player player = event.getPlayer();
                Advancement advancement = event.getAdvancement();
                String key = advancement.getKey().getKey().replace("/", ".");
                String namespace = advancement.getKey().getNamespace();

                if (!namespace.equals("minecraft")) {
                    return;
                }

                String lang_key = key.toUpperCase(Locale.ROOT).replaceAll(Pattern.quote("."), "_");
                Advancements item;
                try {
                    item = Advancements.valueOf(lang_key);
                } catch (IllegalArgumentException e) {
                    return;
                }

                EmbedBuilder builder = new EmbedBuilder();
                String url = "https://jaoafa.com/user/{uuid}"
                    .replace("{uuid}", player.getUniqueId().toString());
                String iconUrl = "https://minotar.net/helm/{uuid}/{size}"
                    .replace("{uuid}", player.getUniqueId().toString().replace("-", ""))
                    .replace("{size}", "128");
                builder.setAuthor(player.getName() + " has made the advancement " + item.getTranslated(), url, iconUrl);
                builder.setColor(Color.PINK);

                MyMaidData.getServerChatChannel().sendMessageEmbeds(builder.build()).queue();
            }
        }.runTaskAsynchronously(Main.getJavaPlugin());
    }

    enum Advancements {
        ADVENTURE_ADVENTURING_TIME("冒険の時間"),
        ADVENTURE_ARBALISTIC("クロスボウの達人"),
        ADVENTURE_BULLSEYE("的中"),
        ADVENTURE_HERO_OF_THE_VILLAGE("村の英雄"),
        ADVENTURE_HONEY_BLOCK_SLIDE("べとべとな状況"),
        ADVENTURE_KILL_A_MOB("モンスターハンター"),
        ADVENTURE_KILL_ALL_MOBS("モンスター狩りの達人"),
        ADVENTURE_OL_BETSY("おてんば"),
        ADVENTURE_ROOT("冒険"),
        ADVENTURE_SHOOT_ARROW("狙いを定めて"),
        ADVENTURE_SLEEP_IN_BED("良い夢見てね"),
        ADVENTURE_SNIPER_DUEL("スナイパー対決"),
        ADVENTURE_SUMMON_IRON_GOLEM("お手伝いさん"),
        ADVENTURE_THROW_TRIDENT("もったいぶった一言"),
        ADVENTURE_TOTEM_OF_UNDYING("死を超えて"),
        ADVENTURE_TRADE("良い取引だ！"),
        ADVENTURE_TWO_BIRDS_ONE_ARROW("一石二鳥"),
        ADVENTURE_VERY_VERY_FRIGHTENING("とてもとても恐ろしい"),
        ADVENTURE_VOLUNTARY_EXILE("自主的な亡命"),
        ADVENTURE_WHOS_THE_PILLAGER_NOW("どっちが略奪者？"),
        END_DRAGON_BREATH("口臭に気をつけよう"),
        END_DRAGON_EGG("ザ・ネクストジェネレーション"),
        END_ELYTRA("空はどこまでも高く"),
        END_ENTER_END_GATEWAY("遠方への逃走"),
        END_FIND_END_CITY("ゲームの果ての都市"),
        END_KILL_DRAGON("エンドの解放"),
        END_LEVITATE("ここからの素晴らしい眺め"),
        END_RESPAWN_DRAGON("おしまい…再び…"),
        END_ROOT("ジ・エンド"),
        HUSBANDRY_BALANCED_DIET("バランスの取れた食事"),
        HUSBANDRY_BREED_ALL_ANIMALS("二匹ずつ"),
        HUSBANDRY_BREED_AN_ANIMAL("コウノトリの贈り物"),
        HUSBANDRY_COMPLETE_CATALOGUE("猫大全集"),
        HUSBANDRY_FISHY_BUSINESS("生臭い仕事"),
        HUSBANDRY_NETHERITE_HOE("真面目な献身"),
        HUSBANDRY_PLANT_SEED("種だらけの場所"),
        HUSBANDRY_ROOT("農業"),
        HUSBANDRY_SAFELY_HARVEST_HONEY("秘蜜の晩餐会"),
        HUSBANDRY_SILK_TOUCH_NEST("完全な引越し"),
        HUSBANDRY_TACTICAL_FISHING("戦術的漁業"),
        HUSBANDRY_TAME_AN_ANIMAL("永遠の親友となるだろう"),
        NETHER_ALL_EFFECTS("どうやってここまで？"),
        NETHER_ALL_POTIONS("猛烈なカクテル"),
        NETHER_BREW_POTION("町のお薬屋さん"),
        NETHER_CHARGE_RESPAWN_ANCHOR("人に九生なし"),
        NETHER_CREATE_BEACON("生活のビーコン"),
        NETHER_CREATE_FULL_BEACON("ビーコネーター"),
        NETHER_DISTRACT_PIGLIN("わーいぴかぴか！"),
        NETHER_EXPLORE_NETHER("ホットな観光地"),
        NETHER_FAST_TRAVEL("亜空間バブル"),
        NETHER_FIND_BASTION("兵どもが夢の跡"),
        NETHER_FIND_FORTRESS("恐ろしい要塞"),
        NETHER_GET_WITHER_SKULL("不気味で怖いスケルトン"),
        NETHER_LOOT_BASTION("ブタ戦争"),
        NETHER_NETHERITE_ARMOR("残骸で私を覆って"),
        NETHER_OBTAIN_ANCIENT_DEBRIS("深淵に隠されしもの"),
        NETHER_OBTAIN_BLAZE_ROD("炎の中へ"),
        NETHER_OBTAIN_CRYING_OBSIDIAN("玉ねぎを切っているのは誰？"),
        NETHER_RETURN_TO_SENDER("差出人に返送"),
        NETHER_RIDE_STRIDER("足のついたボート"),
        NETHER_ROOT("ネザー"),
        NETHER_SUMMON_WITHER("荒が丘"),
        NETHER_UNEASY_ALLIANCE("不安な同盟"),
        NETHER_USE_LODESTONE("この道をずっとゆけば"),
        STORY_CURE_ZOMBIE_VILLAGER("ゾンビドクター"),
        STORY_DEFLECT_ARROW("今日はやめておきます"),
        STORY_ENCHANT_ITEM("エンチャントの使い手"),
        STORY_ENTER_THE_END("おしまい？"),
        STORY_ENTER_THE_NETHER("さらなる深みへ"),
        STORY_FOLLOW_ENDER_EYE("アイ・スパイ"),
        STORY_FORM_OBSIDIAN("アイス・バケツ・チャレンジ"),
        STORY_IRON_TOOLS("鉄のツルハシで決まり"),
        STORY_LAVA_BUCKET("ホットスタッフ"),
        STORY_MINE_DIAMOND("ダイヤモンド！"),
        STORY_MINE_STONE("石器時代"),
        STORY_OBTAIN_ARMOR("装備せよ"),
        STORY_ROOT("Minecraft"),
        STORY_SHINY_GEAR("ダイヤモンドで私を覆って"),
        STORY_SMELT_IRON("金属を手に入れる"),
        STORY_UPGRADE_TOOLS("アップグレード");

        final String translated;

        Advancements(String translated) {
            this.translated = translated;
        }

        public String getTranslated() {
            return translated;
        }
    }
}
