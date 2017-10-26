# msgpack-json-proxy
HTTPリクエストボディ/レスポンスボディについて、MessagePack形式とJSON形式を相互変換するローカルHTTPプロキシ

## 主な機能

- MessagePackとJSONデータを交互に変換するコマンドラインツール
- `msgpack2json proxy` : リクエストボディのMessagePackをJSONに変換するHTTPプロキシ
- `json2msgpack proxy` : `msgpack2json proxy` が変換したJSONリクエストボディをMessagePackに変換するプロキシ
- デモ用Webサーバ(HTTP) : MessagePackをAjaxでやり取りするサンプルと、テスト用のエンドポイントAPI
- デモ用Webサーバ(HTTPS) : 上記のHTTPS版
- HTTPレスポンスボディの変換(設定ファイルで有効化)
  - `msgpack2json proxy` で変換したリクエストに対して、JSONのレスポンスボディが来たら、MessagePackに変換してクライアントに戻します。
  - `json2msgpack proxy` で変換したリクエストに対して、MessagePackのレスポンスボディが来たら、JSONに変換してクライアントに戻します。
  - 両proxyで、変換してないリクエストに対するJSON/MessagePackレスポンスについては何もしません。

コマンドラインからjarを実行するとヘルプが表示されますので、使い方についてはそちらを参照してください。また、詳細はプロジェクトのWikiページをご覧ください。

```
java -jar msgpack-json-proxy-xxx.jar
```

## msgpack2json / json2msgpack proxy の活用方法

想定ユースケース : HTTPリクエストボディでMessagePackを送り、HTTPレスポンスボディでMessagePackを返すAPIエンドポイントに対して・・・
- ローカルHTTPプロキシツールを挟んで、MessagePackの中身を人間にとって見やすいJSONで確認したい。
- ローカルHTTPプロキシツール上でMessagePackの中身を編集したい。

→ 以下のように msgpack2json proxy と json2msgpack proxy の間にローカルHTTPプロキシを挟むことで実現可能です。

```
(request)
client -(msgpack)->[msgpack2json]-(json)->[local HTTP proxy]-(json)->[json2msgpack]-(msgpack)-> server

(response)
client <-(msgpack)-[msgpack2json]<-(json)-[local HTTP proxy]-(json)<-[json2msgpack]<-(msgpack)- server
```

## v1.0.0 での既知の制限

- float(32)フォーマットを送ると、float(64)に変換されてしまいます。
- intフォーマットのsigned/unsignedの区別は未対応です。
  - [使用しているライブラリ](https://github.com/msgpack/msgpack-java) の実装依存になります。
- MessagePack以外のリクエストについては、レスポンスボディの変換は行われません。

## requirement

* Java8

## 開発環境

* JDK >= 1.8.0_92
* Eclipse >= 4.5.2 (Mars.2 Release), "Eclipse IDE for Java EE Developers" パッケージを使用
* Maven >= 3.3.9 (maven-wrapperにて自動的にDLしてくれる)
* ソースコードやテキストファイル全般の文字コードはUTF-8を使用

## ビルドと実行

```
cd msgpack-json-proxy/

ビルド:
mvnw package

jarファイルから実行:
java -jar target/msgpack-json-proxy-xxx.jar

Mavenプロジェクトから直接実行:
mvnw exec:java
```

## Eclipseプロジェクト用の設定

https://github.com/SecureSkyTechnology/howto-eclipse-setup の `setup-type1` を使用。README.mdで以下を参照のこと:

* Ecipseのインストール
* Clean Up/Formatter 設定
* 必須プラグイン
  * Lombok
* GitでcloneしたMavenプロジェクトのインポート 
