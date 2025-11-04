package com.bytebabies.app.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bytebabies.app.data.Repo
import com.bytebabies.app.navigation.Route
import com.bytebabies.app.ui.components.Dropdown
import com.bytebabies.app.ui.components.OutlinedTextFieldFull
import com.bytebabies.app.ui.components.SectionHeader
import com.bytebabies.app.ui.components.TopBar
import java.time.LocalDateTime
import java.util.UUID

@Composable
fun PaymentsScreen(nav: NavHostController) {
    val ctx = LocalContext.current
    var amount by remember { mutableStateOf("1500") }
    var childIdx by remember { mutableStateOf(0) }
    val parentId = Repo.currentParentId
    val children = if (parentId != null) Repo.findChildrenOfParent(parentId) else Repo.children

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TopBar("Payments") }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            if (children.isEmpty()) {
                Text("No children to pay for.")
                return@Column
            }

            SectionHeader("Select Child")
            Dropdown("Child", children.map { it.name }, childIdx) { childIdx = it }

            Spacer(Modifier.height(12.dp))
            OutlinedTextFieldFull(amount, { amount = it }, "Amount (R)", KeyboardType.Number)

            Spacer(Modifier.height(16.dp))
            // Existing mock flow (kept)
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val kid = children[childIdx]
                    val receipt = """
                        ByteBabies Payment Receipt 
                        Receipt#: ${UUID.randomUUID()}
                        Child: ${kid.name}
                        Amount: R ${amount.toDoubleOrNull() ?: 0.0}
                        Date: ${LocalDateTime.now()}
                    """.trimIndent()
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, receipt)
                        putExtra(Intent.EXTRA_SUBJECT, "ByteBabies Receipt")
                    }
                    ctx.startActivity(Intent.createChooser(intent, "Share Receipt"))
                    Toast.makeText(ctx, "Payment completed. Receipt ready to share.", Toast.LENGTH_SHORT).show()
                }
            ) { Text(".") }

            Spacer(Modifier.height(12.dp))

            // NEW: PayFast Sandbox flow
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    // You could store the chosen amount/child in Repo or SavedStateHandle if needed.
                    Repo.tempPaymentAmount = amount.toBigDecimalOrNull() ?: "0".toBigDecimal()
                    Repo.tempPaymentChildName = children[childIdx].name
                    nav.navigate(Route.PayfastCheckout.r)
                }
            ) { Text("Pay with PayFast (Sandbox)") }

            Spacer(Modifier.height(8.dp))
            Text(
                ".",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}
