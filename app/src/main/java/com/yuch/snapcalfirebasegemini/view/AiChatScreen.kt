package com.yuch.snapcalfirebasegemini.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yuch.snapcalfirebasegemini.data.api.response.AiChatMessage
import com.yuch.snapcalfirebasegemini.data.api.response.UsageAiChat
import com.yuch.snapcalfirebasegemini.viewmodel.AiChatViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Date
import java.util.Locale

@Composable
fun AiChatScreen(aiChatViewModel: AiChatViewModel, onBackClick: () -> Unit) {
    val chatMessages by aiChatViewModel.chatMessages.collectAsState()
    val isLoading by aiChatViewModel.isLoading.collectAsState()
    val errorMessage by aiChatViewModel.errorMessage.collectAsState()
    val selectedService by aiChatViewModel.selectedService.collectAsState()
    val usageInfo by aiChatViewModel.usageInfo.collectAsState()

    var userMessage by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Material3 color scheme
    val colorScheme = MaterialTheme.colorScheme

    // Group messages by date
    val groupedMessages = remember(chatMessages) {
        chatMessages.reversed().groupBy { message ->
            try {
                // Konversi timestamp ke zona waktu lokal
                val zonedDateTime = OffsetDateTime.parse(message.timestamp)
                    .withOffsetSameInstant(
                        ZoneOffset.UTC) // Pastikan dari UTC
                    .atZoneSameInstant(ZoneId.systemDefault()) // Sesuaikan dengan zona waktu perangkat

                // Format tanggal untuk tampilan
                val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                zonedDateTime.format(dateFormatter)
            } catch (e: Exception) {
                "Unknown Date"
            }
        }
    }

    // Current date for comparison
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

    var showDeleteConfirmation by remember { mutableStateOf(false) }

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
                TextButton(
                    onClick = { showDeleteConfirmation = false }
                ) {
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
            // Chat messages
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
                        // Date header
                        item {
                            DateHeader(
                                date = when (date) {
                                    currentDate -> "Today"
                                    yesterdayDate -> "Yesterday"
                                    else -> date
                                }
                            )
                        }

                        // Messages for this date
                        items(messages, key = { it.id }) { message ->
                            ChatBubble(message)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    // Welcome message if chat is empty
                    if (chatMessages.isEmpty()) {
                        item {
                            WelcomeMessage(selectedService)
                        }
                    }
                }

                // Loading indicator
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
                            colors = CardDefaults.cardColors(
                                containerColor = colorScheme.surface
                            )
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

            // Error message
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
                        colors = CardDefaults.cardColors(
                            containerColor = colorScheme.errorContainer
                        )
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

            // Input area
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = userMessage,
                        onValueChange = { userMessage = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Message ${
                            selectedService.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(
                                    Locale.getDefault()
                                ) else it.toString()
                            }
                        }...") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (userMessage.isNotBlank()) {
                                aiChatViewModel.sendMessage(userMessage, selectedService)
                                userMessage = ""
                            }
                        })
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledIconButton(
                        onClick = {
                            if (userMessage.isNotBlank()) {
                                aiChatViewModel.sendMessage(userMessage, selectedService)
                                userMessage = ""
                            }
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = colorScheme.primary
                        ),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = colorScheme.onPrimary
                        )
                    }
                }
            }

            // Service and quota info
            ServiceInfoBar(selectedService, usageInfo)
        }
    }
}

@Composable
fun DateHeader(date: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(
                1f
            ),
            color = MaterialTheme.colorScheme.outline.copy(
                alpha = 0.3f
            )
        )

        Card(
            modifier = Modifier.padding(
                horizontal = 12.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            shape = RoundedCornerShape(
                16.dp
            )
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 6.dp
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(
                        16.dp
                    ),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(
                    modifier = Modifier.width(
                        4.dp
                    )
                )
                Text(
                    text = date,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.weight(
                1f
            ),
            color = MaterialTheme.colorScheme.outline.copy(
                alpha = 0.3f
            )
        )
    }
}

@Composable
fun WelcomeMessage(selectedService: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Rounded.Bolt,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Welcome to AI Chat",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Start chatting with ${
                    selectedService.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            Locale.getDefault()
                        ) else it.toString()
                    }
                } AI to get assistance with your questions.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            // Date and time display
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                val currentDate = remember {
                    SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date())
                }
                Text(
                    text = currentDate,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ChatBubble(message: AiChatMessage) {
    val isUser = message.role == "user"

    val timestampInfo = remember {
        try {
            val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
            val zonedTime = Instant.parse(message.timestamp).atZone(ZoneId.systemDefault())
            zonedTime.format(timeFormatter)
        } catch (e: Exception) {
            ""
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier
                .padding(4.dp)
                .widthIn(max = 280.dp)
                .shadow(4.dp, RoundedCornerShape(16.dp))
                .animateContentSize()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.content,
                    color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = timestampInfo,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isUser) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = if (isUser) TextAlign.End else TextAlign.Start
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(
    selectedService: String,
    onServiceSelected: (String) -> Unit,
    onBackClick: () -> Unit,
    onDeleteChatClick: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "AI Assistant",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            var expanded by remember { mutableStateOf(false) }

            // Current service display
            Text(
                text = selectedService.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(
                        Locale.getDefault()
                    ) else it.toString()
                },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )

            // Model selector
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Select AI Model")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            color = if (selectedService == "gemini") MaterialTheme.colorScheme.primary else Color.Transparent,
                                            shape = CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Gemini")
                            }
                        },
                        onClick = {
                            onServiceSelected("gemini")
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            color = if (selectedService == "groq") MaterialTheme.colorScheme.primary else Color.Transparent,
                                            shape = CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Groq")
                            }
                        },
                        onClick = {
                            onServiceSelected("groq")
                            expanded = false
                        }
                    )
                    // Delete chat history
                    DropdownMenuItem(
                        modifier = Modifier.background(MaterialTheme.colorScheme.error),
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onError
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Delete Chat",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onError
                                )
                            }
                        },
                        onClick = {
                            onDeleteChatClick()
                            expanded = false
                        }
                    )

                }
            }
        }
    )
}

@Composable
fun ServiceInfoBar(selectedService: String, usageInfo: UsageAiChat?) {
    val geminiColor = animateColorAsState(
        targetValue = if (selectedService == "gemini") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        animationSpec = tween(300),
        label = "geminiColor"
    )

    val groqColor = animateColorAsState(
        targetValue = if (selectedService == "groq") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        animationSpec = tween(300),
        label = "groqColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = 4.dp
                    ),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )

            // Quota info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gemini quota
                Column(
                    modifier = Modifier.weight(
                        1f
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Gemini",
                        style = MaterialTheme.typography.labelMedium,
                        color = geminiColor.value,
                        fontWeight = FontWeight.Bold
                    )
                    LinearProgressIndicator(
                        progress = {
                            ((usageInfo?.gemini?.remainingQuota ?: 0) / 10f).coerceIn(0f, 1f)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        color = geminiColor.value,
                    )
                    Text(
                        text = "${usageInfo?.gemini?.remainingQuota ?: 0} remaining",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = 0.7f
                        )
                    )
                }

                HorizontalDivider(
                    modifier = Modifier
                        .height(
                            32.dp
                        )
                        .width(
                            1.dp
                        ),
                    color = MaterialTheme.colorScheme.outline.copy(
                        alpha = 0.3f
                    )
                )

                // Groq quota
                Column(
                    modifier = Modifier.weight(
                        1f
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Groq",
                        style = MaterialTheme.typography.labelMedium,
                        color = groqColor.value,
                        fontWeight = FontWeight.Bold
                    )
                    LinearProgressIndicator(
                        progress = {
                            ((usageInfo?.gemini?.remainingQuota ?: 0) / 10f).coerceIn(0f, 1f)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 16.dp
                            ),
                        color = groqColor.value,
                    )
                    Text(
                        text = "${usageInfo?.groq?.remainingQuota ?: 0} remaining",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = 0.7f
                        )
                    )
                }
            }
        }
    }
}