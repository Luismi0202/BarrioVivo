package com.example.barriovivo.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

data class User(
    val id: String = "",
    val email: String = "",
    val password: String = "",
    val location: Location = Location(),
    val role: UserRole = UserRole.USER,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class UserRole {
    ADMIN, USER
}

data class Location(
    val city: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val country: String = "Spain",
    val zipCode: String = ""
)

data class MealPost(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val title: String = "",
    val description: String = "",
    val photoUri: String = "",
    val expiryDate: LocalDate = LocalDate.now(),
    val location: Location = Location(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val status: MealPostStatus = MealPostStatus.PENDING,
    val adminComment: String = ""
)

enum class MealPostStatus {
    PENDING, APPROVED, REJECTED
}

data class Notification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: NotificationType = NotificationType.INFO,
    val relatedPostId: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val isRead: Boolean = false
)

enum class NotificationType {
    POST_APPROVED, POST_REJECTED, NEW_NEARBY_POST, INFO
}

data class Admin(
    val id: String = "",
    val email: String = "",
    val userId: String = ""
)

