package com.azri.tunely;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import java.io.InputStream;
import java.net.URL;

public class MusicService extends Service {
    static final String CH = "tunely_play";
    static MediaPlayer mp;
    static String title = "Tunely", artist = "", cmd = "";
    static boolean ended = false, playing = false, ready = false;
    static MusicService inst;
    Bitmap art;
    PowerManager.WakeLock wl;
    NotificationManager nm;

    @Override public void onCreate() {
        super.onCreate();
        inst = this;
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.createNotificationChannel(new NotificationChannel(CH, "Pemutar Tunely", NotificationManager.IMPORTANCE_LOW));
        wl = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "tunely:play");
    }

    @Override public int onStartCommand(Intent i, int flags, int id) {
        String a = i == null ? null : i.getAction();
        if ("PLAY".equals(a)) {
            title = i.getStringExtra("title"); artist = i.getStringExtra("artist");
            show();
            loadArt(i.getStringExtra("img"));
            start(i.getStringExtra("url"));
        } else if ("TOGGLE".equals(a)) { if (playing) doPause(); else doResume(); }
        else if ("NEXT".equals(a)) cmd = "next";
        else if ("PREV".equals(a)) cmd = "prev";
        else if ("STOP".equals(a)) { doPause(); stopForeground(true); stopSelf(); }
        else show();
        return START_STICKY;
    }

    void show() {
        Notification n = build();
        if (Build.VERSION.SDK_INT >= 29) startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        else startForeground(1, n);
    }

    PendingIntent pi(String a, int code) {
        return PendingIntent.getService(this, code, new Intent(this, MusicService.class).setAction(a),
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    Notification build() {
        PendingIntent open = PendingIntent.getActivity(this, 9, new Intent(this, MainActivity.class),
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder b = new Notification.Builder(this, CH)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(title).setContentText(artist).setContentIntent(open)
            .setOngoing(playing).setVisibility(Notification.VISIBILITY_PUBLIC)
            .addAction(android.R.drawable.ic_media_previous, "Sebelumnya", pi("PREV", 1))
            .addAction(playing ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play, "Putar/Jeda", pi("TOGGLE", 2))
            .addAction(android.R.drawable.ic_media_next, "Berikutnya", pi("NEXT", 3))
            .setStyle(new Notification.MediaStyle().setShowActionsInCompactView(0, 1, 2));
        if (art != null) b.setLargeIcon(art);
        return b.build();
    }

    void refresh() { nm.notify(1, build()); }

    void loadArt(String u) {
        art = null;
        if (u == null || u.isEmpty()) return;
        new Thread(() -> {
            try (InputStream in = new URL(u).openStream()) { art = BitmapFactory.decodeStream(in); refresh(); } catch (Exception e) { }
        }).start();
    }

    void start(String url) {
        try {
            if (mp != null) { mp.release(); mp = null; }
            ready = false; ended = false; playing = false;
            mp = new MediaPlayer();
            mp.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());
            mp.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
            mp.setDataSource(url);
            mp.setOnPreparedListener(m -> { ready = true; m.start(); playing = true; if (!wl.isHeld()) wl.acquire(6 * 3600 * 1000L); refresh(); });
            mp.setOnCompletionListener(m -> { ended = true; playing = false; refresh(); });
            mp.setOnErrorListener((m, w, e) -> { playing = false; ended = true; return true; });
            mp.prepareAsync();
        } catch (Exception e) { ended = true; }
    }

    static void doPause() { if (mp != null && ready && playing) { mp.pause(); playing = false; if (inst != null) inst.refresh(); } }
    static void doResume() { if (mp != null && ready && !playing) { mp.start(); playing = true; if (inst != null) inst.refresh(); } }
    static void doSeek(int ms) { if (mp != null && ready) mp.seekTo(ms); }

    // format: posisiMs|durasiMs|playing|ended|perintah
    static String state() {
        int p = 0, d = 0;
        try { if (mp != null && ready) { p = mp.getCurrentPosition(); d = mp.getDuration(); } } catch (Exception e) { }
        String s = p + "|" + d + "|" + (playing ? 1 : 0) + "|" + (ended ? 1 : 0) + "|" + cmd;
        ended = false; cmd = "";
        return s;
    }

    @Override public void onDestroy() {
        if (mp != null) { mp.release(); mp = null; }
        if (wl != null && wl.isHeld()) wl.release();
        inst = null;
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent i) { return null; }
}
