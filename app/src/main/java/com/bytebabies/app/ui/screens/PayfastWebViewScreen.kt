package com.bytebabies.app.ui.screens

import android.annotation.SuppressLint
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.bytebabies.app.data.Repo
import com.bytebabies.app.ui.components.TopBar
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.math.BigDecimal
import java.math.RoundingMode

private fun parseAmount(any: Any?): BigDecimal {
    // Accept String, Number, State<*>, etc. and normalise to 2dp BigDecimal.
    val s = when (any) {
        null -> "0"
        is Number -> any.toString()
        else -> any.toString()
    }
        .trim()
        .replace(",", ".")          // allow "1,50" formats
        .replace(Regex("[^0-9.]"), "") // strip stray chars like "R " if present

    return try {
        BigDecimal(s).setScale(2, RoundingMode.HALF_UP)
    } catch (_: Exception) {
        BigDecimal("0.00")
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PayfastWebViewScreen(nav: NavHostController) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TopBar("PayFast Checkout", nav = nav) }
    ) { innerPadding ->

        // ---- Amount (robust parse from any type) ----
        val amount = parseAmount(Repo.tempPaymentAmount)
        val itemName = Repo.tempPaymentChildName?.let { "Fees for $it" } ?: "Creche Fees"

        // ---- PayFast sandbox test merchant details ----
        val merchantId = "10043390"
        val merchantKey = "1o1k8ox301w60"

        // Must be valid HTTPS URLs that resolve publicly
        val returnUrl = "https://example.com/bytebabies/payfast/success"
        val cancelUrl = "https://example.com/bytebabies/payfast/cancel"
        val notifyUrl = "https://sandbox.payfast.co.za/eng/query/validate"

        val qs = mapOf(
            "merchant_id" to merchantId,
            "merchant_key" to merchantKey,
            "amount" to amount.toPlainString(),
            "item_name" to itemName,
            "return_url" to returnUrl,
            "cancel_url" to cancelUrl,
            "notify_url" to notifyUrl,
        ).entries.joinToString("&") { (k, v) ->
            "${URLEncoder.encode(k, StandardCharsets.UTF_8.name())}=${
                URLEncoder.encode(v, StandardCharsets.UTF_8.name())
            }"
        }

        val payfastUrl = "https://sandbox.payfast.co.za/eng/process?$qs"

        AndroidView(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            val url = request?.url?.toString().orEmpty()
                            when {
                                url.startsWith(returnUrl) -> {
                                    Repo.clearTempPayment()
                                    nav.popBackStack()
                                    return true
                                }
                                url.startsWith(cancelUrl) -> {
                                    Repo.clearTempPayment()
                                    nav.popBackStack()
                                    return true
                                }
                            }
                            return false
                        }
                    }
                    loadUrl(payfastUrl)
                }
            }
        )
    }
}
