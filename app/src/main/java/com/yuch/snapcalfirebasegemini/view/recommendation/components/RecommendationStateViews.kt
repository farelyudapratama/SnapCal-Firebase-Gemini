package com.yuch.snapcalfirebasegemini.view.recommendation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.yuch.snapcalfirebasegemini.R
import com.yuch.snapcalfirebasegemini.ui.navigation.Screen

@Composable
fun EmptyRecommendationState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.no_recommendations_available),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.try_refreshing_different_meal),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ErrorRecommendationState(
    message: String,
    onRetry: () -> Unit,
    navController: NavController? = null
) {
    val isProfileDataMissing = message.contains("400", ignoreCase = true) ||
            message.contains("profile", ignoreCase = true) ||
            message.contains("personal info", ignoreCase = true) ||
            message.contains("preferences", ignoreCase = true) ||
            message.contains("data tidak lengkap", ignoreCase = true) ||
            message.contains("silakan lengkapi", ignoreCase = true)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isProfileDataMissing) Color(0xFFFEF3C7) else Color(0xFFFEF2F2)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (isProfileDataMissing) Icons.Default.Person else Icons.Default.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = if (isProfileDataMissing) Color(0xFFD97706) else Color(0xFFDC2626)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isProfileDataMissing)
                    stringResource(R.string.profile_incomplete_title)
                else
                    stringResource(R.string.something_went_wrong),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isProfileDataMissing) Color(0xFFD97706) else Color(0xFFDC2626),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isProfileDataMissing)
                    stringResource(R.string.profile_incomplete_message)
                else
                    message,
                style = MaterialTheme.typography.bodySmall,
                color = if (isProfileDataMissing) Color(0xFFA16207) else Color(0xFF991B1B),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isProfileDataMissing && navController != null) {
                Button(
                    onClick = {
                        navController.navigate(Screen.ProfileOnboarding.createRoute(edit = false))
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD97706)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.complete_profile),
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onRetry) {
                    Text(
                        stringResource(R.string.try_again),
                        color = Color(0xFFD97706)
                    )
                }
            } else {
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDC2626)
                    )
                ) {
                    Text(stringResource(R.string.try_again), color = Color.White)
                }
            }
        }
    }
}
