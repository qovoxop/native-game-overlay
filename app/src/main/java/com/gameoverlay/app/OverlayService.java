package com.gameoverlay.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public final class OverlayService extends Service {
    public static final String ACTION_START = "com.gameoverlay.app.START";
    public static final String ACTION_STOP = "com.gameoverlay.app.STOP";
    private static final String CHANNEL_ID = "overlay_controls";
    private WindowManager windowManager;
    private TextView bubble;
    private WindowManager.LayoutParams params;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ACTION_STOP.equals(intent == null ? null : intent.getAction())) {
            stopOverlay();
        } else if (ACTION_START.equals(intent == null ? null : intent.getAction())) {
            startOverlay();
        }
        return START_NOT_STICKY;
    }

    private void startOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            stopSelf();
            return;
        }
        createChannel();
        Intent open = new Intent(this, MainActivity.class);
        PendingIntent pending = PendingIntent.getActivity(this, 0, open, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_view)
                .setContentTitle("Game Overlay is active")
                .setContentText("Tap the floating button to open the local menu.")
                .setContentIntent(pending)
                .setOngoing(true)
                .build();
        startForeground(1001, notification);
        if (bubble == null) addBubble();
    }

    private void addBubble() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        bubble = new TextView(this);
        bubble.setText("GO");
        bubble.setTextColor(Color.rgb(12, 18, 32));
        bubble.setTextSize(13);
        bubble.setGravity(Gravity.CENTER);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.rgb(110, 231, 249));
        bg.setShape(GradientDrawable.OVAL);
        bubble.setBackground(bg);
        int type = Build.VERSION.SDK_INT >= 26 ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;
        params = new WindowManager.LayoutParams(dp(58), dp(58), type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.END;
        params.x = dp(18);
        params.y = dp(180);
        bubble.setOnTouchListener(new DragHandler());
        bubble.setOnClickListener(v -> showMenu());
        windowManager.addView(bubble, params);
    }

    private void showMenu() {
        Toast.makeText(this, "Overlay menu: shortcut ready", Toast.LENGTH_SHORT).show();
    }

    private void stopOverlay() {
        if (bubble != null && windowManager != null) {
            windowManager.removeView(bubble);
            bubble = null;
        }
        stopForeground(STOP_FOREGROUND_REMOVE);
        stopSelf();
    }

    @Override public void onDestroy() { stopOverlay(); super.onDestroy(); }
    @Override public IBinder onBind(Intent intent) { return null; }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, getString(R.string.overlay_channel_name), NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(getString(R.string.overlay_channel_description));
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    private int dp(int value) { return (int) (value * getResources().getDisplayMetrics().density + 0.5f); }

    private final class DragHandler implements View.OnTouchListener {
        private int downX, downY, startX, startY;
        private long downTime;
        @Override public boolean onTouch(View view, MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    downX = (int) event.getRawX(); downY = (int) event.getRawY();
                    startX = params.x; startY = params.y; downTime = System.currentTimeMillis();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    params.x = startX - ((int) event.getRawX() - downX);
                    params.y = startY + ((int) event.getRawY() - downY);
                    windowManager.updateViewLayout(bubble, params);
                    return true;
                case MotionEvent.ACTION_UP:
                    if (System.currentTimeMillis() - downTime < 220 && Math.abs(event.getRawX() - downX) < 12 && Math.abs(event.getRawY() - downY) < 12) showMenu();
                    return true;
                default: return false;
            }
        }
    }
}