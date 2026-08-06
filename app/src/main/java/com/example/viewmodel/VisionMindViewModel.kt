package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.*
import com.example.network.GeminiClient
import com.example.network.GeminiContent
import com.example.network.GeminiPart
import com.example.network.GeminiRequest
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VisionMindViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val dao = database.conceptDao()
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    // UI Navigation State
    private val _currentScreen = MutableStateFlow("GRAPH") // GRAPH, STUDY, CHAT, GAPS, PATHS
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Database loaded state
    private val _concepts = MutableStateFlow<List<ConceptEntity>>(emptyList())
    val concepts: StateFlow<List<ConceptEntity>> = _concepts.asStateFlow()

    private val _relationships = MutableStateFlow<List<ConceptRelationshipEntity>>(emptyList())
    val relationships: StateFlow<List<ConceptRelationshipEntity>> = _relationships.asStateFlow()

    private val _progress = MutableStateFlow<Map<String, UserProgressEntity>>(emptyMap())
    val progress: StateFlow<Map<String, UserProgressEntity>> = _progress.asStateFlow()

    // Selection & Explanations state
    private val _selectedConcept = MutableStateFlow<ConceptEntity?>(null)
    val selectedConcept: StateFlow<ConceptEntity?> = _selectedConcept.asStateFlow()

    private val _explanationLevel = MutableStateFlow("Intermediate") // Beginner, Intermediate, Advanced
    val explanationLevel: StateFlow<String> = _explanationLevel.asStateFlow()

    private val _customAiExplanation = MutableStateFlow<String?>(null)
    val customAiExplanation: StateFlow<String?> = _customAiExplanation.asStateFlow()

    private val _isGeneratingAiExplanation = MutableStateFlow(false)
    val isGeneratingAiExplanation: StateFlow<Boolean> = _isGeneratingAiExplanation.asStateFlow()

    // Chat history state
    private val _chatMessages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessageEntity>> = _chatMessages.asStateFlow()

    private val _isSendingChatMessage = MutableStateFlow(false)
    val isSendingChatMessage: StateFlow<Boolean> = _isSendingChatMessage.asStateFlow()

    // Active Quiz state
    private val _activeQuizQuestions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val activeQuizQuestions: StateFlow<List<QuizQuestion>> = _activeQuizQuestions.asStateFlow()

    private val _currentQuizIndex = MutableStateFlow(0)
    val currentQuizIndex: StateFlow<Int> = _currentQuizIndex.asStateFlow()

    private val _quizSelectedOption = MutableStateFlow(-1)
    val quizSelectedOption: StateFlow<Int> = _quizSelectedOption.asStateFlow()

    private val _quizSubmitted = MutableStateFlow(false)
    val quizSubmitted: StateFlow<Boolean> = _quizSubmitted.asStateFlow()

    private val _quizCorrectAnswers = MutableStateFlow(0)
    val quizCorrectAnswers: StateFlow<Int> = _quizCorrectAnswers.asStateFlow()

    private val _quizFinished = MutableStateFlow(false)
    val quizFinished: StateFlow<Boolean> = _quizFinished.asStateFlow()

    // Gap analysis & personalized learning paths states
    private val _detectedGaps = MutableStateFlow<List<GapReport>>(emptyList())
    val detectedGaps: StateFlow<List<GapReport>> = _detectedGaps.asStateFlow()

    private val _customLearningPathGoal = MutableStateFlow("")
    val customLearningPathGoal: StateFlow<String> = _customLearningPathGoal.asStateFlow()

    private val _aiLearningPathResponse = MutableStateFlow<String?>(null)
    val aiLearningPathResponse: StateFlow<String?> = _aiLearningPathResponse.asStateFlow()

    private val _isGeneratingLearningPath = MutableStateFlow(false)
    val isGeneratingLearningPath: StateFlow<Boolean> = _isGeneratingLearningPath.asStateFlow()

    init {
        // Seed and load data
        viewModelScope.launch(Dispatchers.IO) {
            val existing = dao.getAllConcepts()
            if (existing.isEmpty()) {
                Log.d("VisionMind", "Database is empty. Seeding database with concepts and relationships...")
                dao.insertConcepts(InitialData.concepts)
                dao.insertRelationships(InitialData.relationships)
            }
            
            // Collect live updates from database
            launch {
                dao.getAllConceptsFlow().collectLatest {
                    _concepts.value = it
                    if (_selectedConcept.value == null && it.isNotEmpty()) {
                        _selectedConcept.value = it.first()
                        loadQuizForConcept(it.first())
                    }
                }
            }

            launch {
                dao.getAllRelationshipsFlow().collectLatest {
                    _relationships.value = it
                }
            }

            launch {
                dao.getAllProgressFlow().collectLatest { progressList ->
                    _progress.value = progressList.associateBy { it.conceptId }
                    analyzeGaps()
                }
            }

            // Sync chat history whenever selected concept changes
            launch {
                _selectedConcept.collectLatest { concept ->
                    concept?.let {
                        dao.getChatMessagesFlow(it.id).collectLatest { msgs ->
                            _chatMessages.value = msgs
                        }
                    }
                }
            }
        }
    }

    fun selectConcept(concept: ConceptEntity) {
        _selectedConcept.value = concept
        _customAiExplanation.value = null
        _quizFinished.value = false
        _currentQuizIndex.value = 0
        _quizSelectedOption.value = -1
        _quizSubmitted.value = false
        _quizCorrectAnswers.value = 0
        loadQuizForConcept(concept)
    }

    fun setExplanationLevel(level: String) {
        _explanationLevel.value = level
        _customAiExplanation.value = null
    }

    fun setScreen(screen: String) {
        _currentScreen.value = screen
    }

    private fun loadQuizForConcept(concept: ConceptEntity) {
        try {
            val type = Types.newParameterizedType(List::class.java, QuizQuestion::class.java)
            val list: List<QuizQuestion>? = moshi.adapter<List<QuizQuestion>>(type).fromJson(concept.quizJson)
            _activeQuizQuestions.value = list ?: emptyList()
        } catch (e: Exception) {
            Log.e("VisionMind", "Error parsing quiz JSON for ${concept.title}", e)
            _activeQuizQuestions.value = emptyList()
        }
    }

    fun selectQuizOption(index: Int) {
        if (!_quizSubmitted.value) {
            _quizSelectedOption.value = index
        }
    }

    fun submitQuizAnswer() {
        if (_quizSelectedOption.value == -1 || _quizSubmitted.value) return
        _quizSubmitted.value = true
        val currentQuestion = _activeQuizQuestions.value.getOrNull(_currentQuizIndex.value) ?: return
        if (_quizSelectedOption.value == currentQuestion.correctOptionIndex) {
            _quizCorrectAnswers.value += 1
        }
    }

    fun nextQuizQuestion() {
        val total = _activeQuizQuestions.value.size
        val nextIdx = _currentQuizIndex.value + 1
        if (nextIdx < total) {
            _currentQuizIndex.value = nextIdx
            _quizSelectedOption.value = -1
            _quizSubmitted.value = false
        } else {
            _quizFinished.value = true
            // Save progress to database
            saveProgressToDb()
        }
    }

    private fun saveProgressToDb() {
        val concept = _selectedConcept.value ?: return
        val scorePercent = if (_activeQuizQuestions.value.isNotEmpty()) {
            (_quizCorrectAnswers.value * 100) / _activeQuizQuestions.value.size
        } else {
            100
        }
        val status = if (scorePercent >= 70) "MASTERED" else "IN_PROGRESS"

        viewModelScope.launch(Dispatchers.IO) {
            val existing = dao.getProgressForConcept(concept.id)
            val updatedNotes = existing?.userNotes ?: ""
            dao.insertProgress(
                UserProgressEntity(
                    conceptId = concept.id,
                    status = status,
                    quizScore = maxOf(existing?.quizScore ?: 0, scorePercent),
                    userNotes = updatedNotes,
                    lastStudiedTime = System.currentTimeMillis()
                )
            )
        }
    }

    fun updateProgressStatus(conceptId: String, status: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = dao.getProgressForConcept(conceptId)
            dao.insertProgress(
                UserProgressEntity(
                    conceptId = conceptId,
                    status = status,
                    quizScore = existing?.quizScore ?: 0,
                    userNotes = existing?.userNotes ?: "",
                    lastStudiedTime = System.currentTimeMillis()
                )
            )
        }
    }

    fun saveUserNotes(notes: String) {
        val concept = _selectedConcept.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val existing = dao.getProgressForConcept(concept.id)
            dao.insertProgress(
                UserProgressEntity(
                    conceptId = concept.id,
                    status = existing?.status ?: "IN_PROGRESS",
                    quizScore = existing?.quizScore ?: 0,
                    userNotes = notes,
                    lastStudiedTime = System.currentTimeMillis()
                )
            )
        }
    }

    // --- GAP ANALYZER ---
    private fun analyzeGaps() {
        val allConcepts = _concepts.value
        val allRels = _relationships.value
        val userProg = _progress.value

        val reports = mutableListOf<GapReport>()

        for (concept in allConcepts) {
            val prog = userProg[concept.id]
            if (prog?.status == "MASTERED" || prog?.status == "IN_PROGRESS") {
                // Check prerequisites
                val prereqs = allRels.filter { it.toId == concept.id }
                val unmasteredPrereqs = mutableListOf<ConceptEntity>()
                for (rel in prereqs) {
                    val pProg = userProg[rel.fromId]
                    if (pProg?.status != "MASTERED") {
                        val prConcept = allConcepts.find { it.id == rel.fromId }
                        if (prConcept != null) {
                            unmasteredPrereqs.add(prConcept)
                        }
                    }
                }

                if (unmasteredPrereqs.isNotEmpty()) {
                    reports.add(
                        GapReport(
                            targetConcept = concept,
                            missingPrerequisites = unmasteredPrereqs,
                            remedialAdvice = "You are currently studying '${concept.title}', but you haven't mastered its key mathematical or conceptual prerequisites: ${unmasteredPrereqs.joinToString { it.title }}. We recommend reviewing these foundations to fully grasp the derivative structures, proofs, or algorithms of '${concept.title}'."
                        )
                    )
                }
            }
        }
        _detectedGaps.value = reports
    }

    // --- GEMINI CORE CALLS ---
    fun askTutor(messageText: String) {
        val concept = _selectedConcept.value ?: return
        val currentMsgs = _chatMessages.value
        val apiKey = BuildConfig.GEMINI_API_KEY

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            // Save placeholder or error message locally
            viewModelScope.launch {
                dao.insertChatMessage(ChatMessageEntity(conceptId = concept.id, sender = "USER", message = messageText, timestamp = System.currentTimeMillis()))
                dao.insertChatMessage(ChatMessageEntity(conceptId = concept.id, sender = "AI", message = "API Key not configured. Please enter a valid Gemini API Key in AI Studio's Secrets panel to converse with the VisionMind Tutor.", timestamp = System.currentTimeMillis() + 100))
            }
            return
        }

        _isSendingChatMessage.value = true

        viewModelScope.launch(Dispatchers.IO) {
            // Store user message
            val userMsg = ChatMessageEntity(
                conceptId = concept.id,
                sender = "USER",
                message = messageText,
                timestamp = System.currentTimeMillis()
            )
            dao.insertChatMessage(userMsg)

            try {
                // Construct System prompt + Chat context
                val systemContext = """
                    You are VisionMind AI, an expert AI tutor, senior researcher, software engineer, and mentor specializing in Computer Vision (CV), Machine Learning (ML), Deep Learning (DL), Mathematics, Signal Processing, Linear Algebra, Calculus, Probability, Geometry, and Optimization.
                    The user is currently studying the topic: "${concept.title}" (Category: ${concept.category}, Difficulty: ${concept.difficulty}).
                    Here is the core material of this topic for your reference:
                    - Intuitive Explanation: ${concept.intuitiveExplanationIntermediate}
                    - Mathematical formulation: ${concept.formalMath}
                    - Python/PyTorch/OpenCV Implementation: ${concept.pythonCode}
                    
                    Respond as an elegant, precise, and conversational mentor. Provide formal mathematical proofs, clear step-by-step logic, code debugs, or direct answers. Always maintain context with previous questions and relate back to the broader computer vision ecosystem. Avoid fluff.
                """.trimIndent()

                val apiContents = mutableListOf<GeminiContent>()
                apiContents.add(GeminiContent(parts = listOf(GeminiPart(systemContext)), role = "user"))
                apiContents.add(GeminiContent(parts = listOf(GeminiPart("Understood. I will act as the VisionMind AI expert mentor and specialize my advice around ${concept.title}. Ask me anything.")), role = "model"))

                // Map previous 10 messages for conversational memory
                currentMsgs.takeLast(10).forEach { msg ->
                    val role = if (msg.sender == "USER") "user" else "model"
                    apiContents.add(GeminiContent(parts = listOf(GeminiPart(msg.message)), role = role))
                }

                // Append active question
                apiContents.add(GeminiContent(parts = listOf(GeminiPart(messageText)), role = "user"))

                val response = GeminiClient.api.generateContent(apiKey, GeminiRequest(contents = apiContents))
                val aiResponseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "VisionMind AI is offline. Could you please rephrase or try again?"

                dao.insertChatMessage(
                    ChatMessageEntity(
                        conceptId = concept.id,
                        sender = "AI",
                        message = aiResponseText,
                        timestamp = System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                Log.e("VisionMind", "Error calling Gemini API", e)
                dao.insertChatMessage(
                    ChatMessageEntity(
                        conceptId = concept.id,
                        sender = "AI",
                        message = "Error: Failed to fetch response from Gemini Tutor. Check your internet connection or API Key. Details: ${e.localizedMessage}",
                        timestamp = System.currentTimeMillis()
                    )
                )
            } finally {
                _isSendingChatMessage.value = false
            }
        }
    }

    fun clearChatHistory() {
        val concept = _selectedConcept.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            dao.clearChatHistory(concept.id)
        }
    }

    fun generateCustomLearningPath(goal: String) {
        _customLearningPathGoal.value = goal
        val apiKey = BuildConfig.GEMINI_API_KEY

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            _aiLearningPathResponse.value = "API Key not configured. Please enter a valid Gemini API Key in the AI Studio Secrets panel to generate a custom learning path."
            return
        }

        _isGeneratingLearningPath.value = true
        _aiLearningPathResponse.value = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val availableConceptTitles = _concepts.value.joinToString { it.title }
                val prompt = """
                    I want to build a career or complete a project based on this goal: "$goal".
                    
                    As the expert VisionMind AI Mentor, design a personalized step-by-step Learning Path to achieve this goal.
                    We have a local knowledge graph consisting of these core topics: $availableConceptTitles.
                    
                    Please structure your learning path as follows:
                    1. **Curated Pathway**: Map out a sequence of 3-5 learning phases. Explain how to leverage our local concepts (e.g. SVD, Sobel, Epipolar Geometry, Backprop, CNNs) or state which external topics (like Kalman Filters, NeRFs, or Transformers) must be added as prerequisites.
                    2. **Real-world Milestones**: What concrete micro-projects can be built at each stage.
                    3. **Architectural Blueprints**: High-level production architecture or math flow recommended to implement the final goal.
                    
                    Keep the tone scientific, highly professional, encouraging, and clear.
                """.trimIndent()

                val apiContents = listOf(
                    GeminiContent(parts = listOf(GeminiPart(prompt)), role = "user")
                )

                val response = GeminiClient.api.generateContent(apiKey, GeminiRequest(contents = apiContents))
                val pathText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "Failed to generate custom pathway. Try refining your goal statement."

                _aiLearningPathResponse.value = pathText
            } catch (e: Exception) {
                Log.e("VisionMind", "Error generating path", e)
                _aiLearningPathResponse.value = "Failed to connect to the research server. Please check your network and API key setup. Exception: ${e.localizedMessage}"
            } finally {
                _isGeneratingLearningPath.value = false
            }
        }
    }

    fun customizeActiveExplanation(customRequirement: String) {
        val concept = _selectedConcept.value ?: return
        val currentLevel = _explanationLevel.value
        val apiKey = BuildConfig.GEMINI_API_KEY

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            _customAiExplanation.value = "Please enter a valid Gemini API Key in AI Studio's Secrets panel to adapt explanations dynamically."
            return
        }

        _isGeneratingAiExplanation.value = true
        _customAiExplanation.value = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val baseExp = when (currentLevel) {
                    "Beginner" -> concept.intuitiveExplanationBeginner
                    "Advanced" -> concept.intuitiveExplanationAdvanced
                    else -> concept.intuitiveExplanationIntermediate
                }

                val prompt = """
                    You are VisionMind AI. Adapt the tutorial for "${concept.title}" according to this specific user request: "$customRequirement".
                    
                    Here is the base content we are starting with:
                    $baseExp
                    
                    Math reference for this concept:
                    ${concept.formalMath}
                    
                    Please rewrite or supplement the tutorial to fully address the user's custom prompt. You can simplify, provide alternative geometric analogies, explain line-by-line PyTorch syntax, or detail the mathematical proof. Maintain complete scientific accuracy, formatted elegantly with scannable bullet points and bold headers.
                """.trimIndent()

                val apiContents = listOf(
                    GeminiContent(parts = listOf(GeminiPart(prompt)), role = "user")
                )

                val response = GeminiClient.api.generateContent(apiKey, GeminiRequest(contents = apiContents))
                val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "Could not customize explanation. Please try again."

                _customAiExplanation.value = responseText
            } catch (e: Exception) {
                Log.e("VisionMind", "Error customizing explanation", e)
                _customAiExplanation.value = "Failed to generate custom tutoring content. Exception: ${e.localizedMessage}"
            } finally {
                _isGeneratingAiExplanation.value = false
            }
        }
    }

    fun resetCustomExplanation() {
        _customAiExplanation.value = null
    }
}

data class GapReport(
    val targetConcept: ConceptEntity,
    val missingPrerequisites: List<ConceptEntity>,
    val remedialAdvice: String
)

class VisionMindViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VisionMindViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VisionMindViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
