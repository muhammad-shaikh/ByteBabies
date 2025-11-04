package com.bytebabies.app.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bytebabies.app.data.Repo
import com.bytebabies.app.ui.components.Dropdown
import com.bytebabies.app.ui.components.OutlinedTextFieldFull
import com.bytebabies.app.ui.components.SectionHeader
import com.bytebabies.app.ui.components.TopBar
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Composable
fun AttendanceScreen() {
    val today = LocalDate.now()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TopBar("Attendance • Today $today") }
    ) { pad ->
        LazyColumn(
            modifier = Modifier.padding(pad).padding(12.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(Repo.children) { c ->
                val rec = Repo.attendance.find { it.childId == c.id && it.date == today }
                val status = when (rec?.present) {
                    true -> "Present"
                    false -> "Absent"
                    else -> "Unmarked"
                }
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), shape = MaterialTheme.shapes.large) {
                    Row(Modifier.padding(12.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(c.name, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                            Text("Status: $status", color = androidx.compose.ui.graphics.Color.Gray)
                        }
                        Row {
                            TextButton(onClick = { Repo.markAttendance(c.id, today, true) }) { Text("Present") }
                            TextButton(onClick = { Repo.markAttendance(c.id, today, false) }) { Text("Absent") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentsScreen() {
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
            Text("Select Child")
            Dropdown("Child", children.map { it.name }, childIdx) { childIdx = it }
            OutlinedTextFieldFull(amount, { amount = it }, "Amount (R)", androidx.compose.ui.text.input.KeyboardType.Number)
            Button(onClick = {
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
                android.widget.Toast.makeText(ctx, "Payment completed. Receipt ready to share.", android.widget.Toast.LENGTH_SHORT).show()
            }) { Text("Pay Now") }
            Spacer(Modifier.height(8.dp))
            Text("Thank You", color = androidx.compose.ui.graphics.Color.Gray)
        }
    }
}
