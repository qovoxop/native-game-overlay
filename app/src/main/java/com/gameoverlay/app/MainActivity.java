package com.gameoverlay.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public final class MainActivity extends Activity {
    private static final int NOTIFICATION_REQUEST = 42;
    private TextView status;
    private TextView startButton;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        buildScreen();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (status != null) refreshStatus();
    }

    private void buildScreen() {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(24), dp(28), dp(24), dp(28));
        content.setBackgroundColor(Color.rgb(12, 18, 32));

        TextView eyebrow = label("NATIVE ANDROID UTILITY", 12, Color.rgb(110, 231, 249));
        content.addView(eyebrow);
        TextView title = label("Game Overlay", 32, Color.WHITE);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        content.addView(title, margins(0, 8, 0, 10));
        content.addView(label("A transparent, user-controlled floating shortcut that stays above your apps.", 16, Color.rgb(168, 179, 199)), margins(0, 0, 0, 22));

        status = label("", 15, Color.rgb(168, 179, 199));
        content.addView(status, margins(0, 0, 0, 18));

        startButton = action("Start overlay");
        startButton.setOnClickListener(v -> startFlow());
        content.addView(startButton, margins(0, 0, 0, 12));

        TextView stop = action("Stop overlay");
        stop.setOnClickListener(v -> stopOverlay());
        content.addView(stop, margins(0, 0, 0, 26));

        TextView details = label("How it works", 20, Color.WHITE);
        details.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        content.addView(details, margins(0, 0, 0, 8));
        content.addView(label("The app only displays its own floating button after you grant Android’s overlay permission. Tap the button to open a local menu, or drag it to a convenient spot. No screen capture, code injection, private app data, or system modification is used.", 15, Color.rgb(168, 179, 199)), margins(0, 0, 0, 22));

        TextView about = label("Permissions are requested only when needed. The persistent notification is Android’s required disclosure while the overlay is active.", 13, Color.rgb(168, 179, 199));
        content.addView(about);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.rgb(12, 18, 32));
        scroll.addView(content);
        setContentView(scroll);
        refreshStatus();
    }

    private void startFlow() {
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            return;
        }
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_REQUEST);
            return;
        }
        startOverlay();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == NOTIFICATION_REQUEST) {
            if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) startOverlay();
            else Toast.makeText(this, "Notifications are needed to disclose the active overlay.", Toast.LENGTH_LONG).show();
        }
    }

    private void startOverlay() {
        Intent intent = new Intent(this, OverlayService.class);
        intent.setAction(OverlayService.ACTION_START);
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(intent); else startService(intent);
        Toast.makeText(this, "Overlay started", Toast.LENGTH_SHORT).show();
        refreshStatus();
    }

    private void stopOverlay() {
        Intent intent = new Intent(this, OverlayService.class);
        intent.setAction(OverlayService.ACTION_STOP);
        startService(intent);
        Toast.makeText(this, "Overlay stopped", Toast.LENGTH_SHORT).show();
        refreshStatus();
    }

    private void refreshStatus() {
        boolean permission = Settings.canDrawOverlays(this);
        status.setText(permission ? "Status: overlay permission is ready" : "Status: grant overlay permission to begin");
        startButton.setText(permission ? "Start overlay" : "Grant overlay permission");
    }

    private TextView label(String text, int size, int color) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(color);
        return view;
    }

    private TextView action(String text) {
        TextView view = label(text, 16, Color.rgb(12, 18, 32));
        view.setGravity(Gravity.CENTER);
        view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        view.setBackgroundColor(Color.rgb(110, 231, 249));
        view.setPadding(dp(16), dp(15), dp(16), dp(15));
        return view;
    }

    private LinearLayout.LayoutParams margins(int l, int t, int r, int b) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(dp(l), dp(t), dp(r), dp(b));
        return params;
    }

    private int dp(int value) { return (int) (value * getResources().getDisplayMetrics().density + 0.5f); }
}