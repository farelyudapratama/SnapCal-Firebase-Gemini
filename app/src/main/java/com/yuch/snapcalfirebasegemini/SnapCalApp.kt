package com.yuch.snapcalfirebasegemini

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yuch.snapcalfirebasegemini.ui.navigation.Screen
import com.yuch.snapcalfirebasegemini.viewmodel.AnnouncementViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.AuthViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.CameraViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.GetFoodViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.OnboardingViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.ProfileViewModel

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun SnapCalApp(
    authViewModel: AuthViewModel,
    cameraViewModel: CameraViewModel,
    getFoodViewModel: GetFoodViewModel,
    profileViewModel: ProfileViewModel,
    onboardingViewModel: OnboardingViewModel,
    announcementViewModel: AnnouncementViewModel? = null
) {
    val navController = rememberNavController()

    val screensWithoutBottomBar = listOf(
        Screen.Login.route,
        Screen.Register.route,
        Screen.ForgotPassword.route,
        Screen.Scan.route,
        Screen.Analyze.route,
        Screen.ManualEntry.route,
        Screen.DetailFood.route,
        Screen.EditFood.route,
        Screen.AiChat.route,
        Screen.ProfileOnboarding.route,
        Screen.Recommendation.route,
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // State untuk membuka ModalBottomSheet
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
        Scaffold(
            bottomBar = {
                if (currentRoute !in screensWithoutBottomBar) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        BottomNavbar(navController = navController)
                    }
                }
            },
            floatingActionButton = {
                when (currentRoute) {
                    Screen.Main.route -> {
                        FloatingActionButton(
                            onClick = { showBottomSheet = true },
                            containerColor = Color(0xFFFF5722),
                            elevation = FloatingActionButtonDefaults.elevation(
                                defaultElevation = 6.dp,
                                pressedElevation = 12.dp
                            ),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Meal",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Screen.Tracking.route -> {
                        FloatingActionButton(
                            onClick = {
                                navController.navigate(Screen.AiChat.route)
                            },
                            containerColor = Color(0xFF2196F3),
                            elevation = FloatingActionButtonDefaults.elevation(
                                defaultElevation = 6.dp,
                                pressedElevation = 12.dp
                            ),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Message,
                                contentDescription = "Message Ai",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Screen.Profile.route -> {
                        // FAB tidak muncul di halaman Profile
                    }
                }
            },
            floatingActionButtonPosition = FabPosition.End,
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            AppNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
                authViewModel = authViewModel,
                cameraViewModel = cameraViewModel,
                getFoodViewModel = getFoodViewModel,
                profileViewModel = profileViewModel,
                onboardingViewModel = onboardingViewModel,
                announcementViewModel = announcementViewModel
            )
        }
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = {
                            navController.navigate(Screen.Scan.route)
                            showBottomSheet = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Scan",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.fab_add_food_entry_ai),
                            fontSize = 16.sp,
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            navController.navigate(Screen.ManualEntry.route)
                            showBottomSheet = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Isi Manual",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.fab_add_food_entry_manual),
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavbar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Surface(
        color = Color.White,
        shadowElevation = 8.dp,
    ) {
        NavigationBar(
            containerColor = Color.White,
            modifier = modifier.height(60.dp),
            tonalElevation = 0.dp
        ) {
            // Home Item
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Beranda",
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = stringResource(R.string.bottom_navigation_home),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                },
                selected = currentRoute == Screen.Main.route,
                onClick = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                )
            )

            // Track Item
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.BarChart,
                        contentDescription = "Tracking",
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = stringResource(R.string.bottom_navigation_tracking),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                },
                selected = currentRoute == Screen.Tracking.route,
                onClick = {
                    navController.navigate(Screen.Tracking.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Blue,
                    selectedTextColor = Color.Blue,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color.White
                )
            )

            // Profile Item
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Akun",
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = stringResource(R.string.bottom_navigation_profile),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                },
                selected = currentRoute == Screen.Profile.route,
                onClick = {
                    navController.navigate(Screen.Profile.route){
                        popUpTo(navController.graph.startDestinationId) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Blue,
                    selectedTextColor = Color.Blue,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color.White
                )
            )
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun PreviewSnapCalApp() {
//    // Menampilkan preview tampilan dengan parameter default (bisa ganti dengan data sebenarnya)
//    SnapCalApp(authViewModel = AuthViewModel(), cameraViewModel = CameraViewModel(), foodViewModel = FoodViewModel(
//        apiService = TODO(),
//        repository = TODO()
//    ))
//}