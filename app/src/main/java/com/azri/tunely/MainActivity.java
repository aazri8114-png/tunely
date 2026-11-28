package com.azri.tunely;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    WebView web;
    Handler ui = new Handler(Looper.getMainLooper());

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        if (Build.VERSION.SDK_INT >= 33) requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        web = new WebView(this);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowUniversalAccessFromFileURLs(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        web.setBackgroundColor(0xFF0B0A14);
        web.setWebViewClient(new WebViewClient());
        web.addJavascriptInterface(new Bridge(), "TunelyNative");
        setContentView(web);
        web.loadUrl("file:///android_asset/index.html");
    }

    class Bridge {
        @JavascriptInterface public void play(String url, String title, String artist, String img) {
            Intent i = new Intent(MainActivity.this, MusicService.class).setAction("PLAY")
                .putExtra("url", url).putExtra("title", title).putExtra("artist", artist).putExtra("img", img);
            startForegroundService(i);
        }
        @JavascriptInterface public void pause() { ui.post(MusicService::doPause); }
        @JavascriptInterface public void resume() { ui.post(MusicService::doResume); }
        @JavascriptInterface public void seek(int ms) { ui.post(() -> MusicService.doSeek(ms)); }
        @JavascriptInterface public String state() { return MusicService.state(); }
    }

    // Tombol kembali tidak menutup aplikasi, musik tetap jalan.
    @Override public void onBackPressed() { moveTaskToBack(true); }
}
