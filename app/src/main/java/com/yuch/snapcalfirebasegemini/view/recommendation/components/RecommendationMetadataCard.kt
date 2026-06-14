package com.yuch.snapcalfirebasegemini.view.recommendation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yuch.snapcalfirebasegemini.R
import com.yuch.snapcalfirebasegemini.data.api.response.RecommendationMetadata
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun RecommendationMetadataCard(metadata: RecommendationMetadata) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (metadata.fallbackUsed) Icons.Default.Warning else Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (metadata.fallbackUsed) Color(0xFFF59E0B) else Color(0xFF0369A1),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = metadata.modelUsed,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0369A1)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (metadata.fallbackUsed) {
                                Text(
                                    text = stringResource(R.string.fallback_mode),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFF59E0B)
                                )
                            }
                            if (metadata.fromCache) {
                                Text(
                                    text = stringResource(R.string.from_cache),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF059669)
                                )
                            }
                        }
                    }
                }

                val formatter = DateTimeFormatter.ofPattern("MMM dd, HH:mm")
                val dateTime = try {
                    Instant.parse(metadata.generatedAt)
                        .atZone(ZoneId.systemDefault())
                        .format(formatter)
                } catch (e: Exception) {
                    "Recently"
                }

                Text(
                    text = dateTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF0369A1)
                )
            }
        }
    }
}
