# 貢献する - CONTRIBUTING

MyMaid4 の開発に興味を持っていただいてありがとうございます！  
私たちのプロジェクトにコントリビュートする前に、以下の文書をお読みください。

## Required conditions

MyMaid4 にコントリビュートするには、以下の条件を満たす必要があります。

- [jao Minecraft Server](https://jaoafa.com/) で活動したことがあり、運営方針等を理解していること
- [jao Minecraft Server](https://jaoafa.com/) のサーバルールなどに違反しておらず、各種処罰を行われていないこと

## Development Environment

開発に関する環境情報です。

- IDE: IntelliJ IDEA (お勧め)
- Language: Java 17
- Package Manager: Maven

Eclipse などでも開発できますが、開発のサポートやテストサーバの動作は IntelliJ IDEA のみ対応します。

## Talk Language

原則、コミットのメッセージやプルリクエストのメッセージなどは日本語で記述してください。

## Project board

プロジェクトのタスク管理ボードとして [GitHub の Project 機能](https://github.com/jaoafa/MyMaid4/projects/1) を使用しています。

## How to develop

このプロジェクトでは、以下のプロセスに則り開発を進めます。

- 全ての開発作業は各ユーザーのフォークリポジトリで行います。
- 実施した開発内容は動作テストを行い、期待通りにエラーなく動作することを確認してください。
- 1 つのコマンド・1 つの機能を制作し終え、本番環境に反映しても構わない場合はオリジナルリポジトリである jaoafa/MyMaid4 にプルリクエストを送信してください。
- 送信されたプルリクエストはコードオーナーによってコードをチェックされ、問題がなければマージされます。問題がある場合はプルリクエストのレビュー・コメントにて、その旨を記載しますので応答・修正して下さい。

開発を行う前に、 [MyMaid4 の Wiki の開発者向け記事](https://github.com/jaoafa/MyMaid4/wiki/For-Developers) をご覧ください。  
ここには、MyMaid4 を開発するために必要となるであろう様々な情報がまとめられています。

## Precautions for development

開発にあたり、次の注意事項をご確認ください。

## Folders structure

本プロジェクトの大まかなフォルダ構造は以下の通りです。

![](https://i.imgur.com/3dYcg7R.jpeg)

## Specifications

- 全てのコマンドは [`src/main/java/com/jaoafa/mymaid4/command/Cmd_<CommandName>.java`](src/main/java/com/jaoafa/mymaid4/command)
  に作成される必要があります。
- 全てのイベント駆動の機能は [`src/main/java/com/jaoafa/mymaid4/event/Event_<FuncName>.java`](src/main/java/com/jaoafa/mymaid4/event)
  に作成される必要があります。
- コマンドは コマンドフレームワーク [Incendo/cloud](https://github.com/Incendo/cloud) を使用しています。
- `config.yml` で設定される設定情報は `MyMaidConfig` にあり、 `Main.getMyMaidConfig()` から取得できます。
- 複数のクラスにわたって使用される変数は `MyMaidData` に変数を作成し、 Getter と Setter を使用して管理してください。
- 複数のクラスにわたって多く使用される関数は `MyMaidLibrary` に関数を作成し、Javadoc を書いたうえで `extends MyMaidLibrary` して利用してください。
- データベースは jaoMain と ZakuroHat の二つがありますが、原則 jaoMain
  が使用されます。それぞれ `MyMaidData.getMainMySQLDBManager` `MyMaidData.getZKRHatMySQLDBManager` で取得できます。
- 開発サーバかどうかは `MyMaidConfig.isDevelopmentServer()` で判定できます。`plugins/MyMaid4/this-server-is-development`
  ファイルの存在有無で確認しています。
- 全てのコマンドのパーミッションノードは小文字の `mymaid.<CommandName>` でなければなりません。このパーミッションノードは自動で付与されます。

### General

- コマンド・機能の開発を始める前に、次の作業を実施することを強くお勧めします。
    - **`upstream/master` からブランチを作成するなどを行い、最新の状態から開発する**
    - **[Projects](https://github.com/jaoafa/MyMaid4/projects/1) で、該当する看板があれば `In Progress` に移動する**
    - 該当する Issue の `Assignees` に自分を追加する
- ローカル変数はなにかしらの理由がある場合を除き小文字で始めてください。
- 将来的に追加・修正などを行わなければならない項目がある場合は、 `// TODO <Message>` で TODO を登録してください。

### Command

- 使用しているコマンドフレームワークは [Incendo/cloud](https://github.com/Incendo/cloud) です。
    - ドキュメントは [こちら](https://incendo.github.io/cloud) です。
- 全てのコマンドは [`src/main/java/com/jaoafa/mymaid4/command/Cmd_<CommandName>.java`](src/main/java/com/jaoafa/mymaid4/command)
  に配置され、これらが自動で読み込まれます。
- 同時に、クラス名は `Cmd_<CommandName>` でなければなりません。`<CommandName>` は大文字・小文字を問いません。
- また、ここに配置されるコマンドクラスは `com.jaoafa.mymaid4.lib.CommandPremise` インターフェースを実装する必要があります。（`implements CommandPremise`）
- コマンドを実行したユーザーにメッセージを送る場合は `MyMaidLibrary` にある `SendMessage` メソッドを利用してください。
- コマンドの情報（コマンド名・説明）は `details()` で定義します。
- コマンドの内容は `register()` で定義します。このメソッドは Main クラスの `registerCommand` から呼び出され、コマンドが追加されます。（`plugin.yml` に書く必要がありません）

### Event

- 全てのイベント駆動の機能は [`src/main/java/com/jaoafa/mymaid4/event/Event_<FuncName>.java`](src/main/java/com/jaoafa/mymaid4/event)
  に配置され、これらが自動で読み込まれます。
- 同時に、クラス名は `Event_<FuncName>` でなければなりません。
- `<FuncName>` は自由で構いません。
- また、ここに配置されるコマンドクラスは `org.bukkit.event.Listener` と `com.jaoafa.mymaid4.lib.EventPremise`
  インターフェースを実装する必要があります。（`implements Listener, EventPremise`）
- そのクラス(イベント)が持つ機能の情報は `description()` で定義します。

## Git

### Commit

- 発生しているエラーなどはコミット・プルリクエスト前にすべて修正してください。
-

コミットメッセージは **[CommitLint のルール](https://github.com/conventional-changelog/commitlint/tree/master/%40commitlint/config-conventional#rules)
である以下に沿っていることを期待しますが、必須ではありません。**

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

- 必ずフォークして開発してください
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

## Code Quality

コードの品質や安全性、依存パッケージを管理し一定以上に保つため、以下のサービスを利用しています。

- [CodeQL](https://codeql.github.com/): GitHub によるの静的アプリケーションセキュリティテスト (SAST) です。Push / Pull Request 時にチェックされます。
- [GitHub Security Advisories](https://docs.github.com/ja/code-security/repository-security-advisories/about-github-security-advisories-for-repositories):
  GitHub によるリポジトリセキュリティ脆弱性検知ツールです。
- [Dependabot](https://docs.github.com/ja/code-security/supply-chain-security/managing-vulnerabilities-in-your-projects-dependencies):
  GitHub による依存パッケージ脆弱性管理サービスです。
- [Renovate](https://www.whitesourcesoftware.com/free-developer-tools/renovate/): WhiteSource
  による依存パッケージバージョン管理ツールです。自動でアップデートを収集し、Pull Request を作成します。
- [CodeFactor](https://www.codefactor.io/): 静的コード解析サービスです。Push / Pull Request 時にチェックされます。master
  ブランチのレポートは [こちら](https://www.codefactor.io/repository/github/jaoafa/mymaid4) にあります。
- [Qodana](https://www.jetbrains.com/ja-jp/qodana/): IntelliJ によるコード品質管理ツールです。Push / Pull Request 時にチェックされます。master
  ブランチのレポートは [こちら](https://jaoafa.github.io/MyMaid4/) にあります。

## Other

不明な点は jMS Gamers Club の `#development` チャンネルなどで質問してください。  
プロジェクトにコントリビュートするすべての人々は、[行動規範](CODE_OF_CONDUCT.md) を読み遵守しなければならないことを忘れないでください。
