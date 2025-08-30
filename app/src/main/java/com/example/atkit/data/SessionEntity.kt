package com.example.atkit.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey
    val sessionId: String,
    val name: String,
    val age: Int,
    val timestamp: Long,
    val imageCount: Int = 0
)
