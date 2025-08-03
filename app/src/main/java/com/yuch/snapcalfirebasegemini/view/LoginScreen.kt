package com.yuch.snapcalfirebasegemini.view

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.yuch.snapcalfirebasegemini.R
import com.yuch.snapcalfirebasegemini.ui.navigation.Screen
import com.yuch.snapcalfirebasegemini.viewmodel.AuthState
import com.yuch.snapcalfirebasegemini.viewmodel.AuthViewModel
import kotlin.random.Random

@Composable
fun LoginScreen(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // Real-time validation dengan optimasi menggunakan derivedStateOf
    val isEmailValid by remember(email) {
        derivedStateOf {
            email.trim().matches(Regex("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$")) || email.isEmpty()
        }
    }

    val isPasswordValid by remember(password) {
        derivedStateOf {
            password.length >= 6 || password.isEmpty()
        }
    }

    val isFormValid by remember(email, password) {
        derivedStateOf {
            email.trim().matches(Regex("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$")) &&
            password.length >= 6
        }
    }

    LaunchedEffect(authState.value) {
        when(authState.value){
            is AuthState.Authenticated -> navController.navigate(Screen.Main.route){
                popUpTo(0)
            }
            is AuthState.Error -> Toast.makeText(context,
                (authState.value as AuthState.Error).message, Toast.LENGTH_SHORT).show()
            else -> Unit
        }
    }

    // Edge-to-edge styling seperti MainScreen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A237E),
                        Color(0xFF3949AB)
                    ),
                    endY = 400f
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                // Lingkaran dekoratif di background - positioned above the white background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(y = (-200).dp)
                ) {
                    val circlePositions = remember {
                        List(15) {
                            Triple(
                                (80..140).random().dp,
                                (-30..350).random().dp to (-30..700).random().dp,
                                Random.nextFloat() * (0.08f - 0.03f) + 0.03f
                            )
                        }
                    }

                    circlePositions.forEach { (size, position, alpha) ->
                        Box(
                            modifier = Modifier
                                .size(size)
                                .offset(x = position.first, y = position.second)
                                .background(
                                    color = Color.White.copy(alpha = alpha),
                                    shape = CircleShape
                                )
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .graphicsLayer {
                            alpha = 0.8f
                            shadowElevation = 8f
                        },
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.9f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Logo aplikasi
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    color = Color(0xFF1A237E),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Camera,
                                contentDescription = "SnapCal Logo",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Text(
                            text = stringResource(R.string.welcome_login),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A237E)
                        )

                        Text(
                            text = stringResource(R.string.login_subtitle),
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )

                        // Email field dengan validasi real-time
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email",
                                    tint = if (isEmailValid) Color(0xFF1A237E) else Color(0xFFE53E3E)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isEmailValid) Color(0xFF1A237E) else Color(0xFFE53E3E),
                                unfocusedBorderColor = if (isEmailValid) Color.Gray else Color(0xFFE53E3E),
                                focusedLabelColor = if (isEmailValid) Color(0xFF1A237E) else Color(0xFFE53E3E),
                                cursorColor = Color(0xFF1A237E)
                            ),
                            isError = !isEmailValid && email.isNotEmpty(),
                            supportingText = {
                                if (!isEmailValid && email.isNotEmpty()) {
                                    Text(
                                        text = "Format email tidak valid",
                                        color = Color(0xFFE53E3E),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        )

                        // Password field dengan validasi real-time
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Password",
                                    tint = if (isPasswordValid) Color(0xFF1A237E) else Color(0xFFE53E3E)
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle Password Visibility",
                                        tint = Color(0xFF1A237E)
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    if (isFormValid) {
                                        authViewModel.login(email, password)
                                    }
                                }
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isPasswordValid) Color(0xFF1A237E) else Color(0xFFE53E3E),
                                unfocusedBorderColor = if (isPasswordValid) Color.Gray else Color(0xFFE53E3E),
                                focusedLabelColor = if (isPasswordValid) Color(0xFF1A237E) else Color(0xFFE53E3E),
                                cursorColor = Color(0xFF1A237E)
                            ),
                            isError = !isPasswordValid && password.isNotEmpty(),
                            supportingText = {
                                if (!isPasswordValid && password.isNotEmpty()) {
                                    Text(
                                        text = "Password minimal 6 karakter",
                                        color = Color(0xFFE53E3E),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        )

                        Text(
                            text = stringResource(R.string.forgot_password),
                            modifier = Modifier
                                .align(Alignment.End)
                                .clickable { navController.navigate("forgot-password") },
                            color = Color(0xFF1A237E),
                            fontSize = 12.sp
                        )

                        // Login button dengan optimasi state
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                authViewModel.login(email, password)
                            },
                            enabled = isFormValid && authState.value != AuthState.Loading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1A237E),
                                disabledContainerColor = Color(0xFF1A237E).copy(alpha = 0.5f)
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 8.dp,
                                disabledElevation = 0.dp
                            )
                        ) {
                            if (authState.value == AuthState.Loading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    stringResource(R.string.login_button),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Register option
                        Row(
                            modifier = Modifier
                                .clickable { navController.navigate("register") }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                stringResource(R.string.dont_have_account),
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                            Text(
                                " ${stringResource(R.string.register_here)}",
                                color = Color(0xFF1A237E),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}