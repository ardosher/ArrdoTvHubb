package uz.ardo.tvhub

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient

/** Televizorda brauzer bo'lmasa, havolani ilova ichida ochadi. */
class WebActivity : Activity() {
    private lateinit var web: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        web = WebView(this)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.webViewClient = WebViewClient()
        setContentView(web)
        web.loadUrl(intent.getStringExtra("url") ?: "https://ardosher.uz/ArdoTvHub")
    }

    override fun onBackPressed() {
        if (web.canGoBack()) web.goBack() else super.onBackPressed()
    }
}
