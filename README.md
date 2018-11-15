ssepush-sample-android: Android SSE Push 受信サンプル
=====================================================

Android SDK SSE Push機能の、インスタレーション登録と SSE Push 受信を
確認するサンプルアプリです。

実行方法
---------

Config.java.sample を Config.java にリネームし、Config.java 内の TENANT_ID, APP_ID, APP_KEY, ENDPOINT_URI を設定してください。

Android Studio で本ディレクトリをロードし、ビルド・実行を行ってください。

注意事項
--------

本プログラムは動作確認用のサンプルのため、以下の制限・注意事項があります。

* 本サンプルでは SSE Push の受信処理を Activitiy 内で行っています。バックグランドで SSE Push を正しく受信するようにするためには、Service として実装する必要があります。
* 本サンプルでは受信した通知をそのまま Activity 上に表示しており、Android の Notification は使用していません。ステータスバーなどに通知として表示させる場合は、Notification を使用して通知を行う必要があります。


