package com.yuch.snapcalfirebasegemini.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.yuch.snapcalfirebasegemini.R
import com.yuch.snapcalfirebasegemini.data.api.response.UserPreferences
import com.yuch.snapcalfirebasegemini.viewmodel.AuthState
import com.yuch.snapcalfirebasegemini.viewmodel.AuthViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.GetFoodViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.ProfileViewModel
import java.util.Locale
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    getFoodViewModel: GetFoodViewModel,
    profileViewModel: ProfileViewModel
) {
    val authState = authViewModel.authState.observeAsState()
    val email by authViewModel.userEmail.observeAsState("")
    val userPreferences by profileViewModel.userPreferences.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showOptionsBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(authState.value) {
        if (authState.value is AuthState.Unauthenticated) {
            navController.navigate("login") { popUpTo(0) }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF5B21B6), Color(0xFF9333EA)),
                    endY = 400f
                )
            )
    ) {
        Scaffold(
            topBar = {
                ProfileTopAppBar(
                    email = email,
                    onSignOut = { authViewModel.signout() },
                    onMoreOptions = { showOptionsBottomSheet = true } // Tambah menu options
                )
            },
            containerColor = Color.Transparent,
        ) { paddingValues ->
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                if (userPreferences?.personalInfo == null) {
                    EmptyStateCard(
                        message = stringResource(R.string.personal_info_empty),
                        onActionClick = {
                            navController.navigate("profile_onboarding?edit=false")
                        },
                        actionLabel = stringResource(R.string.fill_now)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .background(
                                color = Color(0xFFF9FAFB),
                                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                            ),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        item {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            // Header dengan quick actions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SectionHeader(stringResource(R.string.overview))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Quick edit button untuk personal info
                                    IconButton(
                                        onClick = { navController.navigate("profile_onboarding?edit=true&section=personal") },
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = stringResource(R.string.edit_personal_info),
                                            tint = Color(0xFF7C3AED),
                                        )
                                    }

                                }
                            }
                        }
                        userPreferences?.personalInfo?.let { info ->
                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    StatCard(
                                        modifier = Modifier.weight(1f),
                                        icon = Icons.Default.FitnessCenter,
                                        label = stringResource(R.string.weight),
                                        value = "${info.weight}",
                                        unit = stringResource(R.string.kg),
                                        color = Color(0xFF2563EB),
                                    )
                                    StatCard(
                                        modifier = Modifier.weight(1f),
                                        icon = Icons.Default.Height,
                                        label = stringResource(R.string.height),
                                        value = "${info.height}",
                                        unit = stringResource(R.string.cm),
                                        color = Color(0xFF16A34A),
                                    )
                                }
                            }
                            item {
                                BMICard(height = info.height, weight = info.weight)
                            }
                        }
                        item {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            SectionHeader(stringResource(R.string.my_preferences))
                        }
                        item {
                            AllPreferencesCard(
                                userPreferences = userPreferences,
                                onEditClick = {
                                    navController.navigate("profile_onboarding?edit=true&section=preferences")
                                }
                            )
                            Spacer(modifier = Modifier.height(76.dp))
                        }
                    }
                }
            }
        }
    }

    // Bottom Sheet untuk options menu
    if (showOptionsBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showOptionsBottomSheet = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // TODO: Belum ada opsi lain, bisa ditambahkan nanti
//                Text(
//                    "Profile Options",
//                    style = MaterialTheme.typography.titleLarge,
//                    fontWeight = FontWeight.Bold,
//                    modifier = Modifier.padding(bottom = 16.dp)
//                )

//                ProfileOptionItem(
//                    icon = Icons.Default.Edit,
//                    title = "Edit All Profile",
//                    subtitle = "Edit personal info and preferences",
//                    onClick = {
//                        showOptionsBottomSheet = false
//                        navController.navigate("profile_onboarding?edit=true")
//                    }
//                )

//                ProfileOptionItem(
//                    icon = Icons.Default.Refresh,
//                    title = "Reset Preferences",
//                    subtitle = "Reset dietary preferences only",
//                    onClick = {
//                        showOptionsBottomSheet = false
//                        // Implement reset preferences
////                        profileViewModel.resetPreferences()
//                    }
//                )

//                ProfileOptionItem(
//                    icon = Icons.Default.Download,
//                    title = "Export Data",
//                    subtitle = "Download your profile data",
//                    onClick = {
//                        showOptionsBottomSheet = false
//                        // Implement export functionality
////                        profileViewModel.exportData()
//                    }
//                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                ProfileOptionItem(
                    icon = Icons.Default.Delete,
                    title = stringResource(R.string.delete_profile),
                    subtitle = stringResource(R.string.delete_profile_subtitle),
                    onClick = {
                        showOptionsBottomSheet = false
                        showDeleteDialog = true
                    },
                    isDestructive = true
                )

                Spacer(modifier = Modifier.height(36.dp))
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_profile)) },
            text = {
                Text(stringResource(R.string.delete_profile_confirmation))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
//                        profileViewModel.deleteProfile()
//                        authViewModel.signout()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTopAppBar(
    email: String,
    onSignOut: () -> Unit,
    onMoreOptions: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Column {
                Text(stringResource(R.string.my_profile), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Text(email, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        },
        actions = {
            IconButton(onClick = onMoreOptions) {
                Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.more_options), tint = Color.White)
            }
            IconButton(onClick = onSignOut) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = stringResource(R.string.sign_out), tint = Color.White)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, titleContentColor = Color.White)
    )
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    unit: String,
    color: Color,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(label, fontSize = 12.sp, color = Color.Gray)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
                        Text(unit, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
                    }
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(color.copy(alpha = 0.15f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = color)
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    message: String,
    onActionClick: () -> Unit,
    actionLabel: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.8f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFFF57C00),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    message,
                    color = Color(0xFFF57C00),
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onActionClick) {
                    Text(actionLabel)
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, start = 4.dp)
    )
}

@Composable
fun BMICard(height: Int, weight: Int) {
    val bmi = if (height > 0) weight / (height / 100.0).pow(2) else 0.0
    val context = LocalContext.current
    val (category, color) = when {
        bmi < 18.5 -> context.getString(R.string.underweight) to Color(0xFF3B82F6)
        bmi < 25 -> context.getString(R.string.normal) to Color(0xFF10B981)
        bmi < 30 -> context.getString(R.string.overweight) to Color(0xFFF59E0B)
        else -> context.getString(R.string.obese) to Color(0xFFEF4444)
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.body_mass_index), fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(String.format(Locale.US, "%.1f", bmi), fontSize = 36.sp, fontWeight = FontWeight.Bold, color = color)
                Spacer(Modifier.width(12.dp))
                Text(category, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = color)
            }
        }
    }
}

@Composable
fun AllPreferencesCard(userPreferences: UserPreferences?, onEditClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.preferences_restrictions), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_preferences), tint = Color(0xFF7C3AED))
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            userPreferences?.let {
                PreferenceItem(Icons.Default.LocalHospital, stringResource(R.string.health_conditions), it.healthConditions + it.customHealthConditions)
                PreferenceItem(Icons.Default.Warning, stringResource(R.string.allergies), it.allergies + it.customAllergies)
                PreferenceItem(Icons.Default.RestaurantMenu, stringResource(R.string.dietary_types), it.dietaryRestrictions)
                PreferenceItem(Icons.Default.Favorite, stringResource(R.string.liked_foods), it.likedFoods)
                PreferenceItem(Icons.Default.ThumbDown, stringResource(R.string.disliked_foods), it.dislikedFoods)
            } ?: Text(stringResource(R.string.no_preferences_set), color = Color.Gray)
        }
    }
}

@Composable
fun PreferenceItem(icon: ImageVector, title: String, items: List<String>) {
    if (items.isNotEmpty()) {
        Column(Modifier.padding(bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEach { item ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, Color.LightGray)
                    ) {
                        Text(item.replaceFirstChar { it.titlecase(Locale.ROOT) },
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            fontSize = 12.sp, color = Color.DarkGray)
                    }
                }
            }
        }
    }
}

@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal,
    verticalArrangement: Arrangement.Vertical,
    content: @Composable () -> Unit
) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val hSpacing = horizontalArrangement.spacing.roundToPx()
        val vSpacing = verticalArrangement.spacing.roundToPx()
        val rows = mutableListOf<List<Placeable>>()
        val rowHeights = mutableListOf<Int>()
        var currentRow = mutableListOf<Placeable>()
        var currentWidth = 0
        var currentHeight = 0

        measurables.forEach { measurable ->
            val placeable = measurable.measure(constraints)
            if (currentWidth + placeable.width > constraints.maxWidth) {
                rows.add(currentRow); rowHeights.add(currentHeight)
                currentRow = mutableListOf()
                currentWidth = 0; currentHeight = 0
            }
            currentRow.add(placeable)
            currentWidth += placeable.width + hSpacing
            currentHeight = maxOf(currentHeight, placeable.height)
        }
        if (currentRow.isNotEmpty()) { rows.add(currentRow); rowHeights.add(currentHeight) }

        val totalHeight = rowHeights.sum() + maxOf(0, rows.size - 1) * vSpacing
        layout(constraints.maxWidth, totalHeight) {
            var y = 0
            rows.forEachIndexed { i, row ->
                var x = 0
                row.forEach { placeable ->
                    placeable.placeRelative(x, y)
                    x += placeable.width + hSpacing
                }
                y += rowHeights[i] + vSpacing
            }
        }
    }
}

@Composable
fun ProfileOptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    val textColor = if (isDestructive) Color.Red else Color.Black
    val iconColor = if (isDestructive) Color.Red else Color(0xFF7C3AED)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        iconColor.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = textColor
                )
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}