package com.yuch.snapcalfirebasegemini.view

import android.widget.Toast
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.yuch.snapcalfirebasegemini.ui.theme.ErrorRed
import com.yuch.snapcalfirebasegemini.ui.theme.LightBlue
import com.yuch.snapcalfirebasegemini.ui.theme.PrimaryBlue
import com.yuch.snapcalfirebasegemini.viewmodel.AuthState
import com.yuch.snapcalfirebasegemini.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    // State from ViewModel
    val email by authViewModel.emailInput.collectAsStateWithLifecycle()
    val password by authViewModel.passwordInput.collectAsStateWithLifecycle()
    
    // Local state for confirmation
    var confirmPassword by remember { mutableStateOf("") }
    
    // Visibility state
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // Validation from ViewModel
    val isEmailValid by authViewModel.isEmailValid.collectAsStateWithLifecycle()
    val isPasswordValid by authViewModel.isPasswordValid.collectAsStateWithLifecycle()

    // Local Validation
    val passwordsMatch by remember(password, confirmPassword) {
        derivedStateOf {
            password == confirmPassword || confirmPassword.isEmpty()
        }
    }

    // Combine validation for button enablement
    val isRegisterFormValid by remember(isEmailValid, isPasswordValid, passwordsMatch, confirmPassword) {
        derivedStateOf {
            isEmailValid && isPasswordValid && 
            password == confirmPassword && 
            confirmPassword.isNotEmpty()
        }
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> navController.navigate(Screen.Main.route) {
                popUpTo(0)
            }
            is AuthState.EmailNotVerified -> {
                // Handle case where email verification is required (optional to handle here if needed)
                // Usually LoginScreen handles the blocking, but RegisterScreen might show "Check email"
                Toast.makeText(context, "Silakan cek email Anda untuk verifikasi.", Toast.LENGTH_LONG).show()
                // Navigate to Login or stay? If we enforced "No entry", we should go to Login or show a dialog.
                // Assuming we want to redirect them to Login to "wait".
                navController.navigate(Screen.Login.route) {
                    popUpTo(0)
                }
            }
            is AuthState.Error -> Toast.makeText(
                context,
                (authState as AuthState.Error).message,
                Toast.LENGTH_SHORT
            ).show()
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
                        PrimaryBlue,
                        LightBlue
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
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Judul Register
                        Text(
                            text = stringResource(R.string.register_title),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )

                        Text(
                            text = stringResource(R.string.register_subtitle),
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Email Field
                        OutlinedTextField(
                            value = email,
                            onValueChange = { authViewModel.onEmailChange(it) },
                            label = { Text("Email") },
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email",
                                    tint = if (isEmailValid) PrimaryBlue else ErrorRed
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isEmailValid) PrimaryBlue else ErrorRed,
                                unfocusedBorderColor = if (isEmailValid) Color.Gray else ErrorRed,
                                focusedLabelColor = if (isEmailValid) PrimaryBlue else ErrorRed,
                                cursorColor = PrimaryBlue
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
                                        color = ErrorRed,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        )

                        // Password Field
                        OutlinedTextField(
                            value = password,
                            onValueChange = { authViewModel.onPasswordChange(it) },
                            label = { Text("Password") },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = "Password Icon",
                                    tint = if (isPasswordValid) PrimaryBlue else ErrorRed
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle Password Visibility",
                                        tint = PrimaryBlue
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isPasswordValid) PrimaryBlue else ErrorRed,
                                unfocusedBorderColor = if (isPasswordValid) Color.Gray else ErrorRed,
                                focusedLabelColor = if (isPasswordValid) PrimaryBlue else ErrorRed,
                                cursorColor = PrimaryBlue
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
                                        color = ErrorRed,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        )

                        // Confirm Password Field
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text(stringResource(R.string.register_confirm_password_label)) },
                            singleLine = true,
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = "Confirm Password Icon",
                                    tint = if (passwordsMatch) PrimaryBlue else ErrorRed
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle Confirm Password Visibility",
                                        tint = PrimaryBlue
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (passwordsMatch) PrimaryBlue else ErrorRed,
                                unfocusedBorderColor = if (passwordsMatch) Color.Gray else ErrorRed,
                                focusedLabelColor = if (passwordsMatch) PrimaryBlue else ErrorRed,
                                cursorColor = PrimaryBlue
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    if (isRegisterFormValid) {
                                        authViewModel.signup()
                                    }
                                }
                            ),
                            isError = !passwordsMatch && confirmPassword.isNotEmpty(),
                            supportingText = {
                                if (!passwordsMatch && confirmPassword.isNotEmpty()) {
                                    Text(
                                        text = "Password tidak cocok",
                                        color = ErrorRed,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Register Button
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                authViewModel.signup()
                            },
                            enabled = isRegisterFormValid && authState != AuthState.Loading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryBlue,
                                disabledContainerColor = PrimaryBlue.copy(alpha = 0.5f)
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 8.dp,
                                disabledElevation = 0.dp
                            )
                        ) {
                            if (authState == AuthState.Loading) {
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

                        // Login Button
                        Row(
                            modifier = Modifier
                                .clickable { navController.navigate(Screen.Login.route) }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${stringResource(R.string.register_already_have_account)} ",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                            Text(
                                stringResource(R.string.register_login_here),
                                color = PrimaryBlue,
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
