package com.arindom.social

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
