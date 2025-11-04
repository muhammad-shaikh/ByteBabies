package com.bytebabies.app.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.bytebabies.app.model.*
import java.math.BigDecimal
import java.time.LocalDate

object Repo {
    // ---------------- In-memory "DB" ----------------
    val parents = mutableStateListOf<Parent>()
    val teachers = mutableStateListOf<Teacher>()
    val children = mutableStateListOf<Child>()
    val events = mutableStateListOf<Event>()
    val attendance = mutableStateListOf<AttendanceRecord>()
    val messages = mutableStateListOf<Message>()
    val media = mutableStateListOf<MediaItem>()
    val meals = mutableStateListOf<Meal>()
    val orders = mutableStateListOf<MealOrder>()

    // ---------------- Session / Auth-ish ----------------
    var currentRole by mutableStateOf<Role?>(null)
    var currentParentId by mutableStateOf<String?>(null)

    // ---------------- PayFast temp state (for checkout) ----------------
    // These are used by PaymentsScreen → PayfastWebViewScreen
    var tempPaymentAmount: BigDecimal? = null
    var tempPaymentChildName: String? = null

    fun clearTempPayment() {
        tempPaymentAmount = null
        tempPaymentChildName = null
    }

    // ---------------- Seed sample data ----------------
    fun seed() {
        if (teachers.isNotEmpty()) return

        val t1 = Teacher(name = "Ms Thandi")
        val t2 = Teacher(name = "Mr Dlamini")
        teachers.addAll(listOf(t1, t2))

        val p1 = Parent(
            name = "Ayesha Khan",
            email = "ayesha@example.com",
            phone = "0712345678",
            consentMedia = true
        )
        val p2 = Parent(
            name = "John Mokoena",
            email = "john@example.com",
            phone = "0798765432"
        )
        parents.addAll(listOf(p1, p2))

        val c1 = Child(
            name = "Zara Khan",
            parentId = p1.id,
            teacherId = t1.id,
            allergies = "Peanuts",
            medicalNotes = "Uses inhaler"
        )
        val c2 = Child(
            name = "Neo Mokoena",
            parentId = p2.id,
            teacherId = t2.id
        )
        children.addAll(listOf(c1, c2))

        events.addAll(
            listOf(
                Event(
                    title = "Sports Day",
                    description = "Bring hats and sunscreen",
                    date = LocalDate.now().plusDays(7),
                    location = "School Field"
                ),
                Event(
                    title = "Parents Meeting",
                    description = "Term planning",
                    date = LocalDate.now().plusDays(14),
                    location = "Hall"
                )
            )
        )

        meals.addAll(
            listOf(
                Meal(
                    name = "Chicken Pasta",
                    ingredients = "Chicken, pasta, tomato",
                    dietaryInfo = "Contains gluten"
                ),
                Meal(
                    name = "Veggie Wrap",
                    ingredients = "Tortilla, lettuce, tomato, beans",
                    dietaryInfo = "Vegetarian"
                ),
                Meal(
                    name = "Fruit Bowl",
                    ingredients = "Apples, bananas, grapes",
                    dietaryInfo = "Vegan, gluten-free"
                )
            )
        )
    }

    // ---------------- Helpers ----------------
    fun findChildrenOfParent(parentId: String) = children.filter { it.parentId == parentId }
    fun childName(childId: String) = children.find { it.id == childId }?.name ?: "Unknown"
    fun mealName(mealId: String) = meals.find { it.id == mealId }?.name ?: "Meal"

    fun markAttendance(childId: String, date: LocalDate, present: Boolean) {
        val existing = attendance.find { it.childId == childId && it.date == date }
        if (existing != null) {
            existing.present = present
        } else {
            attendance.add(AttendanceRecord(childId = childId, date = date, present = present))
        }
    }

    fun absentTodayForParent(parentId: String): List<Child> {
        val kids = findChildrenOfParent(parentId)
        return kids.filter { k ->
            val rec = attendance.find { it.childId == k.id && it.date == LocalDate.now() }
            rec?.present == false
        }
    }
}
