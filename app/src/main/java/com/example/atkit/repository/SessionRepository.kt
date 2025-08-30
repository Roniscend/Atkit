package com.example.atkit.repository

import com.example.atkit.data.SessionDao
import com.example.atkit.data.SessionEntity
import kotlinx.coroutines.flow.Flow

class SessionRepository(private val sessionDao: SessionDao) {
    fun getAllSessions(): Flow<List<SessionEntity>> = sessionDao.getAllSessions()

    suspend fun getSessionById(sessionId: String): SessionEntity? =
        sessionDao.getSessionById(sessionId)

    suspend fun insertSession(session: SessionEntity) = sessionDao.insertSession(session)

    suspend fun updateSession(session: SessionEntity) = sessionDao.updateSession(session)

    suspend fun deleteSession(session: SessionEntity) = sessionDao.deleteSession(session)

    fun searchSessions(query: String): Flow<List<SessionEntity>> =
        sessionDao.searchSessions(query)
}
