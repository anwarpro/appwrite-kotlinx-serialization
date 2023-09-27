package com.helloanwar.kmmapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.helloanwar.kmmapplication.ui.theme.KmmApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KmmApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf(Screen.LOGIN) }

                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        when (currentScreen) {
                            Screen.LOGIN -> {
                                LoginScreen(
                                    onSignup = {
                                        currentScreen = Screen.SIGNUP
                                    },
                                    onDashboard = {
                                        currentScreen = Screen.SIGNUP
                                    }
                                )
                            }

                            Screen.SIGNUP -> {
                                SignupScreen(
                                    onLogin = {
                                        currentScreen = Screen.LOGIN
                                    }
                                )
                            }

                            Screen.DASHBOARD -> {
                                Dashboard(
                                    onLogin = {
                                        currentScreen = Screen.LOGIN
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class Screen {
    LOGIN, SIGNUP, DASHBOARD
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColumnScope.LoginScreen(
    onSignup: () -> Unit,
    onDashboard: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    Text(text = "Login")
    OutlinedTextField(
        value = email,
        onValueChange = {
            email = it
        }
    )
    OutlinedTextField(
        value = password,
        onValueChange = {
            password = it
        }
    )

    Button(
        onClick = {

        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp)
    ) {
        Text(text = "Sign in")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColumnScope.SignupScreen(
    onLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    Text(text = "Sign up")
    OutlinedTextField(
        value = name,
        onValueChange = {
            name = it
        }
    )
    OutlinedTextField(
        value = email,
        onValueChange = {
            email = it
        }
    )
    OutlinedTextField(
        value = password,
        onValueChange = {
            password = it
        }
    )

    TextButton(onClick = {
        onLogin()
    }) {
        Text(text = "Already have a account ?", style = MaterialTheme.typography.bodySmall)
    }

    Button(
        onClick = {

        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp)
    ) {
        Text(text = "Sign up")
    }
}

@Composable
fun ColumnScope.Dashboard(
    onLogin: () -> Unit
) {
    Text(text = "Dashboard")
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    KmmApplicationTheme {
        Column {
            LoginScreen(onSignup = {}, onDashboard = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpPreview() {
    KmmApplicationTheme {
        Column {
            SignupScreen(onLogin = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    KmmApplicationTheme {
        Column {
            Dashboard(onLogin = {})
        }
    }
}