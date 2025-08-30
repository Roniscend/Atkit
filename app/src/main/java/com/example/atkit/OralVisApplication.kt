package com.example.atkit

import android.app.Application
import com.example.atkit.data.SessionDatabase
import com.example.atkit.repository.SessionRepository

class OralVisApplication : Application() {
    val database by lazy { SessionDatabase.getDatabase(this) }
    val repository by lazy { SessionRepository(database.sessionDao()) }
}
