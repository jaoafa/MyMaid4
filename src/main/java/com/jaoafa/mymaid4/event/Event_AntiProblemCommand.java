package com.jaoafa.mymaid4.event;

import com.jaoafa.mymaid4.lib.EBan;
import com.jaoafa.mymaid4.lib.Jail;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import com.jaoafa.mymaid4.lib.SelectorParser;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class Event_AntiProblemCommand extends MyMaidLibrary implements Listener {
    static Map<String, AntiCommand> antiCommandMap = new HashMap<>();
    static String[] LeastOne = new String[]{"distance", "type", "team", "name"};

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

        if(!func.isPresent()){
            return;
        }

        EBan eban = new EBan(player);
        if (eban.isBanned()){
            event.setCancelled(true);
            return;
        }
        Jail jail = new Jail(player);
        if (jail.isBanned()) {
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
            if (args[1].equalsIgnoreCase("@p") || args[1].equalsIgnoreCase(player.getName())) {
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
                return;
            }
            if (args[1].startsWith("@e") && !MyMaidLibrary.isAMR(player)) {
                //DefaultもしくはVerifiedが実行した場合
                player.chat("最後にキレたのは高2のころかな。オタクだからってウェイ系に絡まれたときw");
                player.chat(
                    "最初は微笑してたんだけど、推しを貶されて気づいたらウェイ系は意識無くなってて、25人くらいに取り押さえられてたw記憶無いけど、ひたすら笑顔で殴ってたらしいw俺ってサイコパスなのかもなww");
                player.chat("(私は\"" + String.join(" ", args) + "\"コマンドを使用しました。)");
                event.setCancelled(true);
                MyMaidLibrary.checkSpam(player);
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
                //MyMaidLibrary.chatFake(ChatColor.GOLD, "jaotan", text);
                player.sendMessage(text);
                player.setHealth(0);
                event.setCancelled(true);
                MyMaidLibrary.checkSpam(player);
                return;
            }
            if (args[1].startsWith("@e")) {
                try {
                    SelectorParser parser = new SelectorParser(args[1]);
                    if (!parser.isValidValues()) {
                        player.sendMessage(String.format("[Command] %s指定されたセレクターは適切でありません。", ChatColor.GREEN));
                        Set<String> invalids = parser.getInvalidValues();
                        player.sendMessage(String.format("[COMMAND] %s不適切だったセレクター引数: %s", ChatColor.GREEN, String.join(", ", invalids)));
                        event.setCancelled(true);
                        MyMaidLibrary.checkSpam(player);
                        return;
                    }
                    if (!parser.getArgs().containsKey("distance")) {
                        boolean exist = false;
                        for (String one : LeastOne) {
                            if (parser.getArgs().containsKey(one)) {
                                exist = true;
                                break;
                            }
                        }
                        if (!exist) {
                            player.sendMessage(String.format("[COMMAND] %s指定されたセレクターは適切でありません。", ChatColor.GREEN));
                            player.sendMessage(String.format("[COMMAND] %s理由: @eセレクターで引数「%s」のいずれかを指定せずに実行することはできません。", ChatColor.GREEN, String.join("」・「", LeastOne)));
                            event.setCancelled(true);
                            MyMaidLibrary.checkSpam(player);
                            return;
                        }
                    } else {
                        player.sendMessage(String.format("[COMMAND] %s指定されたセレクターは適切でありません。", ChatColor.GREEN));
                        player.sendMessage(String.format("[COMMAND] %s理由: @eセレクターで引数「r」を指定せずに実行することはできません。", ChatColor.GREEN));
                        event.setCancelled(true);
                        MyMaidLibrary.checkSpam(player);
                        return;
                    }
                } catch (IllegalArgumentException e) {
                    player.sendMessage(String.format("[COMMAND] %s指定されたセレクターは適切でありません。", ChatColor.GREEN));
                    player.sendMessage(String.format("[COMMAND] %s理由: %s", ChatColor.GREEN, e.getMessage()));
                    event.setCancelled(true);
                    MyMaidLibrary.checkSpam(player);
                    return;
                }
            }
            if (args[1].startsWith("@a")) {
                try {
                    SelectorParser parser = new SelectorParser(args[1]);
                    if (!parser.isValidValues()) {
                        player.sendMessage(String.format("[COMMAND] %s指定されたセレクターは適切でありません。", ChatColor.GREEN));
                        event.setCancelled(true);
                        MyMaidLibrary.checkSpam(player);
                    }
                } catch (IllegalArgumentException e) {
                    player.sendMessage(String.format("[COMMAND] %s指定されたセレクターは適切でありません。", ChatColor.GREEN));
                    player.sendMessage(String.format("[COMMAND] %s理由: %s", ChatColor.GREEN, e.getMessage()));
                    event.setCancelled(true);
                    MyMaidLibrary.checkSpam(player);
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
        }
    }

    static class AntiCmd_WECalc implements AntiCommand {
        @Override
        public void execute(PlayerCommandPreprocessEvent event, Player player, String[] args) {
            player.chat("オ、オオwwwwwwwwオレアタマ良いwwwwwwww最近めっちょ成績あがってんねんオレwwwwwwwwエゴサとかかけるとめっちょ人気やねんwwwwァァァァァァァwwwクソハゲアタマを見下しながら食べるフライドチキンは一段とウメェなァァァァwwwwwwww");
            player.chat("(私は\"" + String.join(" ", args) + "\"コマンドを使用しました。)");
            checkSpam(player);
            event.setCancelled(true);
        }
    }

    static class AntiCmd_WEEval implements AntiCommand {
        @Override
        public void execute(PlayerCommandPreprocessEvent event, Player player, String[] args) {
            player.chat("オ、オオwwwwwwwwオレコマンド実行できるwwwwwwww最近マイクラやってんねんオレwwwwwwwwカスどもをぶちのめしてるねんwwwwァァァァァァァwwwカスに見下されながら食べるフィレオフィッシュは一段とウメェなァァァァwwwwwwww");
            player.chat("(私は\"" + String.join(" ", args) + "\"コマンドを使用しました。)");
            checkSpam(player);
            event.setCancelled(true);
        }
    }

    static class AntiCmd_WGGod implements AntiCommand {
        @Override
        public void execute(PlayerCommandPreprocessEvent event, Player player, String[] args) {
            player.chat("オ、オオwwwwwwwwオレアルファwwwwwwww最近めっちょふぁぼられてんねんオレwwwwwwwwエゴサとかかけるとめっちょ人気やねんwwwwァァァァァァァwwwクソアルファを見下しながら食べるエビフィレオは一段とウメェなァァァァwwwwwwww");
            player.chat("(私は\"" + String.join(" ", args) + "\"コマンドを使用しました。)");
            checkSpam(player);
            event.setCancelled(true);
        }
    }

    static class AntiCmd_PluginCmd implements AntiCommand {
        @Override
        public void execute(PlayerCommandPreprocessEvent event, Player player, String[] args) {
            if (isAMR(player)) {
                return;
            }
            /*
            player.chat("聞いてよアカネチャン！ ん？");
            player.chat("良いこと思いつきました なんや？");
            player.chat("私 有名実況者になります！");
            player.chat("せやな");
            player.chat("せーやっ せーやっ せーやっ せーやっ せーやっ せーやっな。");
            player.chat("今話題のゲームがあるんですよ うん");
            player.chat("面白そうなので私もやってみようと思うんです うん");
            player.chat("まあゆかりさんは天才ですから？ うん？");
            player.chat("敵を華麗にバッタバッタやっつけるわけです うん");
            player.chat("それを生放送したりですね！ うん");
            player.chat("動画アップしてですね！ うん");
            player.chat("その結果ランキングに載るわけです！ うん");
            player.chat("そしてみんなにチヤホヤされてですね！ うん");
            player.chat("ゆかりちゃんカワイイ～！ カッコイイ～！って！！ うん");
            player.chat("言われちゃうんです！！！！ うん");
            player.chat("いや～困っちゃいますね～ うん");
            player.chat("ね！いい考えでしょ！アカネチャン！");
            player.chat("続きは http://www.nicovideo.jp/watch/sm32492001 で！ｗ");
            player.chat("(私は\"" + command + "\"コマンドを使用しました。)");
             */

            player.chat("聞いてよアカネチャン！ ん？");
            player.chat("良いこと思いつきました なんや？");
            player.chat("私 有名実況者になります！");
            player.chat("(私は\"" + String.join(" ", args) + "\"コマンドを使用しました。)");
            checkSpam(player);
            event.setCancelled(true);
        }
    }

    static class AntiCmd_ReloadCmd implements AntiCommand {
        @Override
        public void execute(PlayerCommandPreprocessEvent event, Player player, String[] args) {
            if (isAM(player)) {
                return;
            }
            /*
            player.chat("ﾀｯﾀｯﾀﾀｯﾀｯwwwﾀｯﾀｯﾀｯwww");
            player.chat("ﾀｯﾀｯﾀﾀｯﾀｯﾀｯﾀｯﾀｯﾀｯ三└(┐卍^o^)卍ﾄﾞｩﾙﾙﾙﾙﾄﾞﾄﾞ");
            player.chat("ﾃﾃﾃﾃﾃｰﾃﾃﾃﾃﾃﾃﾃﾃﾃﾃwwwﾃﾃﾃﾃﾃｰﾃﾃﾃﾃﾃﾃｰwwwﾃﾃﾃﾃﾃﾃｰﾃﾃﾃﾃﾃﾃﾃﾃﾃﾃwwwﾃﾃﾃﾃﾃｰﾃﾃ");
            player.chat("ﾄﾞﾄﾞﾝ( ･´ｰ･｀)ﾄﾞﾝ！");
            player.chat("XXホモぉ┌(┌＾o＾)┐");
            player.chat("GGホモぉ┌(┌＾o＾)┐");
            player.chat("(っ'ヮ'c)ﾌｧｧｧｧﾌｧﾌｧﾌｧﾌｧﾌｧｧｧｧｧwww");
            player.chat("ﾎﾞｯｽｺﾞｫｫｫwww ");
            player.chat("XXホモぉ┌(┌＾o＾)┐");
            player.chat("GGホモぉ┌(┌＾o＾)┐");
            player.chat("(っ'ヮ'c)ﾌｧｧｧｧﾌｧﾌｧﾌｧﾌｧﾌｧｽﾞﾝﾁｬｯﾁｬ💃ｽﾞﾝﾁｬｯﾁｬ💃");
            player.chat("XXホモぉ┌(┌＾o＾)┐");
            player.chat("GGホモぉ┌(┌＾o＾)┐");
            player.chat("(っ'ヮ'c)ﾌｧｧｧｧﾌｧﾌｧﾌｧﾌｧﾌｧｧｧｧｧwww");
            player.chat("ﾎﾞｯｽｺﾞｫｫｫwww");
            player.chat("XXホモぉ┌(┌＾o＾)┐");
            player.chat("GGホモぉ┌(┌＾o＾)┐");
            player.chat("(っ'ヮ'c)ﾌｧｧｧｧﾌｧﾌｧﾌｧﾌｧﾌｧｽﾞﾝﾁｬ(ง ˙ω˙)วｽﾞﾝﾁｬ(ง ˙ω˙)ว");
            player.chat("ﾄﾞﾝｯﾄﾞﾝｯ('ω'乂)ｲｶｰﾝwwwﾀﾞｯﾀﾝ⊂二二（ ＾ω＾）二⊃ﾌﾞｰﾝwww");
            player.chat("ﾃﾃﾃﾃﾃｰﾃﾃﾃﾃﾃﾃﾃﾃﾃﾃwwwﾃﾃﾃﾃﾃｰﾃﾃﾃﾃﾃﾃｰwwwﾃﾃﾃﾃﾃｰﾃﾃﾃﾃﾃﾃﾃﾃﾃﾃwwwﾃﾃﾃﾃﾃｰﾃﾃ");
            player.chat("ﾄﾞﾄﾞﾝ( ･´ｰ･｀)ﾄﾞﾝ！");
            player.chat("インド人を右にィ(´･∀･｀)");
            player.chat("インド人を右にィ（ ՞ਊ ՞）☝");
            player.chat("インド人を右にィ(´･∀･｀)");
            player.chat("インド人を右にィ（☝ ՞ਊ ՞）☝");
            player.chat("インド人を右にィ(´･∀･｀)");
            player.chat("インド人を右にィ（☝ ՞ਊ ՞）☝");
            player.chat("インドインドインド人!!");
            player.chat("インド人をﾙﾙﾙﾙﾙyyyyy");
            player.chat("ﾗﾝｯﾃﾝﾝﾃｪﾗﾝｯﾃﾝﾝﾃｪwwwﾗﾝﾗﾝﾗﾝ( ﾟдﾟ)");
            player.chat("ﾗﾝｯﾃﾝﾝﾃｪﾙﾝﾊﾞｶｩwwwﾗﾝｯﾃﾝﾝﾃｪﾙﾝﾊﾞｶｩﾃｼｭｶﾝﾃﾙｩｩｩwwwwww");
            player.chat("ヤブ医者ﾊﾞｽﾀｰヤブ医者ﾊﾞｽﾀｰ(^^)");
            player.chat("ヤブ医者ヤブ医者ヤブ医者ﾊﾞｽﾀｰ(^^)");
            player.chat("ヤブ医者ﾊﾞｽﾀｰヤブ医者ﾊﾞｽﾀｰ(^^)");
            player.chat("ﾊﾞﾊﾞﾊﾞﾊﾞﾌﾞﾌﾞﾌﾞﾌﾞﾍﾞﾍﾞﾍﾞﾍﾞﾊﾞｽﾀｰ(^^)");
            player.chat("全品100円50円引きwww全品100円50円引き");
            player.chat("全品100円50円引きwww全品100円50円引き");
            player.chat("全品100円50円引き");
            player.chat("ﾃﾃﾃﾃ|ω･)وﾞ ㌧㌧ﾄﾞｯﾄﾞｯ三└(┐卍^o^)卍ﾄﾞｩﾙﾙﾙﾙﾄﾞﾄﾞ");
            player.chat("全品100円50円引きwww全品100円50円引き");
            player.chat("┣¨┣¨┣¨┣(꒪ͧд꒪ͧ)┣¨┣¨┣¨┣¨");
            player.chat("┣¨┣¨┣¨┣(꒪ͧд꒪ͧ)┣¨┣¨┣¨┣¨");
            player.chat("ﾄﾞﾄﾞﾄﾞｩﾙﾙ(((卍 ･Θ･)卍ﾄﾞｩﾙﾙﾙﾄﾞﾄﾞﾄﾞｩﾙﾙ(((卍 ･Θ･)卍ﾄﾞｩﾙﾙﾙ三└(┐卍^o^)卍ﾄﾞｩﾙﾙﾙﾙﾄﾞﾝｯ( •̀ω•́ )/");
            player.chat("┌(┌ ・ω・)┐ﾀﾞﾝｯ");
            player.chat("ﾃﾝｯﾃﾝｯﾃﾝｯ!!!!!ﾃﾊﾊﾊｯﾊﾃﾝｯ!!!!!( ﾟдﾟ)ﾊｯ!");
            player.chat("ﾃﾝｯﾃﾝｯﾃﾝｯ!!!!!ﾃﾊﾊﾊｯﾊﾃﾝｯ!!!!!( ﾟдﾟ)ﾊｯ!");
            player.chat("ﾃﾝｯﾃﾝｯﾃﾝｯ!!!!!ﾃﾊﾊﾊｯﾊﾃﾝｯ!!!!!( ﾟдﾟ)ﾊｯ!");
            player.chat("ﾃﾝｯﾃﾝｯﾃﾝｯ!!!!!ﾃﾊﾊﾊｯﾊﾃﾝｯ!!!!!( ﾟдﾟ)ﾊｯ!");
            player.chat("ﾊｯﾊﾃﾝｯ!!!!!ﾃﾝｯ!!!!!ﾃﾝｯ!!ﾃﾝｯ!!ﾃﾝｯ!!( ﾟдﾟ)ﾊｯ!");
            player.chat("ﾃﾝｯ!!!!!ﾃﾝｯ!!!!!ﾃﾊﾊﾊｯﾊﾃﾝｯ!!!!!( ﾟдﾟ)ﾊｯ!");
            player.chat("ﾃﾝｯ!!ﾃﾝｯ!!ﾃﾝｯ!!ﾃﾝｯ!!ﾊｯﾊﾊﾊｯ(ง `▽´)╯ﾊｯﾊｯﾊ!!Oh…(´･∀･｀)");
            player.chat("SEGAのゲームはゲイゲイゲイ!ゲイゲイゲイ!");
            player.chat("SEGAのゲームは( ﾟдﾟ)ﾊｯ!( ﾟдﾟ)ﾊｯ!( ﾟдﾟ)ﾊｯ!");
            player.chat("SEGAのゲームはゲイ!ゲイ!ゲイ!");
            player.chat(
                    "SEGAのゲームは宇宙一ィィィィィィィィィィィィ！！！！！！！ィィィ！！ィィィ！！ィィィ！！ィィィ！！ィィィ！！ィィィ！！ィィィ！！イイイイイイイイイイィィィィィ！！！イイイイイィィ⤵");
            player.chat("(私は\"" + command + "\"コマンドを使用しました。)");
             */

            player.chat("インド人を右にィ(´･∀･｀)");
            player.chat("インド人を右にィ（ ՞ਊ ՞）☝");
            player.chat("インド人を右にィ(´･∀･｀)");
            player.chat("インド人を右にィ（☝ ՞ਊ ՞）☝");
            player.chat("インド人を右にィ(´･∀･｀)");
            player.chat("インド人を右にィ（☝ ՞ਊ ՞）☝");
            player.chat("(私は\"" + String.join(" ", args) + "\"コマンドを使用しました。)");
            checkSpam(player);
            event.setCancelled(true);
        }
    }

    static class AntiCmd_BanCmd implements AntiCommand {
        @Override
        public void execute(PlayerCommandPreprocessEvent event, Player player, String[] args) {
            if (isAM(player)) {
                return;
            }
            player.chat("†エンゲキ†...");
            player.chat(
                "私達の世界は…演劇で溢れています…その劇を演じる者…受け入れて消費する者…全ての者がそれに魅了されます…舞台の上に上がり…世界に自分の価値をはからせましょう…その舞台が…現実のものであるかないかにかかわらず…私達は…私達の役を演じるのです…しかし…それらの役割を無くしてしまったら…私達は一体何者なのでしょう…人々が、善と悪を区別しなくなり…目に見える世界が失われ…舞台の幕が降ろされてしまったら…私達は…本当の自分達であること…それが…生きているということなのでしょうか…魂を…持っているということなのでしょうか……＼キイイイイイイイン！！！！！！！！！／");
            player.chat("(私は\"" + String.join(" ", args) + "\"コマンドを使用しました。)");
            checkSpam(player);
            event.setCancelled(true);
        }
    }

    static class AntiCmd_KickCmd implements AntiCommand {
        @Override
        public void execute(PlayerCommandPreprocessEvent event, Player player, String[] args) {
            if (isAM(player)) {
                return;
            }
            /*
            player.chat("Something is quiet. There is no Gallarhorn in the city and it is a different difference from the headquarters.");
            player.chat("Ah. I wonder if the fighting power of Mars is turned towards the plane.");
            player.chat("Wait a moment like that, but it does not matter!");
            player.chat("You are in a good mood.");
            player.chat("It looks like a sled! Everyone is saved, Takaki was doing my best, I have to work hard!");
            player.chat("Ah.");
            player.chat(
                    "(Yes, everything we've accumulated so far is not a waste, the road will continue as long as we do not stop)");
            player.chat("Ah!");
            player.chat("Headmaster? What are you doing? Headmaster!");
            player.chat("Damn Wow ~ !");
            player.chat("Ho! Ah!");
            player.chat("Ha ha ha · · ·. What is it, you did not have enough time? Fuu....");
            player.chat("Hea...Headmaster... Ah...oh...");
            player.chat("What kind of voice are you calling out? Ride...");
            player.chat("Because... Because...");
            player.chat("I am Orga Itsuka, the headmaster. This is not a problem...");
            player.chat("Something that... for me somehow...");
            player.chat("It is my job to protect the members.");
            player.chat("However!");
            player.chat("Let's go because it is good. Everyone is waiting. in addition···.");
             */

            player.chat("I will not stop, as long as you do not stop, I'll be there before that!");
            player.chat("That's why, Don't you ever stop!");
            player.chat("(私は\"" + String.join(" ", args) + "\"コマンドを使用しました。)");
            player.setHealth(0.0D);
            checkSpam(player);
            event.setCancelled(true);
        }
    }

    static class AntiCmd_VersionCmd implements AntiCommand {
        @Override
        public void execute(PlayerCommandPreprocessEvent event, Player player, String[] args) {
            if (isAMR(player)) {
                return;
            }
            /*
            player.chat("(‘o’) ﾍｲ ﾕﾉｫﾝﾜｲ ﾜｨｱｨﾜｨｬ?");
            player.chat("(‘o’) ｲﾖｯﾊ ｲﾔﾊ ｲﾔﾊ ｲﾔﾊ ｲﾔﾊ ｲﾔﾊ ｲﾔﾊ … ｫﾎﾎｯﾊｰﾎﾎｯﾊｰﾎﾎｯﾊｰﾊﾊﾊﾊﾊﾎﾎ…");
            player.chat("(‘o’) ィ～ッニャッハッハッハッハッハッハッハッハッハッ");
            player.chat("(‘o’) ィ～ニャッハッハッハッハッハッハッハッハッ");
            player.chat("(‘o’) ﾝィ～ッニャッハッハッハッハッハッハッハッハッハッハッ");
            player.chat("(‘o’) オーホホオーホホオーホホホホホホ");
            player.chat("(‘o’) ｲﾖ ｲﾖ ｲﾖ ﾎﾎ ｲﾖ ｲﾖ ｲﾖ ﾎﾎ ｲﾖ ｲﾖ ｲﾖ ﾎﾎ ｵｰﾎﾎ ｵｯﾎﾎ");
            player.chat("(‘o’) ｲﾖ ｲﾖ ｲﾖ ﾎﾎ ｲﾖ ｲﾖ ｲﾖ ﾎﾎ ｲﾖ ｲﾖ ｲﾖ…ｲﾖ…ｲﾖ…ｲﾖ…");
            player.chat("(‘o’) ィ～ニャッハッハッハッハッハハハッハッハハハッハッハッハハハッ(ﾋﾟｩｰﾝ)");
            player.chat("(‘o’) ィ～ニャッハッハッハッハハハッハッハハハハハハッハッハッ(ｳｫｰｱｰ?ﾀﾞｨｬ)");
            player.chat("(‘o’) ィ～ニャッハッハッハッハッハハハッハッハハハッハッハッハハハッ(ﾋﾟｩｰﾝ)");
            player.chat("(‘o’) ィ～ニャッハッハッハッハハハッハッハハハハハハッハッハッ(ﾆｮﾝ)ウォオオオオウ！！！！！！");
            */
            player.chat("(‘o’) ＜ を");
            player.chat("(私は\"" + String.join(" ", args) + "\"コマンドを使用しました。)");
            checkSpam(player);
            event.setCancelled(true);
        }
    }

    static class AntiCmd_StopCmd implements AntiCommand {
        @Override
        public void execute(PlayerCommandPreprocessEvent event, Player player, String[] args) {
            if (isAM(player)) {
                return;
            }
            /*
            player.chat("なんか静かですね。街の中にはギャラルホルンもいないし本部とはえらい違いだ。");
            player.chat("ああ。火星の戦力は軒並み向こうに回してんのかもな。");
            player.chat("まっそんなのもう関係ないですけどね！");
            player.chat("上機嫌だな。");
            player.chat("そりゃそうですよ！みんな助かるし、タカキも頑張ってたし、俺も頑張らないと！");
            player.chat("ああ。");
            player.chat("（そうだ。俺たちが今まで積み上げてきたもんは全部無駄じゃなかった。これからも俺たちが立ち止まらないかぎり道は続く）");
            player.chat("ぐわっ！");
            player.chat("団長？何やってんだよ？団長！");
            player.chat("ぐっ！うおぉ～～！");
            player.chat("うおっ！あっ！");
            player.chat("はぁはぁはぁ・・・。なんだよ、結構当たんじゃねぇか。ふっ・・・。");
            player.chat("だ・・・団長・・・。あっ・・・あぁ・・・。");
            player.chat("なんて声、出してやがる・・・ライドォン。");
            player.chat("だって・・・だって・・・。");
            player.chat("俺は鉄華団団長オルガ・イツカだぞ。こんくれぇなんてこたぁねぇ。");
            player.chat("そんな・・・俺なんかのために・・・。");
            player.chat("団員を守んのは俺の仕事だ。");
            player.chat("でも！");
            player.chat("いいから行くぞ。皆が待ってんだ。それに・・・。");
            */

            player.chat("俺は止まんねぇからよ、お前らが止まんねぇかぎり、その先に俺はいるぞ！");
            player.chat("だからよ、止まるんじゃねぇぞ・・・。");
            player.chat("(私は\"" + String.join(" ", args) + "\"コマンドを使用しました。)");
            player.setHealth(0.0D);
            checkSpam(player);
            event.setCancelled(true);
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
        }
    }

}
