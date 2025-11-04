package com.bytebabies.app.payments

import java.net.URLEncoder
import java.security.MessageDigest
import java.util.Locale

object PayfastSigner {

    /**
     * Build the signature according to PayFast rules:
     * 1) Sort params alphabetically by key
     * 2) Exclude empty values and `signature` itself
     * 3) URL-encode values (space as %20, not +)
     * 4) Join with & and append `&passphrase=...` if you use one
     * 5) SHA-512 hex lower-case
     */
    fun sign(params: Map<String, String>, passphrase: String?): String {
        val filtered = params
            .filter { (k, v) -> k.lowercase(Locale.ROOT) != "signature" && v.isNotEmpty() }
            .toSortedMap(String.CASE_INSENSITIVE_ORDER)

        val base = filtered.entries.joinToString("&") { (k, v) ->
            "${encode(k)}=${encode(v)}"
        } + (if (!passphrase.isNullOrEmpty()) "&passphrase=${encode(passphrase)}" else "")

        return sha512(base)
    }

    private fun encode(v: String): String =
        URLEncoder.encode(v, Charsets.UTF_8.name()).replace("+", "%20")

    private fun sha512(input: String): String {
        val md = MessageDigest.getInstance("SHA-512")
        val bytes = md.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
