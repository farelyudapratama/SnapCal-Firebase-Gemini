package com.yuch.snapcalfirebasegemini.view

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
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

@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

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

    val passwordsMatch by remember(password, confirmPassword) {
        derivedStateOf {
            password == confirmPassword || confirmPassword.isEmpty()
        }
    }

    val isFormValid by remember(email, password, confirmPassword) {
        derivedStateOf {
            email.trim().matches(Regex("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$")) &&
            password.length >= 6 &&
            password == confirmPassword &&
            confirmPassword.isNotEmpty()
        }
    }

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated -> navController.navigate(Screen.Main.route) {
                popUpTo(0)
            }
            is AuthState.Error -> Toast.makeText(
                context,
                (authState.value as AuthState.Error).message,
                Toast.LENGTH_SHORT
            ).show()
            else -> Unit
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A237E),
                        Color(0xFF3949AB)
                    )
                )
            )
    ) {
        // Card dengan efek kaca (tanpa blur) - tetap mempertahankan tampilan asli
        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .align(Alignment.Center),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.2f) // Semi transparan
            ),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // **Judul Register**
                Text(
                    text = stringResource(R.string.register_title),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = stringResource(R.string.register_subtitle),
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // **Email Field dengan validasi real-time**
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email", color = Color.White) },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email",
                            tint = if (isEmailValid) Color.White else Color(0xFFFF6363)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = TextStyle(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isEmailValid) Color.White else Color(0xFFFF6363),
                        unfocusedBorderColor = if (isEmailValid) Color.White.copy(alpha = 0.5f) else Color(0xFFFF6363),
                        cursorColor = Color.White,
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    isError = !isEmailValid && email.isNotEmpty(),
                    supportingText = {
                        if (!isEmailValid && email.isNotEmpty()) {
                            Text(
                                text = "Format email tidak valid",
                                color = Color(0xFFFFCECE),
                                fontSize = 12.sp
                            )
                        }
                    }
                )

                // **Password Field dengan validasi real-time**
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = Color.White) },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    leadingIcon = {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Password Icon",
                            tint = if (isPasswordValid) Color.White else Color(0xFFFF6363)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Password Visibility",
                                tint = Color.White
                            )
                        }
                    },
                    textStyle = TextStyle(color = Color.White),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isPasswordValid) Color.White else Color(0xFFFF6363),
                        unfocusedBorderColor = if (isPasswordValid) Color.White.copy(alpha = 0.5f) else Color(0xFFFF6363),
                        cursorColor = Color.White,
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    isError = !isPasswordValid && password.isNotEmpty(),
                    supportingText = {
                        if (!isPasswordValid && password.isNotEmpty()) {
                            Text(
                                text = "Password minimal 6 karakter",
                                color = Color(0xFFFF6363),
                                fontSize = 12.sp
                            )
                        }
                    }
                )

                // **Confirm Password Field dengan validasi real-time**
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text(stringResource(R.string.register_confirm_password_label), color = Color.White) },
                    singleLine = true,
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    leadingIcon = {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Confirm Password Icon",
                            tint = if (passwordsMatch) Color.White else Color(0xFFFF6363)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Confirm Password Visibility",
                                tint = Color.White
                            )
                        }
                    },
                    textStyle = TextStyle(color = Color.White),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (passwordsMatch) Color.White else Color(0xFFFF6363),
                        unfocusedBorderColor = if (passwordsMatch) Color.White.copy(alpha = 0.5f) else Color(0xFFFF6363),
                        cursorColor = Color.White,
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            if (isFormValid) {
                                authViewModel.signup(email, password)
                            }
                        }
                    ),
                    isError = !passwordsMatch && confirmPassword.isNotEmpty(),
                    supportingText = {
                        if (!passwordsMatch && confirmPassword.isNotEmpty()) {
                            Text(
                                text = "Password tidak cocok",
                                color = Color(0xFFFF6363),
                                fontSize = 12.sp
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // **Register Button dengan optimasi state**
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        authViewModel.signup(email, password)
                    },
                    enabled = isFormValid && authState.value != AuthState.Loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF06378D),
                        disabledContainerColor = Color(0xFF06378D).copy(alpha = 0.5f)
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
                            stringResource(R.string.register_button),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // **Divider dengan teks "Atau"**
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }

                // **Login Button**
                Row(
                    modifier = Modifier
                        .clickable { navController.navigate("login") }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${stringResource(R.string.register_already_have_account)} ",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Text(
                        stringResource(R.string.register_login_here),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}