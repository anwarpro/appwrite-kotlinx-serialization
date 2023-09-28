package com.helloanwar.kmmapplication.ui.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.helloanwar.kmmapplication.ui.theme.KmmApplicationTheme
import com.helloanwar.kmmapplication.ui.utils.NetworkResponse
import io.appwrite.models.Session
import io.appwrite.services.Account
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColumnScope.LoginScreen(
    onSignup: () -> Unit,
    onDashboard: (Session) -> Unit,
    account: Account? = null
) {
    var email by remember { mutableStateOf("anwar.hussen.pro@gmail.com") }
    var password by remember { mutableStateOf("testtest") }
    var loginResponse by remember {
        mutableStateOf<NetworkResponse<Session>>(NetworkResponse.Idle)
    }

    val scope = rememberCoroutineScope()

    Text(
        text = "Login",
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = email,
        onValueChange = {
            email = it
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        label = {
            Text(text = "Email")
        }
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = password,
        onValueChange = {
            password = it
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        label = {
            Text(text = "Password")
        }
    )
    TextButton(
        onClick = {
            onSignup()
        },
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Create an account",
            style = MaterialTheme.typography.bodySmall
        )
    }

    AnimatedVisibility(
        visible = loginResponse is NetworkResponse.Failure
    ) {
        if (loginResponse is NetworkResponse.Failure) {
            val error = (loginResponse as NetworkResponse.Failure).error
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.error
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    Button(
        onClick = {
            scope.launch {
                if (email.isBlank()) {
                    loginResponse = NetworkResponse.Failure("Email can't be blank")
                    return@launch
                }
                if (password.isBlank()) {
                    loginResponse = NetworkResponse.Failure("Password can't be blank")
                    return@launch
                }
                try {
                    loginResponse = NetworkResponse.Loading
                    val user = account!!.createEmailSession(email, password)
                    loginResponse = NetworkResponse.Success(data = user)
                    onDashboard(user)
                } catch (e: Exception) {
                    e.printStackTrace()
                    loginResponse = NetworkResponse.Failure(e.localizedMessage ?: "")
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp, vertical = 16.dp),
        enabled = loginResponse == NetworkResponse.Idle || loginResponse is NetworkResponse.Failure
    ) {
        when (loginResponse) {
            is NetworkResponse.Loading -> {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }

            is NetworkResponse.Failure,
            NetworkResponse.Idle -> {
                Text(text = "Sign in")
            }

            is NetworkResponse.Success -> {

            }
        }
    }
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