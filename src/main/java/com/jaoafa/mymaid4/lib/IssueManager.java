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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import okhttp3.*;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.BookMeta;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class IssueManager {
    private static final Path path = Main.getJavaPlugin().getDataFolder().toPath().resolve("issues.json");

    public static CreateIssueResponse addIssue(Player player, Repository repository, BookMeta meta, List<Label> labels) {
        String accessToken = Main.getMyMaidConfig().getGitHubAccessToken();
        if (accessToken == null) {
            throw new IllegalStateException("GitHub Access Token is not set.");
        }

        String url = String.format("https://api.github.com/repos/%s/issues", repository.getRepo());

        String title = meta.getTitle();
        String author = meta.getAuthor();
        List<Component> pages = meta.pages();
        String body = """
            %s
                        
            ## システム追加情報

            - 作成者: [%s (`%s`)](%s)
            - 執筆者: %s
            """.formatted(
            pages.stream()
                .skip(1)
                .map(o -> ChatColor.stripColor(PlainTextComponentSerializer.plainText().serialize(o)))
                .collect(Collectors.joining("\n")),
            player.getName(),
            player.getUniqueId().toString(),
            String.format("https://users.jaoafa.com/%s", player.getUniqueId()),
            author
        );

        JSONObject json = new JSONObject()
            .put("title", title)
            .put("body", body);

        if (labels != null && !labels.isEmpty()) {
            JSONArray labelArray = new JSONArray();
            for (Label label : labels) {
                labelArray.put(label.getLabel());
            }
            json.put("labels", labelArray);
        }

        try {
            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = RequestBody
                .create(json.toString(), MediaType.parse("application/json; charset=UTF-8"));
            Request request = new Request.Builder()
                .url(url)
                .header("Authorization", String.format("token %s", accessToken))
                .post(requestBody)
                .build();
            JSONObject obj;
            int statusCode;
            try (Response response = client.newCall(request).execute()) {
                statusCode = response.code();
                if (statusCode != 201) {
                    return new CreateIssueResponse(statusCode);
                }
                obj = new JSONObject(Objects.requireNonNull(response.body()).string());
            }

            int issueNum = obj.getInt("number");
            addIssueData(player, repository, issueNum);
            return new CreateIssueResponse(statusCode).issueNumber(issueNum);
        } catch (IOException e) {
            MyMaidLibrary.reportError(IssueManager.class, e);
            return new CreateIssueResponse(0).errorMessage(e.getMessage());
        }
    }

    private static void addIssueData(Player player, Repository repository, int issueNumber) {
        try {
            JSONObject json = new JSONObject();
            if (Files.exists(path)) {
                json = new JSONObject(Files.readString(path));
            }
            if (!json.has(player.getUniqueId().toString())) {
                json.put(player.getUniqueId().toString(), new JSONArray());
            }
            JSONArray array = json.getJSONArray(player.getUniqueId().toString());
            array.put(new JSONObject()
                .put("repo", repository.getRepo())
                .put("issue", issueNumber)
                .put("time", System.currentTimeMillis())
            );
            json.put(player.getUniqueId().toString(), array);
            Files.writeString(path, json.toString());
        } catch (IOException e) {
            MyMaidLibrary.reportError(IssueManager.class, e);
        }
    }

    public static void removePlayerIssue(Player player, int issueNumber) {
        try {
            JSONObject json = new JSONObject();
            if (Files.exists(path)) {
                json = new JSONObject(Files.readString(path));
            }
            if (!json.has(player.getUniqueId().toString())) {
                return;
            }
            JSONArray array = json.getJSONArray(player.getUniqueId().toString());
            List<JSONObject> list = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                if (obj.getInt("issue") == issueNumber) {
                    continue;
                }
                list.add(obj);
            }
            json.put(player.getUniqueId().toString(), list);
            Files.writeString(path, json.toString());
        } catch (IOException e) {
            MyMaidLibrary.reportError(IssueManager.class, e);
        }
    }

    public static String getRepoIssueType(Repository repository) {
        switch (repository) {
            case MyMaid4 -> {
                return "不具合報告";
            }
            case jaoMinecraftServer -> {
                return "フィードバック";
            }
        }
        return "UNKNOWN";
    }

    public static List<Issue> getPlayerIssues(Player player) {
        try {
            JSONObject json = new JSONObject();
            if (Files.exists(path)) {
                json = new JSONObject(Files.readString(path));
            }
            if (!json.has(player.getUniqueId().toString())) {
                return new ArrayList<>();
            }
            JSONArray array = json.getJSONArray(player.getUniqueId().toString());
            List<Issue> issues = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Repository repo = Repository.fromRepo(obj.getString("repo"));
                issues.add(new Issue(repo, obj.getInt("issue"), obj.getLong("time")));
            }
            return issues;
        } catch (IOException e) {
            MyMaidLibrary.reportError(IssueManager.class, e);
        }
        return new ArrayList<>();
    }

    public static boolean isClosedIssue(Repository repository, int issueNumber) {
        String accessToken = Main.getMyMaidConfig().getGitHubAccessToken();
        if (accessToken == null) {
            throw new IllegalStateException("GitHub Access Token is not set.");
        }

        String url = String.format("https://api.github.com/repos/%s/issues/%s", repository.getRepo(), issueNumber);

        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                .url(url)
                .header("Authorization", String.format("token %s", accessToken))
                .get()
                .build();
            JSONObject obj;
            int statusCode;
            try (Response response = client.newCall(request).execute()) {
                statusCode = response.code();
                if (statusCode != 200) {
                    return false;
                }
                obj = new JSONObject(Objects.requireNonNull(response.body()).string());
            }

            return obj.getString("state").equalsIgnoreCase("closed");
        } catch (IOException e) {
            MyMaidLibrary.reportError(IssueManager.class, e);
            return false;
        }
    }

    public record Issue(Repository repository, int issueNumber, long time) {
    }

    public static class CreateIssueResponse {
        private final int statusCode;
        private final boolean status;
        private int issueNumber;
        private String errorMessage;

        private CreateIssueResponse(int code) {
            this.statusCode = code;
            this.status = code >= 200 && code < 300;
        }

        public static CreateIssueResponse of(int code) {
            return new CreateIssueResponse(code);
        }

        public CreateIssueResponse issueNumber(int issueNumber) {
            this.issueNumber = issueNumber;
            return this;
        }

        public CreateIssueResponse errorMessage(String message) {
            this.errorMessage = message;
            return this;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public boolean getStatus() {
            return status;
        }

        public int getIssueNumber() {
            return issueNumber;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    public enum Repository {
        MyMaid4("jaoafa/MyMaid4"),
        jaoMinecraftServer("jaoafa/jao-Minecraft-Server"),
        ;

        private final String repo;

        Repository(String repo) {
            this.repo = repo;
        }

        public String getRepo() {
            return repo;
        }

        public static Repository fromRepo(String repo) {
            for (Repository repository : values()) {
                if (repository.getRepo().equals(repo)) {
                    return repository;
                }
            }
            return null;
        }
    }

    public enum Label {
        BUG("\uD83D\uDC1Bbug");

        private final String label;

        Label(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
}
