package com.bytebabies.app.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class Parent(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var email: String,
    var phone: String,
    var consentMedia: Boolean = false
)

data class Teacher(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
)

data class Child(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var parentId: String,
    var teacherId: String,
    var allergies: String = "",
    var medicalNotes: String = ""
)

data class Event(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var description: String,
    var date: LocalDate,
    var location: String
)

data class AttendanceRecord(
    val id: String = UUID.randomUUID().toString(),
    val childId: String,
    val date: LocalDate,
    var present: Boolean
)

data class Message(
    val id: String = UUID.randomUUID().toString(),
    val fromParentId: String?,
    val toAdmin: Boolean,
    val content: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

data class MediaItem(
    val id: String = UUID.randomUUID().toString(),
    val uri: String,
    val uploadedBy: String,
    val childId: String? = null,
    val caption: String = ""
)

data class Meal(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val ingredients: String,
    val dietaryInfo: String
)

data class MealOrder(
    val id: String = UUID.randomUUID().toString(),
    val childId: String,
    val mealId: String,
    val date: LocalDate
)

enum class Role { ADMIN, PARENT }
