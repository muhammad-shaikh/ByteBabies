package com.bytebabies.app.ui.screens

import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.bytebabies.app.data.Repo
import com.bytebabies.app.model.*
import com.bytebabies.app.navigation.Route
import com.bytebabies.app.ui.components.*
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun AdminHome(nav: androidx.navigation.NavHostController) {
    val ctx = LocalContext.current
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopBar(
                title = "Admin Dashboard",
                showSwitch = true,
                onSwitch = {
                    // Clear in-memory auth state and go to Login
                    Repo.currentRole = null
                    Repo.currentParentId = null
                    nav.navigate(Route.Login.r) {
                        popUpTo(Route.Login.r) { inclusive = true }
                    }
                    Toast.makeText(ctx, "Switched profile", Toast.LENGTH_SHORT).show()
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                TextButton(onClick = { nav.navigate(Route.AdminParents.r) }) { Text("Parents") }
                TextButton(onClick = { nav.navigate(Route.AdminChildren.r) }) { Text("Children") }
                TextButton(onClick = { nav.navigate(Route.AdminTeachers.r) }) { Text("Teachers") }
                TextButton(onClick = { nav.navigate(Route.Attendance.r) }) { Text("Attendance") }
            }
        }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            SectionHeader("Manage")
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FeatureCard(
                    title = "Events",
                    description = "Create / notify parents",
                    onClick = { nav.navigate(Route.AdminEvents.r) },
                    gradient = bbGradient(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.secondaryContainer)
                )
                FeatureCard(
                    title = "Meals",
                    description = "Menu & dietary info",
                    onClick = { nav.navigate(Route.AdminMeals.r) },
                    gradient = bbGradient(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.primaryContainer)
                )
                FeatureCard(
                    title = "Media Uploads",
                    description = "Share photos / videos",
                    onClick = { nav.navigate(Route.AdminMedia.r) },
                    gradient = bbGradient(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.tertiaryContainer)
                )
                FeatureCard(
                    title = "Messages",
                    description = "Announcements & inbox",
                    onClick = { nav.navigate(Route.AdminMessages.r) },
                    gradient = bbGradient(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primary)
                )
            }

            Spacer(Modifier.height(16.dp))
            Text("You have ${Repo.messages.count { it.toAdmin }} parent messages.", color = androidx.compose.ui.graphics.Color.Gray)
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                Repo.currentRole = null
                Repo.currentParentId = null
                Toast.makeText(ctx, "Signed out", Toast.LENGTH_SHORT).show()
                // Optional: also return to Login on sign out
                nav.navigate(Route.Login.r) {
                    popUpTo(Route.Login.r) { inclusive = true }
                }
            }) { Text("Sign Out") }
        }
    }
}

@Composable
fun AdminParentsScreen() {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TopBar("Admin • Parents") }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            SectionHeader("Add Parent")
            OutlinedTextFieldFull(name, { name = it }, "Name")
            OutlinedTextFieldFull(email, { email = it }, "Email")
            OutlinedTextFieldFull(phone, { phone = it }, "Phone", androidx.compose.ui.text.input.KeyboardType.Phone)
            Row {
                Button(onClick = {
                    if (name.isNotBlank() && email.isNotBlank()) {
                        Repo.parents.add(Parent(name = name, email = email, phone = phone))
                        name = ""; email = ""; phone = ""
                    }
                }) { Text("Save") }
            }
            SectionHeader("All Parents")
            LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                items(Repo.parents) { p ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), shape = MaterialTheme.shapes.large) {
                        ListItem(
                            headlineContent = { Text(p.name) },
                            supportingContent = { Text("${p.email} • ${p.phone}") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminChildrenScreen() {
    var name by remember { mutableStateOf("") }
    var allergies by remember { mutableStateOf("") }
    var medical by remember { mutableStateOf("") }
    var parentIdx by remember { mutableStateOf(0) }
    var teacherIdx by remember { mutableStateOf(0) }
    val parentList = Repo.parents
    val teacherList = Repo.teachers

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TopBar("Admin • Children") }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            SectionHeader("Add Child")
            OutlinedTextFieldFull(name, { name = it }, "Child Name")
            OutlinedTextFieldFull(allergies, { allergies = it }, "Allergies")
            OutlinedTextFieldFull(medical, { medical = it }, "Medical Notes")
            Spacer(Modifier.height(8.dp))
            Text("Assign Parent & Teacher", fontWeight = FontWeight.SemiBold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Dropdown("Parent", parentList.map { it.name }, parentIdx) { parentIdx = it }
                Dropdown("Teacher", teacherList.map { it.name }, teacherIdx) { teacherIdx = it }
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                if (name.isNotBlank() && parentList.isNotEmpty() && teacherList.isNotEmpty()) {
                    Repo.children.add(
                        Child(
                            name = name,
                            parentId = parentList[parentIdx].id,
                            teacherId = teacherList[teacherIdx].id,
                            allergies = allergies,
                            medicalNotes = medical
                        )
                    )
                    name = ""; allergies = ""; medical = ""
                }
            }) { Text("Save") }

            SectionHeader("All Children")
            LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                items(Repo.children) { c ->
                    val parent = Repo.parents.find { it.id == c.parentId }?.name ?: "—"
                    val teacher = Repo.teachers.find { it.id == c.teacherId }?.name ?: "—"
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), shape = MaterialTheme.shapes.large) {
                        ListItem(
                            headlineContent = { Text(c.name) },
                            supportingContent = { Text("Parent: $parent • Teacher: $teacher") },
                            trailingContent = {
                                var show by remember { mutableStateOf(false) }
                                TextButton(onClick = { show = true }) { Text("Reassign") }
                                if (show) ReassignDialog(c) { show = false }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminTeachersScreen() {
    var name by remember { mutableStateOf("") }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TopBar("Admin • Teachers") }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            SectionHeader("Add Teacher")
            OutlinedTextFieldFull(name, { name = it }, "Name")
            Button(onClick = { if (name.isNotBlank()) { Repo.teachers.add(Teacher(name = name)); name = "" } }) { Text("Save") }

            SectionHeader("All Teachers")
            LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                items(Repo.teachers) { t ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), shape = MaterialTheme.shapes.large) {
                        ListItem(headlineContent = { Text(t.name) })
                    }
                }
            }
        }
    }
}

@Composable
fun AdminEventsScreen() {
    val ctx = LocalContext.current
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var dateStr by remember { mutableStateOf(LocalDate.now().plusDays(1).toString()) }
    var loc by remember { mutableStateOf("Hall") }
    val activity = LocalActivity.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TopBar("Admin • Events") }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            SectionHeader("Create Event")
            OutlinedTextFieldFull(title, { title = it }, "Title")
            OutlinedTextFieldFull(desc, { desc = it }, "Description")
            OutlinedTextFieldFull(dateStr, { dateStr = it }, "Date (YYYY-MM-DD)")
            OutlinedTextFieldFull(loc, { loc = it }, "Location")
            Row {
                Button(onClick = {
                    runCatching { LocalDate.parse(dateStr) }.onSuccess { d ->
                        Repo.events.add(Event(title = title, description = desc, date = d, location = loc))
                        title = ""; desc = ""; dateStr = LocalDate.now().plusDays(1).toString(); loc = ""
                        Toast.makeText(ctx, "Event created & parents notified (in-app).", Toast.LENGTH_SHORT).show()
                    }
                }) { Text("Save & Notify") }
            }

            SectionHeader("All Events")
            LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
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
}

@Composable
fun AdminMealsScreen() {
    var name by remember { mutableStateOf("") }
    var ing by remember { mutableStateOf("") }
    var diet by remember { mutableStateOf("") }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TopBar("Admin • Meals") }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            SectionHeader("Add Meal")
            OutlinedTextFieldFull(name, { name = it }, "Meal name")
            OutlinedTextFieldFull(ing, { ing = it }, "Ingredients")
            OutlinedTextFieldFull(diet, { diet = it }, "Dietary info")
            Button(onClick = {
                if (name.isNotBlank()) {
                    Repo.meals.add(Meal(name = name, ingredients = ing, dietaryInfo = diet))
                    name = ""; ing = ""; diet = ""
                }
            }) { Text("Save") }

            SectionHeader("All Meals")
            LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                items(Repo.meals) { m ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), shape = MaterialTheme.shapes.large) {
                        ListItem(
                            headlineContent = { Text(m.name) },
                            supportingContent = { Text("${m.ingredients} • ${m.dietaryInfo}") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminMediaScreen() {
    val ctx = LocalContext.current
    var caption by remember { mutableStateOf("") }
    var pickUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        pickUri = uri
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TopBar("Admin • Media Upload") }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            SectionHeader("Select Image/Video & Caption")
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Button(onClick = { launcher.launch("*/*") }) { Text("Pick File") }
                Spacer(Modifier.width(8.dp))
                Text(pickUri?.lastPathSegment ?: "No file selected", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            }
            OutlinedTextFieldFull(caption, { caption = it }, "Caption")

            Button(onClick = {
                pickUri?.let {
                    Repo.media.add(MediaItem(uri = it.toString(), uploadedBy = "admin", caption = caption))
                    caption = ""; pickUri = null
                    Toast.makeText(ctx, "Uploaded.", Toast.LENGTH_SHORT).show()
                }
            }) { Text("Upload") }

            SectionHeader("All Media")
            LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                items(Repo.media) { m ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), shape = MaterialTheme.shapes.large) {
                        ListItem(
                            headlineContent = { Text(m.caption.ifBlank { "Media" }) },
                            supportingContent = { Text(m.uri, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminMessagesScreen() {
    var announcement by remember { mutableStateOf("") }
    val now = java.time.LocalDateTime.now()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TopBar("Admin • Messages") }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            SectionHeader("Broadcast Announcement")
            OutlinedTextFieldFull(announcement, { announcement = it }, "Message to all parents")
            Button(onClick = {
                Repo.messages.add(Message(fromParentId = null, toAdmin = false, content = "ANNOUNCEMENT @ $now: $announcement"))
                announcement = ""
            }) { Text("Send") }

            SectionHeader("Inbox (from Parents)")
            LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                items(Repo.messages.filter { it.toAdmin }.sortedByDescending { it.timestamp }) { msg ->
                    val parentName = Repo.parents.find { it.id == msg.fromParentId }?.name ?: "Unknown Parent"
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), shape = MaterialTheme.shapes.large) {
                        ListItem(
                            headlineContent = { Text(parentName) },
                            supportingContent = { Text(msg.content) },
                            trailingContent = { Text(msg.timestamp.toLocalTime().toString(), color = androidx.compose.ui.graphics.Color.Gray) }
                        )
                    }
                }
            }
        }
    }
}
