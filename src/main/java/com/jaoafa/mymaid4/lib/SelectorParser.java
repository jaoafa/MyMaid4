package com.jaoafa.mymaid4.lib;

import org.bukkit.entity.EntityType;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * コマンドを実行する際に対象となるエンティティを指定する、すなわちセレクターを判定するクラス
 * MC version 1.16.5
 *
 * @author jojo_
 * かっこいい！
 */
public class SelectorParser extends MyMaidLibrary {
    /*
    @p          最寄りのプレイヤひとり
    @r          ランダムなプレイヤ
    @a          全てのプレイヤ
    @e          全てのエンティティ
    @s          コマンドの実行者
     */
    List<String> selector_list = Arrays.asList("p", "r", "a", "e", "s");
    /*
    x,y,z       座標
    distance    距離
    dx dy dz    角度
    <p>
    scores      スコア
    tag         たぐ
    team        チーム
    <p>
    limit       人数
    sort        limitと同時に使う。主に優先度を設定する
    level       レベル
    gamemode    ゲームモード
    name        なまえ
    x_rotation  地平線に対する偏角、首の傾き
    y_rotation  XZ平面上での回転角、つまり方角
    type        エンティティの種類
    nbt         nbtタグ
    advancements進捗
    predicate   わからんマジでわからん
     */
    List<String> argument_list = Arrays.asList(
        "x", "y", "z",
        "distance",
        "dx", "dy", "dz",
        "scores",
        "tag",
        "team",
        "limit",
        "sort",
        "level",
        "gamemode",
        "name",
        "x_rotation", "y_rotation",
        "type",
        "nbt",
        "advancements",
        "predicate"
    );

    boolean valid = true;
    String selector;
    Map<String, String> args = new HashMap<>();

    public SelectorParser(String SelectorText) throws IllegalArgumentException {
        Pattern p = Pattern.compile("^@(.)(.*)$");
        Matcher m = p.matcher(SelectorText);
        if (!m.find()) {
            valid = false;
            throw new IllegalArgumentException("セレクターテキストがセレクターとして認識できませんでした。");
        }
        selector = m.group(1);
        if (!selector_list.contains(selector)) {
            throw new IllegalArgumentException("セレクターが認識できませんでした。");
        }
        if (m.group(2).equals("[]")) {
            throw new IllegalArgumentException("セレクターの引数が認識できませんでした。");
        }
        p = Pattern.compile("^//[(.+)]$");
        m = p.matcher(m.group(2));
        if (!m.find()) {
            return;
        }
        if (m.group(1).equals("")) {
            throw new IllegalArgumentException("セレクターの引数が認識できませんでした。");
        }
        if (!m.group(1).contains(",")) {
            String arg = m.group();
            if (arg.contains("=")) {
                String[] key_value = arg.split("=");
                String key = key_value[0];
                String value = key_value[1];
                this.args.put(key, value);
            } else {
                throw new IllegalArgumentException("セレクターの一番目の引数が認識できませんでした。");
            }
            return;
        }

        String[] args = m.group(1).split(",");
        int i = 0;
        for (String arg : args) {
            if (arg.contains("=")) {
                String[] key_value = arg.split("=");
                String key = key_value[0];
                String value = key_value[1];
                this.args.put(key, value);
                i++;
            } else {
                throw new IllegalArgumentException("セレクターの" + (i + 1) + "番目の引数が認識できませんでした。");
            }
        }
    }

    /**
     * 引数は適当かどうか調べる
     *
     * @return 引数が適当ならture
     */
    public boolean isValidValues() {
        if (!valid) {
            return false;
        }
        if (args.containsKey("x") && !isInt(args.get("x"))) {
            return false;
        }
        if (args.containsKey("y") && !isInt(args.get("y"))) {
            return false;
        }
        if (args.containsKey("z") && !isInt(args.get("z"))) {
            return false;
        }
        if (args.containsKey("distance") && !isInt(args.get("distance"))) {
            return false;
        }
        if (args.containsKey("type") && !isInt(args.get("type"))) {
            boolean TypeCheck = false;
            for (EntityType type : EntityType.values()) {
                if (!"Player".equalsIgnoreCase(args.get("type"))) {
                    if (type.getName() == null) {
                        continue;
                    }
                    if (type.getName().equalsIgnoreCase(args.get("type"))) {
                        TypeCheck = true;
                    }
                    if (type.getName().equalsIgnoreCase("!" + args.get("type"))) {
                        TypeCheck = true;
                    }
                }
                if (type.getName().equalsIgnoreCase("!player")) {
                    TypeCheck = true;
                }

            }
            return TypeCheck;
        }
        return true;

    }

    /**
     * どの引数が適当でないかを返す
     *
     * @return 適当じゃない引数
     */
    public Set<String> getInvalidValues() {
        Set<String> invalid = new HashSet<>();
        if (!valid) {
            invalid.add("ALL");
        }
        if (args.containsKey("x") && !isInt(args.get("x"))) {
            invalid.add("x");
        }
        if (args.containsKey("y") && !isInt(args.get("y"))) {
            invalid.add("y");
        }
        if (args.containsKey("z") && !isInt(args.get("z"))) {
            invalid.add("z");
        }
        if (args.containsKey("distance") && !isInt(args.get("distance"))) {
            invalid.add("distance");
        }
        if (args.containsKey("type")) {
            boolean TypeCheck = false;
            for (EntityType type : EntityType.values()) {
                if (!"Player".equals(args.get("type"))) {
                    if (type.getName() == null) {
                        continue;
                    }
                    if (type.getName().equalsIgnoreCase(args.get("type"))) {
                        TypeCheck = true;
                    }
                }
            }
            if (!TypeCheck) invalid.add("TYPE:" + args.get("type"));
        }
        return invalid;

    }

    /**
     * セレクタを取得する @つき
     *
     * @return [セレクタ]
     */

    public String getSelector() {
        return "@" + selector;
    }

    /**
     * セレクターの引数を取得する
     *
     * @return セレクターの引数
     */
    public Map<String, String> getArgs() {
        return args;
    }
}
