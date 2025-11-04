package com.bytebabies.app.payments

object PayfastConfig {

    const val merchantId = "10043390"
    const val merchantKey = "1o1k8ox301w60"
    const val passphrase = "falcongt123456"

    const val processUrl = "https://sandbox.payfast.co.za/eng/process"

    // These appear on PayFast's hosted page
    const val itemNameDefault = "ByteBabies Fee"
    const val itemDescriptionDefault = "Creche payment (sandbox demo)"

    // Deep link return/cancel (must match Manifest intent-filters if you use them)
    const val returnUrl = "bytebabies://payfast/return"
    const val cancelUrl = "bytebabies://payfast/cancel"


    const val notifyUrl = "https://your-server.example.com/payfast/itn" // optional for demo
}
