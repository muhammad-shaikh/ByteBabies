package com.bytebabies.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.bytebabies.app.data.Repo
import com.bytebabies.app.navigation.Route
import com.bytebabies.app.ui.components.*
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun ParentHome(nav: androidx.navigation.NavHostController) {
    val ctx = LocalContext.current
    val pid = Repo.currentParentId
    val absent = pid?.let { Repo.absentTodayForParent(it) } ?: emptyList()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopBar(
                title = "Parent Dashboard",
                showSwitch = true,
                onSwitch = {
                    // Clear in-memory auth and go back to Login
                    Repo.currentRole = null
                    Repo.currentParentId = null
                    nav.navigate(Route.Login.r) {
                        popUpTo(Route.Login.r) { inclusive = true }
                    }
                },
                actions = {
                    if (absent.isNotEmpty()) {
                        AssistChip(
                            onClick = {},
                            label = { Text("${absent.size} ABSENT alert(s)") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                TextButton(onClick = { nav.navigate(Route.ParentMessages.r) }) { Text("Messages") }
                TextButton(onClick = { nav.navigate(Route.ParentEvents.r) }) { Text("Events") }
                TextButton(onClick = { nav.navigate(Route.ParentMeals.r) }) { Text("Meals") }
                TextButton(onClick = { nav.navigate(Route.Payments.r) }) { Text("Payments") }
            }
        }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            SectionHeader("Quick Links")
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FeatureCard(
                    title = "Attendance",
                    description = "View your child’s daily attendance",
                    onClick = { nav.navigate(Route.ParentAttendance.r) },
                    gradient = bbGradient(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.secondaryContainer)
                )
                FeatureCard(
                    title = "Media Gallery",
                    description = "Photos & videos from class (consent required)",
                    onClick = { nav.navigate(Route.ParentMedia.r) },
                    gradient = bbGradient(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.tertiaryContainer)
                )
                FeatureCard(
                    title = "Meals & Orders",
                    description = "Menu & pre-order lunches",
                    onClick = { nav.navigate(Route.ParentMeals.r) },
                    gradient = bbGradient(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.primaryContainer)
                )
            }

            Spacer(Modifier.height(16.dp))
            Text("Announcements")
            LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                items(Repo.messages.filter { !it.toAdmin }.sortedByDescending { it.timestamp }) { msg ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), shape = MaterialTheme.shapes.large) {
                        ListItem(
                            headlineContent = { Text("School Announcement") },
                            supportingContent = { Text(msg.content) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ParentMessagesScreen() {
    var message by remember { mutableStateOf("") }
    val pid = Repo.currentParentId
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TopBar("Messages") }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            SectionHeader("Send Message to Admin")
            OutlinedTextFieldFull(message, { message = it }, "Type your message")
            Button(onClick = {
                if (pid != null && message.isNotBlank()) {
                    Repo.messages.add(com.bytebabies.app.model.Message(fromParentId = pid, toAdmin = true, content = message))
                    message = ""
                }
            }) { Text("Send") }

            SectionHeader("Your Sent Messages")
            LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                items(Repo.messages.filter { it.fromParentId == pid }.sortedByDescending { it.timestamp }) { msg ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), shape = MaterialTheme.shapes.large) {
                        ListItem(
                            headlineContent = { Text("To Admin • ${msg.timestamp.toLocalTime()}") },
                            supportingContent = { Text(msg.content) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ParentEventsScreen() {
    val activity = LocalActivity.current
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TopBar("Events") }
    ) { pad ->
        LazyColumn(Modifier.padding(pad).padding(16.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
            items(Repo.events.sortedBy { it.date }) { e ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), shape = MaterialTheme.shapes.large) {
                    ListItem(
                        headlineContent = { Text("${e.title} • ${e.date}") },
                        supportingContent = { Text(e.description) },
                        trailingContent = {
                            TextButton(onClick = {
                                val startMillis = e.date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                                val intent = Intent(Intent.ACTION_INSERT).apply {
                                    data = CalendarContract.Events.CONTENT_URI
                                    putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
                                    putExtra(CalendarContract.Events.TITLE, e.title)
                                    putExtra(CalendarContract.Events.DESCRIPTION, e.description)
                                    putExtra(CalendarContract.Events.EVENT_LOCATION, e.location)
                                    putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
                                }
                                activity?.startActivity(intent)
                            }) { Text("Add to Calendar") }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ParentAttendanceScreen() {
    val pid = Repo.currentParentId
    val kids = if (pid != null) Repo.findChildrenOfParent(pid) else emptyList()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TopBar("Attendance") }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            kids.forEach { c ->
                SectionHeader(c.name)
                val records = Repo.attendance.filter { it.childId == c.id }.sortedByDescending { it.date }
                if (records.isEmpty()) {
                    Text("No records yet.")
                } else {
                    records.forEach { r ->
                        Text("${r.date}: " + if (r.present) "Present" else "Absent")
                    }
                }
            }
        }
    }
}

@Composable
fun ParentMealsScreen() {
    val pid = Repo.currentParentId
    val kids = if (pid != null) Repo.findChildrenOfParent(pid) else emptyList()
    var kidIdx by remember { mutableStateOf(0) }
    var dateStr by remember { mutableStateOf(LocalDate.now().plusDays(1).toString()) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TopBar("Meals") }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            if (kids.isEmpty()) { Text("No children available."); return@Column }
            Text("Child")
            Dropdown("Child", kids.map { it.name }, kidIdx) { kidIdx = it }
            OutlinedTextFieldFull(dateStr, { dateStr = it }, "Date (YYYY-MM-DD)")
            SectionHeader("Menu")
            Repo.meals.forEach { m ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), shape = MaterialTheme.shapes.large) {
                    Column(Modifier.padding(12.dp)) {
                        Text(m.name, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                        Text("${m.ingredients} • ${m.dietaryInfo}", color = androidx.compose.ui.graphics.Color.Gray)
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            Button(onClick = {
                                runCatching { LocalDate.parse(dateStr) }.onSuccess { d ->
                                    Repo.orders.add(com.bytebabies.app.model.MealOrder(childId = kids[kidIdx].id, mealId = m.id, date = d))
                                }
                            }) { Text("Pre-Order") }
                        }
                    }
                }
            }
            SectionHeader("Your Orders")
            val yourOrders = Repo.orders.filter { o -> kids.any { it.id == o.childId } }.sortedByDescending { it.date }
            yourOrders.forEach { o ->
                Text("${o.date}: ${Repo.childName(o.childId)} → ${Repo.mealName(o.mealId)}")
            }
        }
    }
}

@Composable
fun ParentMediaScreen() {
    val pid = Repo.currentParentId
    val parent = Repo.parents.find { it.id == pid }
    val consent = parent?.consentMedia == true

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TopBar("Media") }
    ) { pad ->
        if (!consent) {
            Column(Modifier.padding(pad).padding(16.dp)) {
                Text("Media consent not granted.", color = androidx.compose.ui.graphics.Color(0xFFB71C1C))
                Text("Please enable consent in Profile to view/download shared media.")
            }
            return@Scaffold
        }
        LazyColumn(Modifier.padding(pad).padding(12.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
            items(Repo.media) { m ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), shape = MaterialTheme.shapes.large) {
                    Column(Modifier.padding(12.dp)) {
                        Text(m.caption.ifBlank { "Media" }, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        Image(
                            painter = rememberAsyncImagePainter(m.uri),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.height(8.dp))
                        val ctx = LocalContext.current
                        Button(onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "*/*"
                                putExtra(Intent.EXTRA_STREAM, Uri.parse(m.uri))
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            ctx.startActivity(Intent.createChooser(intent, "Share Media"))
                        }) { Text("Download/Share") }
                    }
                }
            }
        }
    }
}

@Composable
fun ParentProfileScreen() {
    val pid = Repo.currentParentId
    val p = Repo.parents.find { it.id == pid } ?: return
    var consent by remember { mutableStateOf(p.consentMedia) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TopBar("Profile") }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            LabeledText("Name", p.name)
            Spacer(Modifier.height(8.dp))
            LabeledText("Email", p.email)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("Media Consent")
                Spacer(Modifier.width(12.dp))
                Switch(checked = consent, onCheckedChange = {
                    consent = it
                    p.consentMedia = it
                })
            }
        }
    }
}
