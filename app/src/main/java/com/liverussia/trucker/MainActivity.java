package com.liverussia.trucker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int OVERLAY_PERMISSION_REQUEST = 1;
    private boolean isRunning = false;
    private Thread truckerThread;

    private static final int ACCEPT_X = 360, ACCEPT_Y = 820;
    private static final int GAS_X = 530, GAS_Y = 1365;
    private static final int FINISH_X = 360, FINISH_Y = 955;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnStart = findViewById(R.id.btnStart);
        Button btnStop = findViewById(R.id.btnStop);

        btnStart.setOnClickListener(v -> checkPermissionsAndStart());
        btnStop.setOnClickListener(v -> stopTrucker());
    }

    private void checkPermissionsAndStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST);
            } else {
                startTrucker();
            }
        } else {
            startTrucker();
        }
    }

    private void startTrucker() {
        if (isRunning) return;
        isRunning = true;
        Toast.makeText(this, "Trucker START", Toast.LENGTH_SHORT).show();

        truckerThread = new Thread(() -> {
            while (isRunning) {
                try {
                    TapService.performTap(ACCEPT_X, ACCEPT_Y);
                    Thread.sleep(500);
                    TapService.performTap(ACCEPT_X, ACCEPT_Y);
                    Thread.sleep(2000);
                    for (int i = 0; i < 45 && isRunning; i++) {
                        TapService.performTap(GAS_X, GAS_Y);
                        Thread.sleep(1000);
                    }
                    if (isRunning) {
                        TapService.performTap(FINISH_X, FINISH_Y);
                        Thread.sleep(3000);
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        truckerThread.start();
    }

    private void stopTrucker() {
        isRunning = false;
        if (truckerThread != null) truckerThread.interrupt();
        Toast.makeText(this, "Trucker STOP", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_REQUEST) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                startTrucker();
            } else {
                Toast.makeText(this, "Permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }
    }
