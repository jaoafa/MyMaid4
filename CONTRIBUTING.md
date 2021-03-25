# Contributing

開発に参加する方は必ずこの文書を読んでください

## Development Environment

開発に関する環境情報です。

- IDE: IntelliJ IDEA (お勧め)
- Language: Java (8)
- Package Manager: Maven

Eclipse などでも開発できますが、開発のサポートやテストサーバの動作は IntelliJ IDEA のみ対応します。

## Talk Language

原則、コミットのメッセージやプルリクエストのメッセージなどは日本語で記述してください。

## Development Process

開発に参加する手順は次のとおりです。

1. `jaoafa/MyMaid4` の `master` ブランチを自分のユーザーへフォークする
2. コードを書く (後述する [Precautions for development](#precautions-for-development) もお読みください)
3. 期待通りに動作するかどうかテストする
4. コミットする
5. プッシュする
6. `jaoafa/MyMaid4` プルリクエストを送信する

プログラミングを始める前に、`jaoafa/MyMaid4` (一般的に `upstream` と呼ばれます) からリベースプルを実施することを強くお勧めします。(これをしないとコンフリクトする場合があります)

`upstream` の登録は以下の方法で行えます。

![](https://i.imgur.com/w1CValK.png)

1. プロジェクトを右クリックし、 `Git` -> `リモート管理` と進む
2. `+` を押し、名前に `upstream` 、 URL に `https://github.com/jaoafa/MyMaid4.git` と打ち込む
3. `OK` を押し登録する

リベースプルは以下の方法で行えます。

![](https://i.imgur.com/MqCXMrq.png)

1. 右下のブランチ名が表示されている欄をクリックする
2. 表示されるウィンドウの右上 🔄(フェッチボタン) を押し、最新の情報に更新する
3. `upstream/master` をクリックし、「リベースを使用して現在のブランチにプル」を押す
4. 成功したことが表示されれば完了

## Test Server

MyMaid4 の動作試験を行うにあたり、サーバが必要になります。用意されているテスト環境は以下の二つです。

- Local Test Server
- ZakuroHat Plugin Test Server

### Local Test Server

開発者のローカル PC にて動作するサーバです。 PaperMC の環境構築などが必要です。強くお勧めします。

環境構築・初期設定は以下の手順にて行ってください。

1. [PaperMC のダウンロードページ](https://papermc.io/downloads) から `1.16.5` の最新のビルドをダウンロードする
2. ダウンロードした jar ファイルを `server` ディレクトリに配置し、 `paper-1.16.5.jar` とリネームする
3. IntelliJ を開き、ウィンドウ右上「実行」で `ReBuild and Reload` を実行する
4. [Minecraft EULA](https://account.mojang.com/documents/minecraft_eula) を読み同意する場合は `eula.txt` の `eula=false` を `eula=true` に変える
5. IntelliJ からプラグインをビルドした後に自動的にリロードするために、[fnetworks/mcrconapi v1.1.1](https://github.com/fnetworks/mcrconapi/releases/tag/v1.1.1) の `mcrconapi-1.1.1.jar` をダウンロード、 `server/mcrconapi-1.1.1.jar` ディレクトリに移動する
6. IntelliJ を開き、ウィンドウ右上「実行」で `ReBuild and Reload` を実行する
7. 表示されるターミナルで `op <PlayerName>` を実行し OP 権限を自身に付与する

実際のテストは以下の手順にて行えます。

1. 動作するコードを書く (ビルドできないと動作しません)
2. 右上「実行」で `ReBuild And Reload` を選ぶ (開発端末に応じて Win と Mac を選んでください)
3. 実行ボタン(`▶`)を押す

これにより、ビルドしたのち

- 既にテストサーバが起動している場合はサーバのリロード
- まだテストサーバが起動していない場合はサーバの起動

が行われます。実施後、 `localhost` でローカルサーバにログインできます。

### ZakuroHat Plugin Test Server

ZakuroHat 上の Docker に立てられている PaperMC 環境で動作するテストサーバです。アドレスは jMS Gamers Club#development のピン止めに記載されています。  
ホワイトリストにて運用されています。参加するには別で規定する条件を満たした上で Tomachi に問い合わせる必要があります。

データベースを使ったコマンド・機能のテストにお使いください。

実際のテストは以下の手順にて行えます。

1. 動作するコードを書く (ビルドできないと動作しません)
2. コミット・プッシュする (プルリクエストはこの時点では不要)
3. テストサーバに入り、 `/loadplugin <User> <Repo> <Branch>` を入力する
4. ビルドが完了し、サーバがリロードされるまで待ってください。
5. リロードが完了すればテストが実施できます。

`/loadplugin` については以下の説明をお読みください。

- リポジトリは `jaoafa/MyMaid4` のフォークリポジトリである必要があります。
- `<User>` にはあなたの GitHub ユーザー名を入力してください。(e.g. `book000`)
- `<Repo>` にはリポジトリ名を入力してください。(e.g. `MyMaid4`)
- `<Branch>` にはあなたが作業しているブランチを入力してください。 (e.g. `master`)
- それぞれのパラメーターはデフォルト値が設定されています。`/loadplugin` のみ実行した場合、 `jaoafa` の `MyMaid4` にある `master` ブランチのコードがクローンされます。
- クローンするブランチはパブリックである必要があります。

## Precautions for development

開発にあたり、次の注意事項をご確認ください。

### General

- 将来的に追加・修正などを行わなければならない項目がある場合は、 `// TODO <Message>` で TODO を登録してください。
- `config.yml` で設定される設定情報は `MyMaidConfig` にあり、 `Main.getMyMaidConfig()` から取得できます。
- 複数のクラスにわたって使用される変数は `MyMaidData` に変数を作成し、 Getter と Setter を使用して管理してください。
- 複数のクラスにわたって多く使用される関数は `MyMaidLibrary` に関数を作成し、Javadoc を書いたうえで `extends MyMaidLibrary` して利用してください。
- データベースは jaoMain と ZakuroHat の二つがありますが、原則 jaoMain が使用されます。それぞれ `MyMaidData.getMainMySQLDBManager` `MyMaidData.getZKRHatMySQLDBManager` で取得できます。
- 開発サーバかどうかは `MyMaidConfig.isDevelopmentServer()` で判定できます。`plugins/MyMaid4/this-server-is-development` ファイルの存在有無で確認しています。

### Command

- 使用しているコマンドフレームワークは [Incendo/cloud](https://github.com/Incendo/cloud) です。
  - ドキュメントは [こちら](https://incendo.github.io/cloud) です。
- 全てのコマンドは [`src/main/java/com/jaoafa/mymaid4/command/Cmd_<CommandName>.java`](src/main/java/com/jaoafa/mymaid4/command) に配置され、これらが自動で読み込まれます。
- 同時に、クラス名は `Cmd_<CommandName>` でなければなりません。`<CommandName>` は大文字・小文字を問いません。
- また、ここに配置されるコマンドクラスは CommandPremise インターフェースを実装する必要があります。（`implements CommandPremise`）
- コマンドの情報（コマンド名・説明）は `details()` で定義します。
- コマンドの内容は `register()` で定義します。このメソッドは Main クラスの `registerCommand` から呼び出され、コマンドが追加されます。（`plugin.yml` に書く必要がありません）
- 全てのコマンドのパーミッションは小文字の `mymaid.<CommandName>` でなければなりません。
- コマンドを実行したユーザーにメッセージを送る場合は `MyMaidLibrary` にある `SendMessage` メソッドを利用してください。

### Event

- 全てのイベント駆動の機能は [`src/main/java/com/jaoafa/mymaid4/event/Event_<FuncName>.java`](src/main/java/com/jaoafa/mymaid4/event) に配置され、これらが自動で読み込まれます。
- 同時に、クラス名は `Event_<FuncName>` でなければなりません。
- `<FuncName>` は自由で構いません

## Git

### Commit

- コミットメッセージは **[CommitLint のルール](https://github.com/conventional-changelog/commitlint/tree/master/%40commitlint/config-conventional#rules) である以下に沿っていることを期待しますが、必須ではありません。**
- 次の形式でコミットメッセージを指定してください: `type(scope): subject` (e.g. `fix(home): message`)
  - `type`, `subject` は必須、 `scope` は必須ではありません
- `type-enum`: `type` は必ず次のいずれかにしなければなりません
  - `build`: ビルド関連
  - `ci`: CI 関連
  - `chore`: いろいろ
  - `docs`: ドキュメント関連
  - `feat`: 新機能
  - `fix`: 修正
  - `perf`: パフォーマンス改善
  - `refactor`: リファクタリング
  - `revert`: コミットの取り消し
  - `style`: コードスタイルの修正
  - `test`: テストコミット
- `type-case`: `type` は必ず小文字でなければなりません (NG: `FIX` / OK: `fix`)
- `type-empty`: `type` は必ず含めなければなりません (NG: `test message` / OK: `test: message`)
- `scope-case`: `scope` は必ず小文字でなければなりません (NG: `fix(HOME): message` / OK: `fix:(home): message`)
- `subject-case`: `subject` は必ず次のいずれかの書式でなければなりません `sentence-case`, `start-case`, `pascal-case`, `upper-case`
- `subject-empty`: `subject` は必ず含めなければなりません (NG: `fix:` / OK: `fix: message`)
- `subject-full-stop`: `subject` は `.` 以外で終えてください (NG: `fix: message.` / OK: `fix: message`)

#### Branch rule

- 基本的にはフォークして開発してください
- 必要がある場合、ブランチは機能追加・修正などに応じて分けて作成してください
- ブランチ名は機能追加・修正の内容を示す言葉で構成してください（例: `add-test-command`, `fix-test-command-api-url`）
- master ブランチへの直接コミットはできません
- 全てのコード追加はプルリクエストを必要とします
- Tomachi に限りセルフマージを可能とします
- レビューはほぼすべてを Tomachi が行います

### Publish

master ブランチ = メインサーバ導入ソースコード です。

- コミットされると、GitHub Actions によってビルドが実施されます。失敗した場合、jMS Gamers Club `#github-notice` に通知が飛びます。
- コミットされると、メインサーバでビルドされビルドに成功すれば本番環境の`MinecraftServerDir/plugins/update/`にビルド成果物が配置されます。再起動時に自動的にアップデートされます。
- バージョン表記は本番環境でのビルド処理によって、`yyyy.mm.dd_hh.mm_最終コミットsha8桁`に変更されます。

## Other

- 不明な点は jMS Gamers Club#development で質問してください。
