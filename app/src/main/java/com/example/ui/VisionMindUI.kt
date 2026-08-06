package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.example.data.*
import com.example.viewmodel.VisionMindViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

// Top-tier Sci-Fi dark palette
val DarkBg = Color(0xFF0B0E14)
val CardBg = Color(0xFF151922)
val BorderColor = Color(0xFF222B3A)
val CyanAccent = Color(0xFF00F0FF)
val VioletAccent = Color(0xFF9D4EDD)
val AmberAccent = Color(0xFFFF9E00)
val RoseAccent = Color(0xFFFF2A6D)
val TextPrimary = Color(0xFFF0F4F8)
val TextSecondary = Color(0xFF94A3B8)

@Composable
fun VisionMindApp(viewModel: VisionMindViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val concepts by viewModel.concepts.collectAsState()
    val selectedConcept by viewModel.selectedConcept.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBg
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // High fidelity App Header
            AppHeader(
                currentScreen = currentScreen,
                onScreenSelected = { viewModel.setScreen(it) }
            )

            // Main view switcher
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                    },
                    label = "ScreenTransition"
                ) { screen ->
                    when (screen) {
                        "GRAPH" -> GraphExplorerScreen(viewModel)
                        "STUDY" -> StudyDeepDiveScreen(viewModel)
                        "CHAT" -> ChatTutorScreen(viewModel)
                        "GAPS" -> GapAnalyzerScreen(viewModel)
                        "PATHS" -> PathPlannerScreen(viewModel)
                    }
                }
            }

            // Bottom Navigation Bar (for compact/mobile view)
            AppBottomBar(
                currentScreen = currentScreen,
                onScreenSelected = { viewModel.setScreen(it) }
            )
        }
    }
}

@Composable
fun AppHeader(currentScreen: String, onScreenSelected: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = BorderStroke(1.dp, BorderColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Hub,
                    contentDescription = "VisionMind Logo",
                    tint = CyanAccent,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "VisionMind AI",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontFamily = FontFamily.SansSerif
                    )
                    Text(
                        text = "Knowledge Engine & CV Mentor",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            // Highlighting active panel quick metrics or warning states
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = { onScreenSelected("GAPS") },
                    modifier = Modifier
                        .background(
                            if (currentScreen == "GAPS") BorderColor else Color.Transparent,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.NewReleases,
                        contentDescription = "Knowledge Gaps Indicator",
                        tint = AmberAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AppBottomBar(currentScreen: String, onScreenSelected: (String) -> Unit) {
    NavigationBar(
        containerColor = CardBg,
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = BorderColor,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
    ) {
        val items = listOf(
            Triple("GRAPH", "Graph", Icons.Outlined.Hub),
            Triple("STUDY", "Study", Icons.Outlined.MenuBook),
            Triple("CHAT", "AI Tutor", Icons.Outlined.Psychology),
            Triple("PATHS", "Paths", Icons.Outlined.Timeline)
        )

        items.forEach { (route, label, icon) ->
            val selected = currentScreen == route
            NavigationBarItem(
                selected = selected,
                onClick = { onScreenSelected(route) },
                label = { Text(label, fontSize = 11.sp, color = if (selected) CyanAccent else TextSecondary) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (selected) CyanAccent else TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = BorderColor
                )
            )
        }
    }
}

// --- DYNAMIC INTERACTIVE GRAPH VIEW ---
@Composable
fun GraphExplorerScreen(viewModel: VisionMindViewModel) {
    val concepts by viewModel.concepts.collectAsState()
    val relationships by viewModel.relationships.collectAsState()
    val userProgress by viewModel.progress.collectAsState()
    val selectedConcept by viewModel.selectedConcept.collectAsState()

    var panX by remember { mutableStateOf(0f) }
    var panY by remember { mutableStateOf(0f) }

    val categoryColors = mapOf(
        "Mathematics" to VioletAccent,
        "Signal Processing" to Color(0xFF3F51B5),
        "Classical CV" to Color(0xFF009688),
        "Deep Learning" to Color(0xFFFF9800),
        "Optimization" to RoseAccent,
        "Geometry" to CyanAccent
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Grid background drawing
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val gridSpacing = 60.dp.toPx()
                    val startX = panX % gridSpacing
                    val startY = panY % gridSpacing

                    // Verticals
                    var x = startX
                    while (x < size.width) {
                        drawLine(
                            color = Color(0xFF1E293B).copy(alpha = 0.3f),
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = 1f
                        )
                        x += gridSpacing
                    }

                    // Horizontals
                    var y = startY
                    while (y < size.height) {
                        drawLine(
                            color = Color(0xFF1E293B).copy(alpha = 0.3f),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1f
                        )
                        y += gridSpacing
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        panX += dragAmount.x
                        panY += dragAmount.y
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Draw prerequisite connection lines
                relationships.forEach { rel ->
                    val fromNode = concepts.find { it.id == rel.fromId }
                    val toNode = concepts.find { it.id == rel.toId }
                    if (fromNode != null && toNode != null) {
                        val start = Offset(fromNode.xPos.dp.toPx() + panX, fromNode.yPos.dp.toPx() + panY)
                        val end = Offset(toNode.xPos.dp.toPx() + panX, toNode.yPos.dp.toPx() + panY)

                        // Highlight line if selected node is part of relationship
                        val isHighlighted = selectedConcept?.id == rel.fromId || selectedConcept?.id == rel.toId
                        val color = if (isHighlighted) CyanAccent.copy(alpha = 0.8f) else Color(0xFF475569).copy(alpha = 0.4f)
                        val stroke = if (isHighlighted) 3.dp.toPx() else 1.5.dp.toPx()

                        // Draw path with pointer arrow
                        drawLine(
                            color = color,
                            start = start,
                            end = end,
                            strokeWidth = stroke,
                            pathEffect = if (!isHighlighted) PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f) else null
                        )

                        // Drawing Arrow at midpoint
                        val midX = (start.x + end.x) / 2
                        val midY = (start.y + end.y) / 2
                        val angle = atan2(end.y - start.y, end.x - start.x)
                        val arrowLength = 12.dp.toPx()
                        val arrowAngle = Math.PI / 6 // 30 degrees

                        val p1 = Offset(
                            midX - arrowLength * cos(angle - arrowAngle).toFloat(),
                            midY - arrowLength * sin(angle - arrowAngle).toFloat()
                        )
                        val p2 = Offset(
                            midX - arrowLength * cos(angle + arrowAngle).toFloat(),
                            midY - arrowLength * sin(angle + arrowAngle).toFloat()
                        )

                        drawPath(
                            path = Path().apply {
                                moveTo(midX, midY)
                                lineTo(p1.x, p1.y)
                                lineTo(p2.x, p2.y)
                                close()
                            },
                            color = color
                        )
                    }
                }

                // Draw Nodes on top
                concepts.forEach { concept ->
                    val x = concept.xPos.dp.toPx() + panX
                    val y = concept.yPos.dp.toPx() + panY
                    val color = categoryColors[concept.category] ?: CyanAccent
                    val isSelected = selectedConcept?.id == concept.id

                    val prog = userProgress[concept.id]
                    val isMastered = prog?.status == "MASTERED"

                    // Glow ring for selected
                    if (isSelected) {
                        drawCircle(
                            color = CyanAccent.copy(alpha = 0.15f),
                            radius = 36.dp.toPx(),
                            center = Offset(x, y)
                        )
                        drawCircle(
                            color = CyanAccent,
                            radius = 24.dp.toPx(),
                            center = Offset(x, y),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }

                    // Base circle filled
                    drawCircle(
                        color = CardBg,
                        radius = 20.dp.toPx(),
                        center = Offset(x, y)
                    )

                    // Accent boundary representing category
                    drawCircle(
                        color = color,
                        radius = 20.dp.toPx(),
                        center = Offset(x, y),
                        style = Stroke(width = if (isMastered) 4.dp.toPx() else 2.dp.toPx())
                    )

                    // Mastered Checkmark or simple dot
                    if (isMastered) {
                        drawCircle(
                            color = Color(0xFF10B981), // Solid Mastered Green
                            radius = 6.dp.toPx(),
                            center = Offset(x + 14.dp.toPx(), y - 14.dp.toPx())
                        )
                    }
                }
            }

            // Click interceptors for Nodes
            concepts.forEach { concept ->
                val x = concept.xPos.dp + panX.dp
                val y = concept.yPos.dp + panY.dp

                Box(
                    modifier = Modifier
                        .offset(x - 24.dp, y - 24.dp)
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable { viewModel.selectConcept(concept) }
                )
            }
        }

        // Floating info cards or category guides
        Card(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg.copy(alpha = 0.9f)),
            border = BorderStroke(1.dp, BorderColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("KNOWLEDGE GRAPH", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(VioletAccent, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Math foundations", fontSize = 10.sp, color = TextSecondary)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF3F51B5), CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Signals / Fourier", fontSize = 10.sp, color = TextSecondary)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF009688), CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Classical CV / Edges", fontSize = 10.sp, color = TextSecondary)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFFFF9800), CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Deep ConvNets", fontSize = 10.sp, color = TextSecondary)
                }
            }
        }

        // Bottom Concept Detail overlay
        AnimatedVisibility(
            visible = selectedConcept != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth(),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            selectedConcept?.let { concept ->
                val progress = userProgress[concept.id]
                val statusText = when (progress?.status) {
                    "MASTERED" -> "Mastered (Score: ${progress.quizScore}%)"
                    "IN_PROGRESS" -> "In Progress (Score: ${progress.quizScore}%)"
                    else -> "Not Started"
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = concept.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${concept.category} • ${concept.difficulty}",
                                    fontSize = 11.sp,
                                    color = CyanAccent
                                )
                            }
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (progress?.status == "MASTERED") Color(0xFF064E3B) else BorderColor
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = statusText,
                                    fontSize = 10.sp,
                                    color = if (progress?.status == "MASTERED") Color(0xFF34D399) else TextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = concept.shortDesc,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.setScreen("STUDY") },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("study_button")
                            ) {
                                Icon(Icons.Default.MenuBook, contentDescription = null, tint = DarkBg, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Study Concept", color = DarkBg, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { viewModel.setScreen("CHAT") },
                                colors = ButtonDefaults.buttonColors(containerColor = BorderColor),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.3f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Psychology, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ask Tutor", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- STUDY SCREEN WITH MULTIPLE TABS AND LOCAL NOTEBOOK ---
@Composable
fun StudyDeepDiveScreen(viewModel: VisionMindViewModel) {
    val selectedConcept by viewModel.selectedConcept.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val explanationLevel by viewModel.explanationLevel.collectAsState()
    val customAiExplanation by viewModel.customAiExplanation.collectAsState()
    val isGeneratingAiExplanation by viewModel.isGeneratingAiExplanation.collectAsState()

    if (selectedConcept == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Select a concept from the knowledge graph to start studying.", color = TextSecondary)
        }
        return
    }

    val concept = selectedConcept!!
    val conceptProgress = progress[concept.id]

    var activeTab by remember { mutableStateOf(0) } // 0: INTUITION, 1: FORMAL MATH, 2: CODE, 3: PAPERS, 4: QUIZ
    var noteText by remember(concept.id) { mutableStateOf(conceptProgress?.userNotes ?: "") }
    var showNotesDrawer by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Study Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(concept.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("${concept.category} • Depth Study", fontSize = 11.sp, color = CyanAccent)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { showNotesDrawer = !showNotesDrawer },
                        modifier = Modifier.background(if (showNotesDrawer) BorderColor else Color.Transparent, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (noteText.isEmpty()) Icons.Outlined.NoteAdd else Icons.Filled.StickyNote2,
                            contentDescription = "My Notes",
                            tint = if (noteText.isEmpty()) TextSecondary else CyanAccent
                        )
                    }
                }
            }
        }

        // Horizontal Category Tabs
        ScrollableTabRow(
            selectedTabIndex = activeTab,
            containerColor = DarkBg,
            edgePadding = 12.dp,
            divider = {},
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                    color = CyanAccent
                )
            }
        ) {
            val tabs = listOf("Intuition", "Formal Math", "Code Lab", "Research", "Practice Quiz")
            tabs.forEachIndexed { idx, title ->
                Tab(
                    selected = activeTab == idx,
                    onClick = { activeTab = idx },
                    text = { Text(title, fontSize = 12.sp, fontWeight = if (activeTab == idx) FontWeight.Bold else FontWeight.Normal) },
                    selectedContentColor = CyanAccent,
                    unselectedContentColor = TextSecondary
                )
            }
        }

        Row(modifier = Modifier.fillMaxSize()) {
            // Main Study Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(12.dp)
            ) {
                when (activeTab) {
                    0 -> IntuitionTab(
                        concept = concept,
                        level = explanationLevel,
                        customExplanation = customAiExplanation,
                        isLoading = isGeneratingAiExplanation,
                        onLevelChange = { viewModel.setExplanationLevel(it) },
                        onCustomSubmit = { viewModel.customizeActiveExplanation(it) },
                        onReset = { viewModel.resetCustomExplanation() }
                    )
                    1 -> FormalMathTab(concept = concept)
                    2 -> CodeLabTab(concept = concept)
                    3 -> ResearchPapersTab(concept = concept)
                    4 -> QuizPracticeTab(viewModel = viewModel)
                }
            }

            // Quick sliding Notebook Drawer
            AnimatedVisibility(
                visible = showNotesDrawer,
                enter = expandHorizontally(expandFrom = Alignment.End) + fadeIn(),
                exit = shrinkHorizontally(shrinkTowards = Alignment.End) + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(280.dp)
                        .padding(start = 6.dp, top = 4.dp, bottom = 12.dp, end = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    border = BorderStroke(1.dp, BorderColor),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("PERSONAL NOTES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                            IconButton(onClick = { showNotesDrawer = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(16.dp))
                            }
                        }
                        Text(
                            "Your persistent journal for proofs, custom equations, and pipeline notes on this topic.",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = noteText,
                            onValueChange = {
                                noteText = it
                                viewModel.saveUserNotes(it)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            textStyle = TextStyle(fontSize = 12.sp, color = TextPrimary),
                            placeholder = { Text("Write equations, insights, or code modifications here...", fontSize = 11.sp, color = TextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanAccent,
                                unfocusedBorderColor = BorderColor,
                                focusedContainerColor = DarkBg,
                                unfocusedContainerColor = DarkBg
                            )
                        )
                    }
                }
            }
        }
    }
}

// --- SUB-TABS COMPOSABLES ---

@Composable
fun IntuitionTab(
    concept: ConceptEntity,
    level: String,
    customExplanation: String?,
    isLoading: Boolean,
    onLevelChange: (String) -> Unit,
    onCustomSubmit: (String) -> Unit,
    onReset: () -> Unit
) {
    var customQuery by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // Adaptive Level Selector (Beginner / Intermediate / Advanced)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBg, RoundedCornerShape(12.dp))
                    .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val levels = listOf("Beginner", "Intermediate", "Advanced")
                levels.forEach { lvl ->
                    val selected = level == lvl
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) BorderColor else Color.Transparent)
                            .clickable { onLevelChange(lvl) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = lvl,
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) CyanAccent else TextSecondary
                        )
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("CONCEPT TUTORIAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                        if (customExplanation != null) {
                            TextButton(onClick = onReset) {
                                Text("Restore Standard", fontSize = 11.sp, color = RoseAccent)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    if (isLoading) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = CyanAccent, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("VisionMind AI adapting content...", fontSize = 12.sp, color = TextSecondary)
                        }
                    } else {
                        val textToDisplay = customExplanation ?: when (level) {
                            "Beginner" -> concept.intuitiveExplanationBeginner
                            "Advanced" -> concept.intuitiveExplanationAdvanced
                            else -> concept.intuitiveExplanationIntermediate
                        }
                        Text(
                            text = textToDisplay,
                            fontSize = 13.sp,
                            color = TextPrimary,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }

        // Live Custom Tutoring Request Box (Connects to Gemini)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "Adapt Explanation dynamically via Gemini",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        "E.g., 'Simplify this for a self-driving engineer' or 'Give me a spatial projection analogy.'",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customQuery,
                            onValueChange = { customQuery = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("How would you like to adapt this topic?", fontSize = 11.sp) },
                            textStyle = TextStyle(fontSize = 12.sp, color = TextPrimary),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanAccent,
                                unfocusedBorderColor = BorderColor,
                                focusedContainerColor = DarkBg,
                                unfocusedContainerColor = DarkBg
                            ),
                            maxLines = 2
                        )
                        Button(
                            onClick = {
                                if (customQuery.isNotBlank()) {
                                    onCustomSubmit(customQuery)
                                    customQuery = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.align(Alignment.CenterVertically)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "Adapt", tint = DarkBg, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FormalMathTab(concept: ConceptEntity) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("MATHEMATICAL FORMULATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = VioletAccent)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = concept.formalMath,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        lineHeight = 22.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkBg, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("FORMAL PROOF / DERIVATION SKETCH", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = VioletAccent)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = concept.proofSketch,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("REAL-WORLD ECOSYSTEM INTEGRATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = concept.realWorldApps,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CodeLabTab(concept: ConceptEntity) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("PRODUCTION CODE REFERENCE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(concept.pythonCode))
                                Toast.makeText(context, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Code", tint = CyanAccent, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = concept.pythonCode,
                        fontSize = 11.sp,
                        color = Color(0xFF34D399), // Clean green terminal tone
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkBg, RoundedCornerShape(10.dp))
                            .horizontalScroll(rememberScrollState())
                            .padding(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ResearchPapersTab(concept: ConceptEntity) {
    val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    val type = Types.newParameterizedType(List::class.java, ResearchPaper::class.java)
    val papers: List<ResearchPaper> = remember(concept.id) {
        try {
            moshi.adapter<List<ResearchPaper>>(type).fromJson(concept.researchPapersJson) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "SEMINAL RESEARCH & LITERATURE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CyanAccent
            )
        }
        items(papers) { paper ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = paper.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${paper.authors} (${paper.year})",
                        fontSize = 11.sp,
                        color = CyanAccent
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Why it matters in the Vision Ecosystem:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = paper.importance,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun QuizPracticeTab(viewModel: VisionMindViewModel) {
    val questions by viewModel.activeQuizQuestions.collectAsState()
    val currentIndex by viewModel.currentQuizIndex.collectAsState()
    val selectedOption by viewModel.quizSelectedOption.collectAsState()
    val submitted by viewModel.quizSubmitted.collectAsState()
    val correctCount by viewModel.quizCorrectAnswers.collectAsState()
    val finished by viewModel.quizFinished.collectAsState()

    if (questions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No quiz questions loaded for this topic.", color = TextSecondary)
        }
        return
    }

    if (finished) {
        val total = questions.size
        val percent = (correctCount * 100) / total
        val passed = percent >= 70

        Card(
            modifier = Modifier.fillMaxSize(),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            border = BorderStroke(1.dp, if (passed) Color(0xFF10B981) else RoseAccent)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = if (passed) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (passed) Color(0xFF10B981) else RoseAccent,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (passed) "CONCEPT MASTERED!" else "RETRIES RECOMMENDED",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Score: $percent% ($correctCount of $total correct)",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (passed) "Amazing work! This topic is marked as mastered in your knowledge graph, unlocking advanced dependents." else "To complete the prerequisite verification and master this topic, aim for a score of 70% or higher.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { viewModel.selectConcept(viewModel.selectedConcept.value!!) },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                ) {
                    Text("Restart Practice Session", color = DarkBg, fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    val currentQuestion = questions[currentIndex]

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "VERIFICATION PRACTICUM: ${currentIndex + 1} OF ${questions.size}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CyanAccent
            )
            Text(
                "Correct: $correctCount",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
        Spacer(modifier = Modifier.height(10.dp))

        // Progress indicator bar
        LinearProgressIndicator(
            progress = { (currentIndex.toFloat() + 1) / questions.size },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = CyanAccent,
            trackColor = BorderColor
        )
        Spacer(modifier = Modifier.height(14.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = currentQuestion.question,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                items(currentQuestion.options.size) { idx ->
                    val isSelected = selectedOption == idx
                    val isCorrect = idx == currentQuestion.correctOptionIndex
                    val optionText = currentQuestion.options[idx]

                    val borderCol = when {
                        submitted && isCorrect -> Color(0xFF10B981)
                        submitted && isSelected && !isCorrect -> RoseAccent
                        isSelected -> CyanAccent
                        else -> BorderColor
                    }

                    val containerCol = when {
                        submitted && isCorrect -> Color(0xFF064E3B).copy(alpha = 0.3f)
                        submitted && isSelected && !isCorrect -> Color(0xFF991B1B).copy(alpha = 0.3f)
                        isSelected -> BorderColor
                        else -> DarkBg
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(containerCol, RoundedCornerShape(10.dp))
                            .border(1.dp, borderCol, RoundedCornerShape(10.dp))
                            .clickable(enabled = !submitted) { viewModel.selectQuizOption(idx) }
                            .padding(12.dp)
                            .testTag("quiz_option_${idx}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { if (!submitted) viewModel.selectQuizOption(idx) },
                            colors = RadioButtonDefaults.colors(selectedColor = CyanAccent, unselectedColor = TextSecondary),
                            enabled = !submitted
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(optionText, fontSize = 12.sp, color = TextPrimary)
                    }
                }

                if (submitted) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = BorderColor),
                            border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("EXPLANATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(currentQuestion.explanation, fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Action panel
        if (!submitted) {
            Button(
                onClick = { viewModel.submitQuizAnswer() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                enabled = selectedOption != -1
            ) {
                Text("Verify Answer", color = DarkBg, fontWeight = FontWeight.Bold)
            }
        } else {
            Button(
                onClick = { viewModel.nextQuizQuestion() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
            ) {
                Text(if (currentIndex + 1 < questions.size) "Next Question" else "Finish Practicum", color = DarkBg, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- AI TUTOR INTERACTIVE CHAT SCREEN ---
@Composable
fun ChatTutorScreen(viewModel: VisionMindViewModel) {
    val selectedConcept by viewModel.selectedConcept.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isSending by viewModel.isSendingChatMessage.collectAsState()

    if (selectedConcept == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Select a concept to open the AI Research Lab.", color = TextSecondary)
        }
        return
    }

    val concept = selectedConcept!!
    var userText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Scroll to latest message
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Chat concept context guide
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(CyanAccent, CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tutor Sync: ${concept.title}", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = { viewModel.clearChatHistory() }) {
                    Text("Clear Labs", fontSize = 11.sp, color = RoseAccent)
                }
            }
        }

        // Messages Box
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (chatMessages.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "VisionMind AI Mentorship Lab",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Pose complex CV questions, ask for manual mathematical derivations, or paste PyTorch code to debug on the spot.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Micro Quick Actions
                        Text("QUICK TUTOR COMMANDS:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        val commands = listOf(
                            "Give me a practical coding challenge on this!",
                            "Walk me through the mathematical derivation.",
                            "Explain the real-world applications in SLAM/Autonomous cars."
                        )
                        commands.forEach { cmd ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.askTutor(cmd) }
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = CardBg),
                                border = BorderStroke(1.dp, BorderColor)
                            ) {
                                Text(cmd, fontSize = 11.sp, color = TextPrimary, modifier = Modifier.padding(10.dp))
                            }
                        }
                    }
                }
            } else {
                items(chatMessages) { message ->
                    val isAi = message.sender == "AI"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isAi) Arrangement.Start else Arrangement.End
                    ) {
                        Card(
                            modifier = Modifier.widthIn(max = 300.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isAi) CardBg else BorderColor
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isAi) CyanAccent.copy(alpha = 0.3f) else Color.Transparent
                            ),
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isAi) 4.dp else 16.dp,
                                bottomEnd = if (isAi) 16.dp else 4.dp
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (isAi) "VisionMind AI" else "Scholar",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAi) CyanAccent else VioletAccent
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = message.message,
                                    fontSize = 12.sp,
                                    color = TextPrimary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            if (isSending) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardBg),
                            border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.3f))
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(color = CyanAccent, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("AI Mentor is analyzing...", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }

        // Input Tray
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = userText,
                    onValueChange = { userText = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input"),
                    placeholder = { Text("Consult mentor...", fontSize = 12.sp) },
                    textStyle = TextStyle(fontSize = 13.sp, color = TextPrimary),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = DarkBg,
                        unfocusedContainerColor = DarkBg
                    ),
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (userText.isNotBlank() && !isSending) {
                            viewModel.askTutor(userText)
                            userText = ""
                        }
                    })
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (userText.isNotBlank() && !isSending) {
                            viewModel.askTutor(userText)
                            userText = ""
                        }
                    },
                    modifier = Modifier
                        .background(CyanAccent, CircleShape)
                        .size(40.dp)
                        .testTag("ask_tutor_button"),
                    enabled = userText.isNotBlank() && !isSending
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = DarkBg, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// --- GAP ANALYZER PANEL ---
@Composable
fun GapAnalyzerScreen(viewModel: VisionMindViewModel) {
    val detectedGaps by viewModel.detectedGaps.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "PREREQUISITE KNOWLEDGE GAP ANALYZER",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = AmberAccent
        )
        Text(
            "Our compiler analyzes your quiz performance and the dependency lines of the active knowledge graph in real-time, verifying whether you possess the mathematical and engineering foundations to advance smoothly.",
            fontSize = 11.sp,
            color = TextSecondary
        )

        if (detectedGaps.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = BorderStroke(1.dp, Color(0xFF10B981))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Zero Gaps Detected", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(
                        "Your prerequisites align perfectly with your studying targets. All advanced dependents are safely unlocked for learning.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(detectedGaps) { gap ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Dependency Warning: ${gap.targetConcept.title}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(gap.remedialAdvice, fontSize = 12.sp, color = TextSecondary)

                            Spacer(modifier = Modifier.height(12.dp))
                            Text("SUGGESTED STUDY ACTION:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AmberAccent)
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                gap.missingPrerequisites.forEach { prereq ->
                                    Button(
                                        onClick = {
                                            viewModel.selectConcept(prereq)
                                            viewModel.setScreen("STUDY")
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BorderColor),
                                        border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Study ${prereq.title.split(" ").firstOrNull() ?: prereq.title}", color = TextPrimary, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- PERSONALIZED LEARNING PATH SCREEN ---
@Composable
fun PathPlannerScreen(viewModel: VisionMindViewModel) {
    val goal by viewModel.customLearningPathGoal.collectAsState()
    val pathText by viewModel.aiLearningPathResponse.collectAsState()
    val isGenerating by viewModel.isGeneratingLearningPath.collectAsState()

    var goalInput by remember { mutableStateOf(goal) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                "PERSONALIZED ROADMAP GENERATOR",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CyanAccent
            )
            Text(
                "Need a tailored path? Type your dream computer vision goal (e.g., 'Learn 3D SLAM reconstructor' or 'Pass my Deep Learning research interview') and our AI Tutor will structure the optimal roadmap for you.",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    OutlinedTextField(
                        value = goalInput,
                        onValueChange = { goalInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("E.g., Master 3D reconstruction pipelines from scratch...", fontSize = 12.sp) },
                        textStyle = TextStyle(fontSize = 13.sp, color = TextPrimary),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = BorderColor,
                            focusedContainerColor = DarkBg,
                            unfocusedContainerColor = DarkBg
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            if (goalInput.isNotBlank()) {
                                viewModel.generateCustomLearningPath(goalInput)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                        enabled = goalInput.isNotBlank() && !isGenerating
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(color = DarkBg, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Compiling custom curriculum...", color = DarkBg, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = DarkBg, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Generate Personalized Pathway", color = DarkBg, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (pathText != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("YOUR PERSONALIZED ACADEMIC PATHWAY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = pathText!!,
                            fontSize = 12.sp,
                            color = TextPrimary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}
