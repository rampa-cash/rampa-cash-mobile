package com.example.rampacashmobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.rampacashmobile.viewmodel.LearnViewModel
import dev.jeziellago.compose.markdowntext.MarkdownText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonScreen(
    navController: NavController,
    moduleId: String,
    submoduleId: String,
    viewModel: LearnViewModel = hiltViewModel()
) {
    val modules by viewModel.modules.collectAsState()
    val currentLessonIndex by viewModel.currentLessonIndex.collectAsState()

    val module = modules.find { it.id == moduleId }
    val submodule = module?.submodules?.find { it.id == submoduleId }

    LaunchedEffect(Unit) {
        viewModel.resetLessonIndex()
    }

    if (module == null || submodule == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Content not found", color = Color.White)
        }
        return
    }

    val lessons = submodule.lessons
    val currentLesson = lessons.getOrNull(currentLessonIndex)
    val isLastLesson = currentLessonIndex == lessons.size - 1
    val isFirstLesson = currentLessonIndex == 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = submodule.title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Lesson ${currentLessonIndex + 1} of ${lessons.size}",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF1E293B)
            )
        )

        // Progress bar
        LinearProgressIndicator(
            progress = { (currentLessonIndex + 1).toFloat() / lessons.size.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = Color(0xFF8B5CF6),
            trackColor = Color(0xFF334155),
        )

        if (currentLesson != null) {
            // Lesson Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Text(
                    text = currentLesson.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                MarkdownText(
                    markdown = currentLesson.content,
                    color = Color(0xFFE2E8F0),
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // Bottom Navigation Buttons
        Surface(
            color = Color(0xFF1E293B),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Previous Button
                if (!isFirstLesson) {
                    OutlinedButton(
                        onClick = { viewModel.previousLesson() },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Previous", fontSize = 16.sp)
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Next/Complete Button
                Button(
                    onClick = {
                        if (isLastLesson) {
                            // Complete submodule
                            viewModel.completeSubmodule(moduleId, submoduleId)
                            navController.popBackStack()
                        } else {
                            viewModel.nextLesson()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8B5CF6)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isLastLesson) "Complete" else "Next",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = if (isLastLesson) "Complete" else "Next",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

