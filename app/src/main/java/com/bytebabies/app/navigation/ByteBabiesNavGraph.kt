package com.bytebabies.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bytebabies.app.ui.screens.*

@Composable
fun ByteBabiesNavGraph() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Route.Login.r) {

        // ---------- Auth ----------
        composable(Route.Login.r) { LoginScreen(nav) }

        // ---------- Admin area ----------
        composable(Route.AdminHome.r) { AdminHome(nav) }
        composable(Route.AdminParents.r) { AdminParentsScreen() }
        composable(Route.AdminChildren.r) { AdminChildrenScreen() }
        composable(Route.AdminTeachers.r) { AdminTeachersScreen() }
        composable(Route.AdminEvents.r) { AdminEventsScreen() }
        composable(Route.AdminMeals.r) { AdminMealsScreen() }
        composable(Route.AdminMedia.r) { AdminMediaScreen() }
        composable(Route.AdminMessages.r) { AdminMessagesScreen() }

        // ---------- Shared ----------
        composable(Route.Attendance.r) { AttendanceScreen() }

        // Pass nav so Payments can navigate to PayFast checkout
        composable(Route.Payments.r) { PaymentsScreen(nav) }

        // ---------- Parent area ----------
        composable(Route.ParentHome.r) { ParentHome(nav) }
        composable(Route.ParentMessages.r) { ParentMessagesScreen() }
        composable(Route.ParentEvents.r) { ParentEventsScreen() }
        composable(Route.ParentAttendance.r) { ParentAttendanceScreen() }
        composable(Route.ParentMeals.r) { ParentMealsScreen() }
        composable(Route.ParentMedia.r) { ParentMediaScreen() }
        composable(Route.ParentProfile.r) { ParentProfileScreen() }

        // ---------- PayFast Checkout (NEW) ----------
        composable(Route.PayfastCheckout.r) { PayfastWebViewScreen(nav) }
    }
}
