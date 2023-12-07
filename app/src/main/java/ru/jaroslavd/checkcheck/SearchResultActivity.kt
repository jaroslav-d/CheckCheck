package ru.jaroslavd.checkcheck

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.webkit.DownloadListener
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView

class SearchResultActivity : Activity() {

    private val webView by lazy { WebView(applicationContext) }
    private val textView get() = findViewById<TextView>(R.id.text)
    private val webViewInterface = object {

        @JavascriptInterface
        fun getQrCode(): String {
            return intent.getStringExtra("query") ?: throw Exception("it have not a qr code")
        }

        @JavascriptInterface
        fun putJsonResult(text: String) {
            textView.post { textView.text = text }
        }
    }

    private val downloadListener =
        DownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            if (contentLength > 10) {
                webView.loadUrl(url)
            }
        }

    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_result)
        webView.apply {
            settings.javaScriptEnabled = true
            settings.userAgentString = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) " +
                    "AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.5.1 Safari/605.1.15"
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String) {
                    if (url.startsWith("blob")) {
                        val script = "let json = document.body.innerHTML;Android.putJsonResult(json);"
                        evaluateJavascript(script, null)
                        super.onPageFinished(view, url)
                    } else {
                        val script = resources.assets.open("script.js").bufferedReader().use { it.readText() }
                        evaluateJavascript(script, null)
                        super.onPageFinished(view, url)
                    }
                }
            }
            setDownloadListener(downloadListener)
            loadUrl("https://proverkacheka.com")
            addJavascriptInterface(webViewInterface, "Android")
        }
    }

}