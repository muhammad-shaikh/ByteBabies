package com.bytebabies.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bytebabies.app.data.Repo
import com.bytebabies.app.model.Role
import com.bytebabies.app.navigation.Route
import com.bytebabies.app.ui.components.OutlinedTextFieldFull
import com.bytebabies.app.ui.components.TopBar

@Composable
fun LoginScreen(nav: NavHostController) {
    var role by remember { mutableStateOf(Role.PARENT) }
    var parentEmail by remember { mutableStateOf("ayesha@example.com") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    val isParent = role == Role.PARENT

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TopBar("Sign In") }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("ByteBabies", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text("Choose a role and sign in")

            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                FilterChip(selected = role == Role.ADMIN, onClick = { role = Role.ADMIN }, label = { Text("Admin") })
                FilterChip(selected = role == Role.PARENT, onClick = { role = Role.PARENT }, label = { Text("Parent") })
            }

            Spacer(Modifier.height(16.dp))
            if (isParent) {
                OutlinedTextFieldFull(parentEmail, { parentEmail = it }, "Parent Email")
                OutlinedTextFieldFull(phone, { phone = it }, "Phone")
            } else {
                OutlinedTextFieldFull("admin@bytebabies.local", {}, "Admin Email (fixed)")
                OutlinedTextFieldFull(password, { password = it }, "Password")
            }

            Spacer(Modifier.height(16.dp))
            Button(modifier = Modifier.fillMaxWidth(), onClick = {
                if (isParent) {
                    val p = Repo.parents.find { it.email.equals(parentEmail.trim(), ignoreCase = true) }
                    if (p != null) {
                        Repo.currentRole = Role.PARENT
                        Repo.currentParentId = p.id
                        nav.navigate(Route.ParentHome.r) { popUpTo(Route.Login.r) { inclusive = true } }
                    }
                } else {
                    Repo.currentRole = Role.ADMIN
                    Repo.currentParentId = null
                    nav.navigate(Route.AdminHome.r) { popUpTo(Route.Login.r) { inclusive = true } }
                }
            }) { Text("Sign In") }

            Spacer(Modifier.height(12.dp))
            Text("Tip: Set a Strong Password.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}
