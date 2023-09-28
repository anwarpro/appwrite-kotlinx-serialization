package com.helloanwar.kmmapplication.ui.signup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.helloanwar.kmmapplication.ui.theme.KmmApplicationTheme
import com.helloanwar.kmmapplication.ui.utils.NetworkResponse
import io.appwrite.ID
import io.appwrite.models.User
import io.appwrite.services.Account
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColumnScope.SignupScreen(
    onLogin: () -> Unit,
    onSuccess: () -> Unit,
    account: Account? = null
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    var signupResponse by remember {
        mutableStateOf<NetworkResponse<User<Map<String, Any>>>>(NetworkResponse.Idle)
    }

    val scope = rememberCoroutineScope()

    Text(
        text = "Sign up",
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = name,
        onValueChange = {
            name = it
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        label = {
            Text(text = "Name")
        }
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Already have a account ?", style = MaterialTheme.typography.bodySmall)
        TextButton(
            onClick = {
                onLogin()
            }
        ) {
            Text(text = "Login", style = MaterialTheme.typography.bodySmall)
        }
    }

    AnimatedVisibility(
        visible = signupResponse is NetworkResponse.Failure
    ) {
        if (signupResponse is NetworkResponse.Failure) {
            val error = (signupResponse as NetworkResponse.Failure).error
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.error
                ),
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    Button(
        onClick = {
            scope.launch {
                if (email.isBlank()) {
                    signupResponse = NetworkResponse.Failure("Email can't be blank")
                    return@launch
                }
                if (password.isBlank()) {
                    signupResponse = NetworkResponse.Failure("Password can't be blank")
                    return@launch
                }
                try {
                    signupResponse = NetworkResponse.Loading
                    val session = account!!.create(
                        userId = ID.unique(),
                        email = email,
                        password = password,
                        name = name
                    )
                    signupResponse = NetworkResponse.Success(data = session)
                } catch (e: Exception) {
                    e.printStackTrace()
                    signupResponse =
                        NetworkResponse.Failure(e.localizedMessage ?: "no error message")
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp),
        enabled = signupResponse == NetworkResponse.Idle || signupResponse is NetworkResponse.Failure
    ) {
        when (signupResponse) {
            is NetworkResponse.Loading -> {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }

            is NetworkResponse.Failure,
            NetworkResponse.Idle -> {
                Text(text = "Sign up")
            }

            is NetworkResponse.Success -> {
                //success
                onSuccess()
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun SignUpPreview() {
    KmmApplicationTheme {
        Column {
            SignupScreen(onLogin = {}, onSuccess = {})
        }
    }
}