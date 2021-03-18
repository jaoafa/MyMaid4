# MyMaid4

このプロジェクトは、[jao Minecraft Server](https://jaoafa.com) で導入されている PaperMC プラグイン「MyMaid4」のソースコード公開場所です。

[jaoafa/MyMaid](https://github.com/jaoafa/MyMaid)・[jaoafa/MyMaid2](https://github.com/jaoafa/MyMaid2)・[jaoafa/MyMaid3](https://github.com/jaoafa/MyMaid3)の後継です。

## FAQ

### MyMaidとはなんですか？

MyMaidとは、jao Minecraft Serverにおける独自のプラグインで特にどれに特化したといった制約を持たせずに総合プラグイン的に製作しているものです。  
`わたしたちの「めいど」をここに。` をキャッチフレーズとし、メイドさんのようにMinecraftを楽しく出来るような補助的役割を基本として制作しています。

### 開発者は誰ですか？

表面上はjao Minecraft Server 開発部としていますが、実質的には[Tomachi](https://github.com/book000)が全ての開発を担っています。

## Development

本バージョンから他者による Issue・プルリクエストを受け付けます。

### Environment

- IDE: IntelliJ IDEA
- Language: Java
- Package Manager: Maven

### Test Server

機能のテスト等はローカルサーバでテストしてください。初期設定を実施すると、IntelliJ でプロジェクトを開いたときに自動的にテストサーバが起動します。

テストサーバを利用するための初期設定は以下の通りです。

1. IntelliJ を閉じる
2. `server` ディレクトリを作成する
3. [PaperMCのダウンロードページ](https://papermc.io/downloads) から`1.16.5`の最新のビルドをダウンロードする
4. ダウンロードした jar ファイルを作成した `server` ディレクトリに移動し、 `paper-1.16.5.jar` とリネームする
5. `paper-1.16.5.jar` を起動し、[Minecraft EULA](https://account.mojang.com/documents/minecraft_eula) を読み同意する場合は `eula.txt` の `eula=false` を `eula=true` に変える
6. IntelliJ からプラグインをビルドした後に自動的にリロードするため、[fnetworks/mcrconapi v1.1.1](https://github.com/fnetworks/mcrconapi/releases/tag/v1.1.1) の `mcrconapi-1.1.1.jar` をダウンロード、 `server/mcrconapi-1.1.1.jar` ディレクトリに移動する
7. IntelliJ を起動・プロジェクトを開き、自動実行される `PaperServer` が起動したあとに `op <PlayerName>` を実行し OP 権限を自身に付与する

プラグインをテストする際は以下を行います。

1. ウィンドウ右上「実行」で `ReBuild and Reload` を実行する
2. Minecraftから `localhost` にログインし、機能が動作するかどうかのテストを行う

### Publish

masterブランチ = メインサーバ導入ソースコード

- コミットされると、GitHub Actionsによってビルドが実施されます。失敗した場合、jMS Gamers Club `#github-notice` に通知が飛びます。
- コミットされると、メインサーバでビルドされビルドに成功すれば本番環境の`MinecraftServerDir/plugins/update/`にビルド成果物が配置されます。再起動時に自動的にアップデートされます。
- バージョン表記は本番環境でのビルド処理によって、`yyyy.mm.dd_hh.mm_最終コミットsha8桁`に変更されます。

### Files

#### Command

- 全てのコマンドは [`src/main/java/com/jaoafa/MyMaid4/Command/Cmd_<CommandName>.java`](src/main/java/com/jaoafa/MyMaid4/Command)に配置されます。
- また、ここに配置されるコマンドクラスは CommandPremise インターフェースを実装する必要があります（`implements CommandPremise`）
- コマンドの内容は `registerCommand` で定義します。このメソッドは Main クラスの `registerCommand` から呼び出され、コマンドが追加されます。（`plugin.yml` に書く必要がありません）
- 全てのコマンドのパーミッションは小文字の `mymaid.<CommandName>` でなければなりません

#### Event

- 全てのイベント駆動の機能は [`src/main/java/com/jaoafa/MyMaid4/Event/Event_<FuncName>.java`](src/main/java/com/jaoafa/MyMaid4/Event)に配置されます。
- `<FuncName>` は自由で構いません

### Git

#### Commit rule

- コミットメッセージは **[CommitLint のルール](https://github.com/conventional-changelog/commitlint/tree/master/%40commitlint/config-conventional#rules) である以下に沿っていることを期待しますが、必須ではありません。**
  - 次の形式でコミットメッセージを指定してください: `type(scope): subject` (e.g. `fix(home): message`)
  - `type`, `subject` は必須、 `scope` は必須ではありません
    - `type-enum`: `type` は必ず次のいずれかにしなければなりません
    - `build`: ビルド関連
    - `ci`: CI関連
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

- ブランチは機能追加・修正などに応じて分けて作成してください
- ブランチ名は機能追加・修正の内容を示す言葉で構成してください（例: `add-test-command`, `fix-test-command-api-url`）
- masterブランチへの直接コミットはできません
- 全てのコード追加はプルリクエストを必要とします
- Tomachi に限りセルフマージを可能とします
- レビューはほぼすべてを Tomachi が行います

## License

ライセンスは**独自のライセンスである[jaoLicense](https://github.com/jaoafa/jao-Minecraft-Server/blob/master/jaoLICENSE.md)を適用**します。

### プログラムについて

ここで公開されているプログラムのソースコードには、多分なにかしらの瑕疵やバグが存在します。しかし、開発者および jao Minecraft Server の管理部・開発部はそれらの瑕疵やバグをなるべく除去する努力義務を負いますが、それらによって生じた一切の問題についての**責任を負いません。**  
また、利用者はこのプラグインに実装されている全ての機能及びプログラムなどを jao Minecraft Server の管理部・開発部の許可なく他の場所において、一部もしくは全部を使用することはできません。Discordなどを通じて、明確に許可を取った上で、許可された範囲内で利用してください。
