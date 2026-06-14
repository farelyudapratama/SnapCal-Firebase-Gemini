package com.yuch.snapcalfirebasegemini.view.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yuch.snapcalfirebasegemini.view.chat.components.ChatBubble
import com.yuch.snapcalfirebasegemini.view.chat.components.ChatInputBar
import com.yuch.snapcalfirebasegemini.view.chat.components.ChatTopBar
import com.yuch.snapcalfirebasegemini.view.chat.components.DateHeader
import com.yuch.snapcalfirebasegemini.view.chat.components.WelcomeMessage
import com.yuch.snapcalfirebasegemini.viewmodel.AiChatViewModel
import kotlinx.coroutines.delay
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun AiChatScreen(aiChatViewModel: AiChatViewModel, onBackClick: () -> Unit) {
    val chatMessages by aiChatViewModel.chatMessages.collectAsState()
    val isLoading by aiChatViewModel.isLoading.collectAsState()
    val errorMessage by aiChatViewModel.errorMessage.collectAsState()
    val selectedService by aiChatViewModel.selectedService.collectAsState()
    val usageInfo by aiChatViewModel.usageInfo.collectAsState()

    var userMessage by remember { mutableStateOf("") }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val colorScheme = MaterialTheme.colorScheme

    val groupedMessages = remember(chatMessages) {
        chatMessages.reversed().groupBy { message ->
            try {
                val zonedDateTime = OffsetDateTime.parse(message.timestamp)
                    .withOffsetSameInstant(ZoneOffset.UTC)
                    .atZoneSameInstant(ZoneId.systemDefault())

                val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                zonedDateTime.format(dateFormatter)
            } catch (e: Exception) {
                "Unknown Date"
            }
        }
    }

    val currentDate = remember {
        ZonedDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
    }

    val yesterdayDate = remember {
        ZonedDateTime.now(ZoneId.systemDefault()).minusDays(1)
            .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
    }

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    LaunchedEffect(usageInfo) {
        if (usageInfo == null) {
            aiChatViewModel.fetchChatUsage()
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            aiChatViewModel.fetchChatUsage()
            delay(30_000)
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Chat History") },
            text = { Text("Are you sure you want to delete all chat history? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        aiChatViewModel.deleteChatHistory()
                        showDeleteConfirmation = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            ChatTopBar(
                selectedService = selectedService,
                onServiceSelected = { aiChatViewModel.setSelectedService(it) },
                onBackClick = onBackClick,
                onDeleteChatClick = { showDeleteConfirmation = true },
            )
        },
        containerColor = colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    reverseLayout = true,
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    groupedMessages.forEach { (date, messages) ->
                        item {
                            DateHeader(
                                date = when (date) {
                                    currentDate -> "Today"
                                    yesterdayDate -> "Yesterday"
                                    else -> date
                                }
                            )
                        }

                        items(messages, key = { it.id }) { message ->
                            ChatBubble(message)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    if (chatMessages.isEmpty()) {
                        item { WelcomeMessage(selectedService) }
                    }
                }

                this@Column.AnimatedVisibility(
                    visible = isLoading,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier.size(100.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(color = colorScheme.primary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Loading...",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                errorMessage?.let {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = colorScheme.errorContainer)
                    ) {
                        Text(
                            text = it,
                            color = colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            ChatInputBar(
                message = userMessage,
                selectedService = selectedService,
                onMessageChange = { userMessage = it },
                onSend = {
                    if (userMessage.isNotBlank()) {
                        aiChatViewModel.sendMessage(userMessage, selectedService)
                        userMessage = ""
                    }
                }
            )
        }
    }
}
