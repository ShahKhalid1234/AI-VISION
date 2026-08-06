package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ConceptDao {
    @Query("SELECT * FROM concepts")
    fun getAllConceptsFlow(): Flow<List<ConceptEntity>>

    @Query("SELECT * FROM concepts")
    suspend fun getAllConcepts(): List<ConceptEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConcepts(concepts: List<ConceptEntity>)

    @Query("SELECT * FROM concept_relationships")
    fun getAllRelationshipsFlow(): Flow<List<ConceptRelationshipEntity>>

    @Query("SELECT * FROM concept_relationships")
    suspend fun getAllRelationships(): List<ConceptRelationshipEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelationships(relationships: List<ConceptRelationshipEntity>)

    @Query("SELECT * FROM user_progress")
    fun getAllProgressFlow(): Flow<List<UserProgressEntity>>

    @Query("SELECT * FROM user_progress WHERE conceptId = :conceptId")
    suspend fun getProgressForConcept(conceptId: String): UserProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: UserProgressEntity)

    @Query("SELECT * FROM chat_history WHERE conceptId = :conceptId OR :conceptId IS NULL ORDER BY timestamp ASC")
    fun getChatMessagesFlow(conceptId: String?): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_history WHERE conceptId = :conceptId OR :conceptId IS NULL")
    suspend fun clearChatHistory(conceptId: String?)
}
