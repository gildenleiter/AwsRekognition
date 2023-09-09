Amazon Rekognitionを簡単に利用できるシステム

■background
Amazon RekognitionのAPI
Amazon Rekognitionを実施した際に作成されたIDなどはDBに登録される

言語：java
フレームワーク：SpringBoot

AWSの設定
application.properties
aws.accesskey：アクセスキー
aws.secretkey：シークレットキー

アクセスキー、シークレットキーはAWSから取得してください

■DaibutsuRekognition（大仏画像を計測する為にDaibutsuRekognitionという名称になっています）
Amazon Rekognitionを動かすためのWeb画面

言語：TypeScript
フレームワーク：Node.js、Aunglar


■DB
backgroundのapplication.propertiesにデータベースの設定を記載しています。
テーブル構成：テーブル定義書.xlsx参照