package com.nec.baas.ssepush;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nec.baas.core.NbAndroidServiceBuilder;
import com.nec.baas.core.NbErrorInfo;
import com.nec.baas.core.NbOperationMode;
import com.nec.baas.core.NbService;
import com.nec.baas.core.NbSetting;
import com.nec.baas.json.NbJSONObject;
import com.nec.baas.push.NbSsePushInstallation;
import com.nec.baas.push.NbSsePushInstallationCallback;
import com.nec.baas.push.NbSsePushReceiveCallback;
import com.nec.baas.push.NbSsePushReceiveClient;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();

    private static final String CHANNEL = "demo";

    private NbService fService;
    private NbSsePushReceiveClient fPushClient;
    private NbSsePushReceiveCallback fCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NbSetting.setOperationMode(NbOperationMode.DEBUG);

        // マルチテナントモードを無効化 (必須ではないが設定することを推奨)
        NbService.enableMultiTenant(false);

        // NbServiceを生成
        fService = new NbAndroidServiceBuilder(this)
                .tenantId(Config.TENANT_ID)
                .appId(Config.APP_ID)
                .appKey(Config.APP_KEY)
                .endPointUri(Config.ENDPOINT_URI)
                .build();

        fPushClient = new NbSsePushReceiveClient();
        fPushClient.setHeartbeatInterval(30L, TimeUnit.SECONDS);

        fCallback = new NbSsePushReceiveCallback() {
            @Override
            public void onConnect() {
                Log.i(TAG, "Connnected.");
            }

            @Override
            public void onDisconnect() {
                Log.i(TAG, "Disconnected");
            }

            @Override
            public void onMessage(NbJSONObject nbJSONObject) {
                Log.i(TAG, "Received.");

                addNotificationToHistory(nbJSONObject);
            }

            @Override
            public void onError(int i, NbErrorInfo nbErrorInfo) {
                Log.e(TAG, "Error: " + nbErrorInfo.getReason());
            }

            @Override
            public void onHeartbeatLost() {
                Log.i(TAG, "HeartbeatLost.");
                fPushClient.disconnect();
                startPolling();
            }
        };

        try {
            registerInstallation();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    private void registerInstallation() {
        NbSsePushReceiveClient.acquireLock();

        final NbSsePushInstallation installation = NbSsePushInstallation.getCurrentInstallation();

        // channel
        Set<String> channels = new HashSet<>();
        channels.add(CHANNEL);
        installation.setChannels(channels);

        // allowedSenders
        // 誰からのPush通知も受け付ける
        Set<String> allowedSenders = new HashSet<>();
        allowedSenders.add("g:anonymous");
        installation.setAllowedSenders(allowedSenders);

        // installation 登録
        installation.save(new NbSsePushInstallationCallback() {
            @Override
            public void onSuccess(NbSsePushInstallation nbSsePushInstallation) {
                Log.i(TAG, "Saved installation.");

                showInstallationInfo(installation);

                NbSsePushReceiveClient.releaseLock();
                startPolling();
            }

            @Override
            public void onFailure(int i, NbErrorInfo nbErrorInfo) {
                Log.e(TAG, "Failed to save installation.");
                Log.e(TAG, String.format("code: %d, info: %s", i, nbErrorInfo));

                NbSsePushReceiveClient.releaseLock();
            }
        });
    }

    // SSEポーリング開始
    private void startPolling() {
        Set<String> events = new HashSet<>();
        events.add("message");

        fPushClient.connect(events, fCallback);
    }

    // 登録したinstallation情報を表示
    private void showInstallationInfo(NbSsePushInstallation installation) {
        String output = installation.getDeviceToken();

        Log.i(TAG, "uri: " + installation.getUri());
        Log.i(TAG, "username: " + installation.getUserName());
        Log.d(TAG, "password: " + installation.getPassword());

        TextView info = findViewById(R.id.info);
        info.setText(output);
    }

    // 通知履歴を表示
    private void addNotificationToHistory(NbJSONObject msgJson) {
        final String message = msgJson.toString();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 一覧へ追加
                LinearLayout history = findViewById(R.id.history);
                TextView text = new TextView(getApplicationContext());
                text.setText(message);
                text.setTextColor(Color.BLACK);
                history.addView(text, 0);
            }
        });
    }
}
