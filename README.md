# msgpack-json-proxy
HTTPリクエストボディ/レスポンスボディについて、MessagePack形式とJSON形式を相互変換するローカルHTTPプロキシ

## 主な機能

- MessagePackとJSONデータを交互に変換するコマンドラインツール
- `msgpack2json proxy` : リクエストボディのMessagePackをJSONに変換するHTTPプロキシ
- `json2msgpack proxy` : `msgpack2json proxy` が送信してきたJSONリクエストボディをMessagePackに変換するプロキシ
- デモ用Webサーバ(HTTP) : MessagePackをAjaxでやり取りするサンプルと、テスト用のエンドポイントAPI
- デモ用Webサーバ(HTTPS) : 上記のHTTPS版

コマンドラインからjarを実行するとヘルプが表示されますので、使い方についてはそちらを参照してください。また、詳細はプロジェクトのWikiページをご覧ください。

```
java -jar msgpack-json-proxy-xxx.jar
```

## msgpack2json / json2msgpack proxy の活用方法

想定ユースケース(HTTPリクエストボディでMessagePackを送るとき):

- MessagePackの中身を人間にとって見やすいJSONで確認したい。
- MessagePackの中身を編集したい。

そのような場合に、以下のように msgpack2json proxy と json2msgpack proxy の間にローカルHTTPプロキシを挟むことで、MessagePackの中身をJSONで変換したり、ローカルHTTPプロキシ上でJSONを編集することでMessagePackの中身を編集することが可能となります。

```
[client]-(msgpack)->[msgpack2json proxy]-(json)->[ローカルHTTPプロキシ]-(json)->[json2msgpack proxy]-(msgpack)->[server]
```

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
* 必須プラグイン Lombok
* GitでcloneしたMavenプロジェクトのインポート 
