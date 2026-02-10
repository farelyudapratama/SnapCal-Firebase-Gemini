package com.yuch.snapcalfirebasegemini.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yuch.snapcalfirebasegemini.data.api.response.Announcement

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AnnouncementCarousel(announcements: List<Announcement>) {
    val pagerState = rememberPagerState(pageCount = { announcements.size })

    val sortedAnnouncements = remember(announcements) {
        announcements.sortedByDescending { it.priority }
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 0.dp),
            pageSpacing = 16.dp
        ) { page ->
            AnnouncementItem(announcement = sortedAnnouncements[page])
        }

        if (sortedAnnouncements.size > 1) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                Modifier
                    .wrapContentHeight()
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pagerState.pageCount) { iteration ->
                    val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else Color.LightGray
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AnnouncementItem(announcement: Announcement) {
    val context = LocalContext.current

    val cardColor = when (announcement.type) {
        "info" -> Color(0xFFE3F2FD)
        "promo" -> Color(0xFFF3E5F5)
        "survey" -> Color(0xFFE8F5E9)
        "warning" -> Color(0xFFFFEBEE)
        "update" -> Color(0xFFE0F2F1)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val icon = when (announcement.type) {
        "info" -> Icons.Default.Info
        "promo" -> Icons.Default.Star
        "survey" -> Icons.Default.Assignment
        "warning" -> Icons.Default.Warning
        "update" -> Icons.Default.Update
        else -> Icons.Default.Notifications
    }

    val iconColor = when (announcement.type) {
        "info" -> Color(0xFF1976D2)
        "promo" -> Color(0xFF7B1FA2)
        "survey" -> Color(0xFF388E3C)
        "warning" -> Color(0xFFD32F2F)
        "update" -> Color(0xFF00796B)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = announcement.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = announcement.message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black.copy(alpha = 0.8f)
            )

            if (announcement.actionType != "none" && !announcement.actionUrl.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                if (announcement.actionType == "button") {
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(announcement.actionUrl))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Cannot open link", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = iconColor),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(text = announcement.actionText ?: "Open")
                    }
                } else if (announcement.actionType == "link") {
                    TextButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(announcement.actionUrl))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Cannot open link", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = announcement.actionText ?: "Learn More",
                            color = iconColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
