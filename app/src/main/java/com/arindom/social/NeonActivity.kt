package com.arindom.social

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Build
import android.view.WindowInsets
import android.view.WindowManager
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient

class NeonActivity : Activity() {
    private lateinit var webView: WebView
    private var uploadCallback: ValueCallback<Array<Uri>>? = null
    private val pickMedia = 41

    @Suppress("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(7, 8, 17)
        window.navigationBarColor = android.graphics.Color.rgb(7, 8, 17)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.allowContentAccess = true
        webView.settings.mediaPlaybackRequiresUserGesture = true
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(view: WebView?, callback: ValueCallback<Array<Uri>>?, params: FileChooserParams?): Boolean {
                uploadCallback?.onReceiveValue(null)
                uploadCallback = callback
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
                }
                startActivityForResult(Intent.createChooser(intent, "Choose photo or video"), pickMedia)
                return true
            }
        }
        webView.webViewClient = WebViewClient()
        webView.setBackgroundColor(android.graphics.Color.rgb(7, 8, 17))
        setContentView(webView)
        // Pass system-bar/camera-cutout insets to CSS. Do not pad the
        // WebView itself: fixed-position bottom navigation ignores that padding.
        webView.setOnApplyWindowInsetsListener { view, insets ->
            val topInset: Int
            val bottomInset: Int
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val statusTop = insets.getInsets(WindowInsets.Type.statusBars()).top
                val navBottom = insets.getInsets(WindowInsets.Type.navigationBars()).bottom
                val cutoutTop = insets.displayCutout?.safeInsetTop ?: 0
                topInset = maxOf(statusTop, cutoutTop)
                bottomInset = navBottom
            } else {
                @Suppress("DEPRECATION")
                val statusTop = insets.systemWindowInsetTop
                @Suppress("DEPRECATION")
                val navBottom = insets.systemWindowInsetBottom
                val cutoutTop = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    insets.displayCutout?.safeInsetTop ?: 0
                } else 0
                topInset = maxOf(statusTop, cutoutTop)
                bottomInset = navBottom
            }
            view.setPadding(0, 0, 0, 0)
            view.evaluateJavascript(
                "document.documentElement.style.setProperty('--native-safe-top', '${topInset}px');" +
                    "document.documentElement.style.setProperty('--native-safe-bottom', '${bottomInset}px');",
                null
            )
            insets
        }
        webView.requestApplyInsets()
        webView.loadUrl("file:///android_asset/www/index.html")
    }

    @Deprecated("Deprecated in Android")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pickMedia) {
            val result = if (resultCode == RESULT_OK) data?.data?.let { arrayOf(it) } else null
            uploadCallback?.onReceiveValue(result)
            uploadCallback = null
        }
    }

    @Deprecated("Deprecated in Android")
    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }
}
