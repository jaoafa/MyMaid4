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
import com.jaoafa.mymaid4.lib.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Event_AntiProblemCommand extends MyMaidLibrary implements Listener, EventPremise {
    static final Map<String, AntiCommand> antiCommandMap = new HashMap<>();

    static {
        antiCommandMap.put("/kill", new AntiCmd_Kill());
        antiCommandMap.put("/minecraft:kill", new AntiCmd_Kill());
        antiCommandMap.put("/pex", new AntiCmd_PexPromote());
        antiCommandMap.put("//calc", new AntiCmd_WECalc());
        antiCommandMap.put("/worldedit:/calc", new AntiCmd_WECalc());
        antiCommandMap.put("//eval", new AntiCmd_WEEval());
        antiCommandMap.put("/worldedit:/eval", new AntiCmd_WEEval());
        antiCommandMap.put("/god", new AntiCmd_WGGod());
        antiCommandMap.put("/worldguard:god", new AntiCmd_WGGod());
        antiCommandMap.put("/pl", new AntiCmd_PluginCmd());
        antiCommandMap.put("/bukkit:pl", new AntiCmd_PluginCmd());
        antiCommandMap.put("/plugins", new AntiCmd_PluginCmd());
        antiCommandMap.put("/bukkit:plugins", new AntiCmd_PluginCmd());
        antiCommandMap.put("/rl", new AntiCmd_ReloadCmd());
        antiCommandMap.put("/bukkit:rl", new AntiCmd_ReloadCmd());
        antiCommandMap.put("/reload", new AntiCmd_ReloadCmd());
        antiCommandMap.put("/bukkit:reload", new AntiCmd_ReloadCmd());
        antiCommandMap.put("/ban", new AntiCmd_BanCmd());
        antiCommandMap.put("/bukkit:ban", new AntiCmd_BanCmd());
        antiCommandMap.put("/mcbans:ban", new AntiCmd_BanCmd());
        antiCommandMap.put("/kick", new AntiCmd_KickCmd());
        antiCommandMap.put("/bukkit:kick", new AntiCmd_KickCmd());
        antiCommandMap.put("/mcbans:kick", new AntiCmd_KickCmd());
        antiCommandMap.put("/ver", new AntiCmd_VersionCmd());
        antiCommandMap.put("/bukkit:ver", new AntiCmd_VersionCmd());
        antiCommandMap.put("/version", new AntiCmd_VersionCmd());
        antiCommandMap.put("/bukkit:version", new AntiCmd_VersionCmd());
        antiCommandMap.put("/stop", new AntiCmd_StopCmd());
        antiCommandMap.put("/bukkit:stop", new AntiCmd_StopCmd());
        antiCommandMap.put("/minecraft:stop", new AntiCmd_StopCmd());
        antiCommandMap.put("/advancement", new AntiCmd_Advancement());
        antiCommandMap.put("/minecraft:advancement", new AntiCmd_Advancement());
        antiCommandMap.put("/login", new AntiCmd_Login());
    }

    static void autoHistoryAdd(Player player, String prefix, String details) {
        if (isAMRV(player)) {
            return;
        }
        Historyjao.getHistoryjao(player).autoAdd(prefix, details);
    }

    @Override
    public String description() {
        return "迷惑コマンドの制限を行います。";
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage();
        Player player = event.getPlayer();
        if (command.length() == 0) {
            return;
        }
        String[] args = command.split(" ");
        Optional<Map.Entry<String, AntiCommand>> func = antiCommandMap.entrySet().stream().filter(cmd -> cmd.getKey().equalsIgnoreCase(args[0])).findFirst();

        if (func.isEmpty()) {
            return;
        }

        EBan eban = EBan.getInstance(player);
        if (eban.isStatus()) {
            event.setCancelled(true);
            return;
        }
        Jail jail = Jail.getInstance(player);
        if (jail.isStatus()) {
            event.setCancelled(true);
            return;
        }
        func.get().getValue().execute(event, player, args);
    }

    interface AntiCommand {
        void execute(PlayerCommandPreprocessEvent event, Player player, String[] args);
    }

    static class AntiCmd_Kill implements AntiCommand {
        @Override
        public void execute(PlayerCommandPreprocessEvent event, Player player, String[] args) {
            if (args.length == 1) {
                return;
            }
            if (args[1].equalsIgnoreCase("@p") || args[1].equalsIgnoreCase("@s") || args[1].equalsIgnoreCase(player.getName())) {
                player.setHealth(0D);
                event.setCancelled(true);
                return;
            }

            if (args[1].equalsIgnoreCase("@e")) {
                player.chat("キリトかなーやっぱりww");
                player.chat("自分は思わないんだけど周りにキリトに似てるってよく言われるwww");
                player.chat("こないだDQNに絡まれた時も気が付いたら意識無くて周りに人が血だらけで倒れてたしなwww");
                player.chat("ちなみに彼女もアスナに似てる(聞いてないw)");
                player.chat("(私は\"" + String.join(" ", args) + "\"コマンドを使用しました。)");
                event.setCancelled(true);
                MyMaidLibrary.checkSpam(player);
                autoHistoryAdd(player, "killコマンドの実行", "(" + String.join(" ", args) + ")");
                return;
            }
            if (args[1].equalsIgnoreCase("@a")) {
                player.chat("キリトかなーやっぱw");
                player.chat("一応オタクだけど彼女いるし、俺って退けない性格だしそこら辺とかめっちゃ似てるって言われる()");
                player.chat("握力も31キロあってクラスの女子にたかられる←彼女いるからやめろ！笑");
                player.chat("俺、これでも中1ですよ？");
                player.chat("(私は\"" + String.join(" ", args) + "\"コマンドを使用しました。)");
                event.setCancelled(true);
                MyMaidLibrary.checkSpam(player);
                autoHistoryAdd(player, "killコマンドの実行", "(" + String.join(" ", args) + ")");
                return;
            }
            if (args[1].startsWith("@e") && !MyMaidLibrary.isAMR(player)) {
                // DefaultもしくはVerifiedが実行した場合
                player.chat("最後にキレたのは高2のころかな。オタクだからってウェイ系に絡まれたときw");
                player.chat(
                    "最初は微笑してたんだけど、推しを貶されて気づいたらウェイ系は意識無くなってて、25人くらいに取り押さえられてたw記憶無いけど、ひたすら笑顔で殴ってたらしいw俺ってサイコパスなのかもなww");
                player.chat("(私は\"" + String.join(" ", args) + "\"コマンドを使用しました。)");
                event.setCancelled(true);
                MyMaidLibrary.checkSpam(player);
                autoHistoryAdd(player, "killコマンドの実行", "(" + String.join(" ", args) + ")");
                return;
            }
            if (!MyMaidLibrary.isAMR(player)) {
                if (player.getName().equalsIgnoreCase(args[1])) {
                    event.setCancelled(true);
                    return;
                }
                String text = args[0].equalsIgnoreCase("/kill")
                    ? String.format("%sさんが%sを殺すとか調子に乗ってると思うので%sさんを殺しておきますね^^", player.getName(), args[1], player.getName())
                    : String.format("%sごときが%sを殺そうだなんて図が高いわ！ %sが死にな！", player.getName(), args[1], player.getName());
                chatFake(NamedTextColor.GOLD, "jaotan", text);
                player.setHealth(0);
                event.setCancelled(true);
                MyMaidLibrary.checkSpam(player);
                autoHistoryAdd(player, "killコマンドの実行", "(" + String.join(" ", args) + ")");
                return;
            }
            if (args[1].startsWith("@e")) {
                try {
                    List<Entity> entities = Bukkit.selectEntities(player, args[1]);
                    if (entities.size() > 10) {
                        player.sendMessage(Component.text().append(
                            Component.text("[KILL] "),
                            Component.text("ターゲットとなるエンティティ数が10以内ではないため、このコマンドを実行できません。(ターゲットエンティティ数: " + entities.size() + ")", NamedTextColor.GREEN)
                        ));
                        event.setCancelled(true);
                    }
                } catch (IllegalArgumentException e) {
                    player.sendMessage(Component.text().append(
                        Component.text("[KILL] "),
                        Component.text("ターゲットセレクターが正しくないため、このコマンドを実行できません。", NamedTextColor.GREEN)
                    ));
                    event.setCancelled(true);
                }
            }
        }
    }

    static class AntiCmd_PexPromote implements AntiCommand {
        @Override
        public void execute(PlayerCommandPreprocessEvent event, Player player, String[] args) {
            player.chat("(◞‸◟) ｻﾊﾞｵﾁﾅｲｰﾅ? ﾎﾜｯｳｳﾞｼﾞｸｼﾞｸﾞｨﾝﾉﾝﾞﾝﾞﾝﾞﾝﾞﾍﾟﾗﾚｸﾞｼﾞｭﾁﾞ…ﾇﾇﾉｮｩﾂﾋﾞｮﾝﾇｽﾞｨｺｹｰｯﾝｦｯ…ｶﾅｼﾐ…");
            player.chat("(私は\"" + String.join(" ", args) + "\"コマンドを使用しました。)");
            checkSpam(player);
            event.setCancelled(true);
            autoHistoryAdd(player, "pex promoteコマンドの実行", "(" + String.join(" ", args) + ")");
        }
    }

    static class AntiCmd_WECalc implements AntiCommand {
        @Override
        public void execute(PlayerCommandPreprocessEvent event, Player player, String[] args) {
            player.chat("オ、オオwwwwwwwwオレアタマ良いwwwwwwww最近めっちょ成績あがってんねんオレwwwwwwwwエゴサとかかけるとめっちょ人気やねんwwwwァァァァァァァwwwクソハゲアタマを見下しながら食べるフライドチキンは一段とウメェなァァァァwwwwwwww");
            player.chat("(私は\"" + String.join(" ", args) + "\"コマンドを使用しました。)");
            checkSpam(player);
            event.setCancelled(true);
            autoHistoryAdd(player, "calcコマンドの実行", "(" + String.join(" ", args) + ")");
        }
    }

    static class AntiCmd_WEEval implements AntiCommand {
        @Override
        public void execute(PlayerCommandPreprocessEvent event, Player player, String[] args) {
            player.chat("オ、オオwwwwwwwwオレコマンド実行できるwwwwwwww最近マイクラやってんねんオレwwwwwwwwカスどもをぶちのめしてるねんwwwwァァァァァァァwwwカスに見下されながら食べるフィレオフィッシュは一段とウメェなァァァァwwwwwwww");
            player.chat("(私は\"" + String.join(" ", args) + "\"コマンドを使用しました。)");
            checkSpam(player);
            event.setCancelled(true);
            autoHistoryAdd(player, "evalコマンドの実行", "(" + String.join(" ", args) + ")");
        }
    }

    static class AntiCmd_WGGod implements AntiCommand {
        @Override
        public void execute(PlayerCommandPreprocessEvent event, Player player, String[] args) {
            player.chat("オ、オオwwwwwwwwオレアルファwwwwwwww最近めっちょふぁぼられてんねんオレwwwwwwwwエゴサとかかけるとめっちょ人気やねんwwwwァァァァァァァwwwクソアルファを見下しながら食べるエビフィレオは一段とウメェなァァァァwwwwwwww");
            player.chat("(私は\"" + String.join(" ", args) + "\"コマンドを使用しました。)");
            checkSpam(player);
            event.setCancelled(true);
            autoHistoryAdd(player, "godコマンドの実行", "(" + String.join(" ", args) + ")");
        }
    }

    static class AntiCmd_PluginCmd implements AntiCommand {
        @Override
        public void execute(PlayerCommandPreprocessEvent event, Player player, String[] args) {
            if (isAMR(player)) {
                return;
            }

            player.chat("聞いてよアカネチャン！ ん？");
            player.chat("良いこと思いつきました なんや？");
            player.chat("私 有名実況者になります！");
            player.chat("(私は\"" + String.join(" ", args) + "\"コマンドを使用しました。)");
            checkSpam(player);
            event.setCancelled(true);
            autoHistoryAdd(player, "pluginsコマンドの実行", "(" + String.join(" ", args) + ")");
        }
    }

    static class AntiCmd_ReloadCmd implements AntiCommand {
        @Override
        public void execute(PlayerCommandPreprocessEvent event, Player player, String[] args) {
            if (isAM(player)) {
                return;
            }

            player.chat("インド人を右にィ(´･∀･｀)");
            player.chat("インド人を右にィ（ ՞ਊ ՞）☝");
            player.chat("インド人を右にィ(´･∀･｀)");
            player.chat("インド人を右にィ（☝ ՞ਊ ՞）☝");
            player.chat("インド人を右にィ(´･∀･｀)");
            player.chat("インド人を右にィ（☝ ՞ਊ ՞）☝");
            player.chat("(私は\"" + String.join(" ", args) + "\"コマンドを使用しました。)");
            checkSpam(player);
            event.setCancelled(true);
            autoHistoryAdd(player, "reloadコマンドの実行", "(" + String.join(" ", args) + ")");
        }
    }

    static class AntiCmd_BanCmd implements AntiCommand {
        @Override
        public void execute(PlayerCommandPreprocessEvent event, Player player, String[] args) {
            if (args.length >= 2 &&
                (args[1].equalsIgnoreCase(player.getName()) ||
                    args[1].equalsIgnoreCase("me") ||
                    args[1].equalsIgnoreCase("@p") ||
                    args[1].equalsIgnoreCase("@s"))) {
                // 自分で自分をBan
                player.kick(Component.translatable("multiplayer.disconnect.banned"));
                event.setCancelled(true);
                return;
            }

            if (isAM(player)) {
                return;
            }

            player.chat("†エンゲキ†...");
            player.chat(
                "私達の世界は…演劇で溢れています…その劇を演じる者…受け入れて消費する者…全ての者がそれに魅了されます…舞台の上に上がり…世界に自分の価値をはからせましょう…その舞台が…現実のものであるかないかにかかわらず…私達は…私達の役を演じるのです…しかし…それらの役割を無くしてしまったら…私達は一体何者なのでしょう…人々が、善と悪を区別しなくなり…目に見える世界が失われ…舞台の幕が降ろされてしまったら…私達は…本当の自分達であること…それが…生きているということなのでしょうか…魂を…持っているということなのでしょうか……＼キイイイイイイイン！！！！！！！！！／");
            player.chat("(私は\"" + String.join(" ", args) + "\"コマンドを使用しました。)");
            checkSpam(player);
            event.setCancelled(true);
            autoHistoryAdd(player, "banコマンドの実行", "(" + String.join(" ", args) + ")");
        }
    }

    static class AntiCmd_KickCmd implements AntiCommand {
        @Override
        public void execute(PlayerCommandPreprocessEvent event, Player player, String[] args) {
            Main.getMyMaidLogger().info("args.length: " + args.length);
            if (args.length >= 2 &&
                (args[1].equalsIgnoreCase(player.getName()) ||
                    args[1].equalsIgnoreCase("me") ||
                    args[1].equalsIgnoreCase("@p") ||
                    args[1].equalsIgnoreCase("@s"))) {
                // 自分で自分をキック
                player.kick(Component.translatable("multiplayer.disconnect.kicked"));
                event.setCancelled(true);
                return;
            }

            if (isAM(player)) {
                return;
            }

            player.chat("I will not stop, as long as you do not stop, I'll be there before that!");
            player.chat("That's why, Don't you ever stop!");
            player.chat("(私は\"" + String.join(" ", args) + "\"コマンドを使用しました。)");
            player.setHealth(0.0D);
            checkSpam(player);
            event.setCancelled(true);
            autoHistoryAdd(player, "kickコマンドの実行", "(" + String.join(" ", args) + ")");
        }
    }

    static class AntiCmd_VersionCmd implements AntiCommand {
        @Override
        public void execute(PlayerCommandPreprocessEvent event, Player player, String[] args) {
            if (isAMR(player)) {
                return;
            }
            player.chat("(‘o’) ＜ を");
            player.chat("(私は\"" + String.join(" ", args) + "\"コマンドを使用しました。)");
            checkSpam(player);
            event.setCancelled(true);
            autoHistoryAdd(player, "versionコマンドの実行", "(" + String.join(" ", args) + ")");
        }
    }

    static class AntiCmd_StopCmd implements AntiCommand {
        @Override
        public void execute(PlayerCommandPreprocessEvent event, Player player, String[] args) {
            if (isAM(player)) {
                return;
            }

            player.chat("俺は止まんねぇからよ、お前らが止まんねぇかぎり、その先に俺はいるぞ！");
            player.chat("だからよ、止まるんじゃねぇぞ・・・。");
            player.chat("(私は\"" + String.join(" ", args) + "\"コマンドを使用しました。)");
            checkSpam(player);

            player.kick(Component.translatable("multiplayer.disconnect.server_shutdown"));
            event.setCancelled(true);
            autoHistoryAdd(player, "stopコマンドの実行", "(" + String.join(" ", args) + ")");
        }
    }

    static class AntiCmd_Advancement implements AntiCommand {
        @Override
        public void execute(PlayerCommandPreprocessEvent event, Player player, String[] args) {
            if (isAMR(player)) {
                return;
            }

            player.chat("僕ね、受験期のときに、眞子さま、あの、あれなんすよ、あのー、待ち受けにしていたんですよ。");
            player.chat("マジでショックです！");
            player.chat("まぁでも幸せなら……OKです！");
            player.chat("(私は\"" + String.join(" ", args) + "\"コマンドを使用しました。)");
            checkSpam(player);
            event.setCancelled(true);
            autoHistoryAdd(player, "advancementコマンドの実行", "(" + String.join(" ", args) + ")");
        }
    }

    static class AntiCmd_Login implements AntiCommand {
        @Override
        public void execute(PlayerCommandPreprocessEvent event, Player player, String[] args) {
            String command = event.getMessage();
            EBan eban = EBan.getInstance(player);
            eban.addBan("jaotan", String.format("コマンド「%s」を実行したことにより、サーバルールへの違反の可能性を検知したため", command));
            player.kick(Component.text("Disconnected."));
            if (MyMaidData.getJaotanChannel() != null) {
                MyMaidData.getJaotanChannel().sendMessage(String.format("プレイヤー「%s」がコマンド「%s」を実行したため、キックしました。", player.getName(), command)).queue();
            } else {
                Main.getMyMaidLogger().info("MyMaidData.getJaotanChannel is null");
            }

            event.setCancelled(true);
            autoHistoryAdd(player, "loginコマンドの実行", "(" + String.join(" ", args) + ")");
        }
    }
}
