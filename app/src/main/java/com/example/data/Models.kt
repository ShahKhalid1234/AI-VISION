package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "concepts")
data class ConceptEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String, // "Mathematics", "Signal Processing", "Classical CV", "Deep Learning", "Optimization", "Geometry"
    val shortDesc: String,
    val difficulty: String, // "Beginner", "Intermediate", "Advanced"
    val intuitiveExplanationBeginner: String,
    val intuitiveExplanationIntermediate: String,
    val intuitiveExplanationAdvanced: String,
    val formalMath: String,
    val proofSketch: String,
    val realWorldApps: String,
    val pythonCode: String,
    val diagramMermaid: String,
    val researchPapersJson: String, // JSON array of ResearchPaper
    val quizJson: String, // JSON array of QuizQuestion
    val interviewQuestionsJson: String, // JSON array of InterviewQuestion
    val xPos: Float, // coordinates for our interactive graph
    val yPos: Float
)

@Entity(tableName = "concept_relationships")
data class ConceptRelationshipEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fromId: String,
    val toId: String,
    val relationType: String // "PREREQUISITE"
)

@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey val conceptId: String,
    val status: String, // "NOT_STARTED", "IN_PROGRESS", "MASTERED"
    val quizScore: Int, // Max quiz score achieved (e.g. out of 100)
    val userNotes: String,
    val lastStudiedTime: Long
)

@Entity(tableName = "chat_history")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conceptId: String?, // Optional context
    val sender: String, // "USER" or "AI"
    val message: String,
    val timestamp: Long
)

// Auxiliary simple data structures for Moshi parsing
data class ResearchPaper(
    val title: String,
    val authors: String,
    val year: String,
    val importance: String,
    val url: String = ""
)

data class QuizQuestion(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val explanation: String
)

data class InterviewQuestion(
    val question: String,
    val answer: String
)
