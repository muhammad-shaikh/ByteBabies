package com.bytebabies.app.navigation

sealed class Route(val r: String) {

    // ---------- Auth ----------
    data object Login : Route("login")

    // ---------- Admin ----------
    data object AdminHome : Route("admin_home")
    data object AdminParents : Route("admin_parents")
    data object AdminChildren : Route("admin_children")
    data object AdminTeachers : Route("admin_teachers")
    data object AdminEvents : Route("admin_events")
    data object AdminMeals : Route("admin_meals")
    data object AdminMedia : Route("admin_media")
    data object AdminMessages : Route("admin_messages")

    // ---------- Shared ----------
    data object Attendance : Route("attendance")
    data object Payments : Route("payments")

    // ---------- Parent ----------
    data object ParentHome : Route("parent_home")
    data object ParentMessages : Route("parent_messages")
    data object ParentEvents : Route("parent_events")
    data object ParentAttendance : Route("parent_attendance")
    data object ParentMeals : Route("parent_meals")
    data object ParentMedia : Route("parent_media")
    data object ParentProfile : Route("parent_profile")

    // ---------- New ----------
    /** Route for PayFast sandbox checkout WebView screen **/
    data object PayfastCheckout : Route("payfast_checkout")
}
