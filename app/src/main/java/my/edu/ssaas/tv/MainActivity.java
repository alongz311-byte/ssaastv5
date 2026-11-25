package my.edu.ssaas.tv;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.webkit.WebViewAssetLoader;

public class MainActivity extends Activity {
    /* Halaman dihidangkan dari asset dengan alamat https (bukan file://) supaya YouTube & fetch berfungsi */
    private static final String URL = "https://appassets.androidplatform.net/assets/index.html";
    private FrameLayout root;
    private WebView web;
    private WebViewAssetLoader loader;
    private final Handler h = new Handler(Looper.getMainLooper());
    private long lastBack = 0;
    private FrameLayout splash;      /* wallpaper + bar loading */
    private ImageView splashImg;
    private View barFill, barTrack;
    private TextView barText;
    private float shown = 0, target = 0;
    private boolean pageDone = false;
    private long startAt;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_FULLSCREEN);
        loader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .build();
        root = new FrameLayout(this);
        root.setBackgroundColor(0xFF070B1A);
        setContentView(root);
        hideUi();
        startAt = System.currentTimeMillis();
        createWeb();
        buildSplash();
        h.postDelayed(this::hideSplash, 15000);   /* had maksimum jika internet lambat */
    }

    private void createWeb() {
        web = new WebView(this);
        web.setBackgroundColor(0xFF070B1A);
        web.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        web.setKeepScreenOn(true);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);   /* autoplay dengan bunyi */
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        if (Build.VERSION.SDK_INT >= 21) s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        s.setUserAgentString(s.getUserAgentString().replace("; wv", ""));
        web.setWebChromeClient(new WebChromeClient() {
            @Override public void onProgressChanged(WebView v, int p) { if (p > target) target = p; }
        });
        web.setWebViewClient(new WebViewClient() {
            @Override public WebResourceResponse shouldInterceptRequest(WebView v, WebResourceRequest r) {
                return loader.shouldInterceptRequest(r.getUrl());
            }
            @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                return !r.isForMainFrame() ? false : !r.getUrl().toString().startsWith("https://appassets.androidplatform.net");
            }
            @Override public void onPageFinished(WebView v, String url) {
                pageDone = true; target = 100;
            }
            @Override public void onReceivedError(WebView v, int code, String desc, String url) {
                /* Halaman utama gagal dimuat -> cuba semula 30 saat kemudian */
                h.postDelayed(MainActivity.this::reload, 30000);
            }
            /* PENTING: jika enjin WebView ranap (punca skrin kosong), bina semula tanpa tutup app */
            @Override public boolean onRenderProcessGone(WebView v, RenderProcessGoneDetail d) {
                recreateWeb();
                return true;
            }
        });
        root.addView(web, new FrameLayout.LayoutParams(-1, -1));
        web.requestFocus();
        web.loadUrl(URL);
    }

    private int dp(float v) { return Math.round(v * getResources().getDisplayMetrics().density); }

    /* Skrin pembuka: wallpaper penuh + bar loading emas + peratus di bahagian bawah */
    private void buildSplash() {
        splash = new FrameLayout(this);
        splash.setBackgroundColor(0xFF070B1A);
        splashImg = new ImageView(this);
        splashImg.setImageResource(R.drawable.splash);
        splashImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        splash.addView(splashImg, new FrameLayout.LayoutParams(-1, -1));

        /* Lapisan gelap lembut di bawah supaya tulisan jelas */
        View shade = new View(this);
        shade.setBackground(new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                new int[]{0xCC000000, 0x00000000}));
        FrameLayout.LayoutParams sl = new FrameLayout.LayoutParams(-1, dp(110), Gravity.BOTTOM);
        splash.addView(shade, sl);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER_HORIZONTAL);

        barText = new TextView(this);
        barText.setTextColor(0xFFFFF4CF);
        barText.setTextSize(15);
        barText.setTypeface(barText.getTypeface(), android.graphics.Typeface.BOLD);
        barText.setShadowLayer(4, 0, 2, Color.BLACK);
        barText.setLetterSpacing(0.08f);
        barText.setText("MEMUATKAN SSAASTV...  0%");
        box.addView(barText, new LinearLayout.LayoutParams(-2, -2));

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        View l = new View(this), r = new View(this);
        FrameLayout track = new FrameLayout(this);
        barTrack = track;
        GradientDrawable tg = new GradientDrawable();
        tg.setColor(0x66000000); tg.setCornerRadius(dp(6)); tg.setStroke(dp(1), 0xCCFCD34D);
        track.setBackground(tg);
        barFill = new View(this);
        GradientDrawable fg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xFFFFF7CC, 0xFFFCD34D, 0xFFF59E0B});
        fg.setCornerRadius(dp(6));
        barFill.setBackground(fg);
        track.addView(barFill, new FrameLayout.LayoutParams(0, -1));
        row.addView(l, new LinearLayout.LayoutParams(0, 1, 1));
        row.addView(track, new LinearLayout.LayoutParams(0, dp(12), 2));
        row.addView(r, new LinearLayout.LayoutParams(0, 1, 1));
        LinearLayout.LayoutParams rl = new LinearLayout.LayoutParams(-1, -2);
        rl.topMargin = dp(10);
        box.addView(row, rl);

        FrameLayout.LayoutParams bl = new FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM);
        bl.bottomMargin = dp(36);
        splash.addView(box, bl);
        root.addView(splash, new FrameLayout.LayoutParams(-1, -1));
        h.post(tick);
    }

    /* Gerakkan bar dengan lancar; tidak pernah nampak "beku" */
    private final Runnable tick = new Runnable() {
        @Override public void run() {
            if (splash == null) return;
            if (!pageDone && target < 90) target = Math.min(90, target + 0.25f);
            shown += (target - shown) * 0.12f;
            if (target - shown < 0.5f) shown = target;
            int p = Math.round(shown);
            int w = barTrack.getWidth();
            if (w > 0) {
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) barFill.getLayoutParams();
                lp.width = Math.max(dp(12), w * p / 100);
                barFill.setLayoutParams(lp);
            }
            barText.setText((p >= 100 ? "SEDIA" : "MEMUATKAN SSAASTV...") + "  " + p + "%");
            if (pageDone && p >= 100 && System.currentTimeMillis() - startAt > 3000) {
                h.postDelayed(MainActivity.this::hideSplash, 600);
                return;
            }
            h.postDelayed(this, 50);
        }
    };

    private void hideSplash() {
        final FrameLayout s = splash;
        if (s == null) return;
        splash = null;
        h.removeCallbacks(tick);
        s.animate().alpha(0f).setDuration(800).withEndAction(() -> {
            root.removeView(s);
            if (splashImg != null) { splashImg.setImageDrawable(null); splashImg = null; }   /* lepaskan memori gambar */
        }).start();
        getWindow().setBackgroundDrawableResource(android.R.color.black);
    }

    private void recreateWeb() {
        try { root.removeView(web); web.destroy(); } catch (Exception ignored) {}
        web = null;
        h.postDelayed(this::createWeb, 1000);
    }

    private void reload() { if (web != null) web.loadUrl(URL); }

    private void hideUi() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    @Override public void onWindowFocusChanged(boolean f) { super.onWindowFocusChanged(f); if (f) hideUi(); }
    @Override protected void onResume() { super.onResume(); if (web != null) web.onResume(); }

    /* Butang BACK: tekan 2 kali dalam 2 saat untuk keluar (elak tertutup tidak sengaja) */
    @Override public boolean onKeyDown(int k, KeyEvent e) {
        if (k == KeyEvent.KEYCODE_BACK) {
            long n = System.currentTimeMillis();
            if (n - lastBack < 2000) { finish(); return true; }
            lastBack = n;
            Toast.makeText(this, "Tekan BACK sekali lagi untuk keluar", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onKeyDown(k, e);
    }

    @Override protected void onDestroy() {
        h.removeCallbacksAndMessages(null);
        if (web != null) { web.destroy(); web = null; }
        super.onDestroy();
    }
}
