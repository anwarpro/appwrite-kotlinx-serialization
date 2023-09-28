package com.helloanwar.kmmapplication

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.helloanwar.kmmapplication.ui.dashboard.Dashboard
import com.helloanwar.kmmapplication.ui.login.LoginScreen
import com.helloanwar.kmmapplication.ui.navigation.Screen
import com.helloanwar.kmmapplication.ui.signup.SignupScreen
import com.helloanwar.kmmapplication.ui.theme.KmmApplicationTheme
import com.helloanwar.kmmapplication.ui.utils.NetworkResponse
import io.appwrite.Client
import io.appwrite.models.Session
import io.appwrite.services.Account

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            KmmApplicationTheme {
                // A surface container using the 'background' color from the theme
                AppScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        var currentScreen by remember { mutableStateOf(Screen.LOGIN) }
        val context = LocalContext.current

        var currentSession by remember {
            mutableStateOf<NetworkResponse<Session>>(NetworkResponse.Idle)
        }

        val client = remember {
            Client(context)
                .setEndpoint("https://cloud.appwrite.io/v1")
                .setProject("64d715cd9e92b3bd6d29")
        }

        LaunchedEffect(Unit) {
            val account = Account(client)
            try {
                currentSession = NetworkResponse.Loading
                val response = account.getSession("current")
                currentSession = NetworkResponse.Success(response)
                currentScreen = Screen.DASHBOARD
            } catch (e: Exception) {
                e.printStackTrace()
                currentSession = NetworkResponse.Failure(e.localizedMessage ?: "")
                currentScreen = Screen.LOGIN
            }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
                verticalArrangement = Arrangement.Center
            ) {
                if (currentSession is NetworkResponse.Idle || currentSession is NetworkResponse.Loading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    //show loading or splash until get session
                    when (currentScreen) {
                        Screen.LOGIN -> {
                            LoginScreen(
                                onSignup = {
                                    currentScreen = Screen.SIGNUP
                                },
                                onDashboard = {
                                    currentSession = NetworkResponse.Success(it)
                                    Toast.makeText(context, "Login success", Toast.LENGTH_SHORT)
                                        .show()
                                    currentScreen = Screen.DASHBOARD
                                },
                                account = Account(client)
                            )
                        }

                        Screen.SIGNUP -> {
                            SignupScreen(
                                onLogin = {
                                    currentScreen = Screen.LOGIN
                                },
                                onSuccess = {
                                    Toast.makeText(context, "Signup success", Toast.LENGTH_SHORT)
                                        .show()
                                    currentScreen = Screen.DASHBOARD
                                },
                                account = Account(client)
                            )
                        }

                        Screen.DASHBOARD -> {
                            Dashboard(
                                onLogin = {
                                    currentScreen = Screen.LOGIN
                                },
                                account = Account(client)
                            )
                        }
                    }
                }
            }
        }
    }
}


@Preview
@Composable
fun AppPreview() {
    KmmApplicationTheme {
        AppScreen()
    }
}