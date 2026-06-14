package com.yuch.snapcalfirebasegemini.view.nutritrack

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.NoFood
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yuch.snapcalfirebasegemini.R

@Composable
fun NutriTrackHeader(date: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(DarkGreenPrimary, MediumGreenSecondary)
                )
            )
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Column(modifier = Modifier.align(Alignment.CenterStart)) {
            Text(
                text = stringResource(R.string.nutritrack_title),
                color = TextOnDark,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = date,
                color = TextOnDark.copy(alpha = 0.8f),
                fontSize = 16.sp
            )
        }
        Icon(
            imageVector = Icons.Default.Restaurant,
            contentDescription = null,
            tint = TextOnDark.copy(alpha = 0.3f),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(60.dp)
        )
    }
}

@Composable
fun NutriTrackTabNavigation(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf(stringResource(R.string.tab_today), stringResource(R.string.tab_weekly))
    val icons = listOf(Icons.Default.Dashboard, Icons.Default.Timeline)

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(LightGreenBackground)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items(tabs.size) { index ->
            NutriTrackTabItem(
                title = tabs[index],
                icon = icons[index],
                isSelected = selectedTab == index,
                onClick = { onTabSelected(index) }
            )
        }
    }
}

@Composable
private fun NutriTrackTabItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val animatedColor by animateColorAsState(
        targetValue = if (isSelected) DarkGreenPrimary else TextSecondaryOnLight,
        animationSpec = tween(300),
        label = "tab_icon_color"
    )
    val animatedTextColor by animateColorAsState(
        targetValue = if (isSelected) DarkGreenPrimary else TextSecondaryOnLight,
        animationSpec = tween(300),
        label = "tab_text_color"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = animatedColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            color = animatedTextColor,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(20.dp)
                    .height(2.dp)
                    .background(
                        color = DarkGreenPrimary,
                        shape = RoundedCornerShape(1.dp)
                    )
            )
        }
    }
}

@Composable
fun NutriTrackLoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = TextOnDark,
                strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                stringResource(R.string.loading_nutrition_data),
                color = TextOnDark,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun NutriTrackEmptyDataScreen(onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.NoFood,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = TextOnDark.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                stringResource(R.string.no_nutrition_data_available),
                color = TextOnDark,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                stringResource(R.string.add_food_entries_to_see_tracking),
                color = TextOnDark.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TextOnDark,
                    contentColor = DarkGreenPrimary
                )
            ) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}
