package com.bytebabies.app.payments

import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.bytebabies.app.navigation.Route
import com.bytebabies.app.ui.components.TopBar

@Composable
fun PayfastWebViewScreen(
    nav: NavHostController,
    amountRands: String,
    buyerEmail: String,
    itemName: String = PayfastConfig.itemNameDefault,
    itemDescription: String = PayfastConfig.itemDescriptionDefault
) {
    var loading by remember { mutableStateOf(true) }

    Scaffold(
        topBar = { TopBar("PayFast Checkout", nav = nav) }
    ) { pad ->
        Box(Modifier.fillMaxSize()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                        webChromeClient = WebChromeClient()
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                loading = true
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                loading = false
                            }

                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                val u = request?.url?.toString().orEmpty()
                                // Handle deep-link returns (optional)
                                if (u.startsWith(PayfastConfig.returnUrl)) {
                                    // Success — return to Payments screen (or show a success state)
                                    nav.popBackStack() // back to PaymentsScreen
                                    return true
                                }
                                if (u.startsWith(PayfastConfig.cancelUrl)) {
                                    // Cancelled
                                    nav.popBackStack()
                                    return true
                                }
                                return false
                            }
                        }

                        // Build params for the POST form
                        val params = mapOf(
                            "merchant_id" to PayfastConfig.merchantId,
                            "merchant_key" to PayfastConfig.merchantKey,
                            "return_url" to PayfastConfig.returnUrl,
                            "cancel_url" to PayfastConfig.cancelUrl,
                            "notify_url" to PayfastConfig.notifyUrl, // optional for demo
                            "amount" to amountRands,                 // e.g. "150.00"
                            "item_name" to itemName,
                            "item_description" to itemDescription,
                            "email_address" to buyerEmail,
                            "currency" to "ZAR",
                            "email_confirmation" to "1",
                            "confirmation_address" to buyerEmail
                        )

                        val signature = PayfastSigner.sign(params, PayfastConfig.passphrase)

                        val formInputs = (params + mapOf("signature" to signature))
                            .entries.joinToString("\n") { (k, v) ->
                                """<input type="hidden" name="$k" value="${html(v)}"/>"""
                            }

                        val html = """
                            <html>
                              <body onload="document.forms[0].submit()">
                                <form action="${PayfastConfig.processUrl}" method="post" accept-charset="utf-8">
                                  $formInputs
                                  <noscript>
                                    <p>Please click the button to proceed to PayFast.</p>
                                    <button type="submit">Pay with PayFast</button>
                                  </noscript>
                                </form>
                              </body>
                            </html>
                        """.trimIndent()

                        loadDataWithBaseURL(
                            PayfastConfig.processUrl,
                            html,
                            "text/html",
                            "utf-8",
                            null
                        )
                    }
                }
            )

            if (loading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

private fun html(s: String): String =
    s.replace("&", "&amp;")
        .replace("\"", "&quot;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
