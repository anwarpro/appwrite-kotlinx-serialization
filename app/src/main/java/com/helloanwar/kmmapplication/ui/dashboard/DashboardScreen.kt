package com.helloanwar.kmmapplication.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import io.appwrite.models.User
import io.appwrite.services.Account
import kotlinx.coroutines.launch


@Composable
fun ColumnScope.Dashboard(
    onLogin: () -> Unit,
    account: Account? = null
) {
    var userResponse by remember {
        mutableStateOf<NetworkResponse<User<Map<String, Any>>>>(NetworkResponse.Idle)
    }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        try {
            userResponse = NetworkResponse.Loading
            val user = account!!.get()
            userResponse = NetworkResponse.Success(user)
        } catch (e: Exception) {
            e.printStackTrace()
            userResponse = NetworkResponse.Failure(e.localizedMessage ?: "")
            //sent to login page again if not network issues
            onLogin()
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "Dashboard",
        modifier = Modifier.padding(horizontal = 16.dp),
        style = MaterialTheme.typography.titleSmall
    )
    Spacer(modifier = Modifier.height(16.dp))
    when (val data = userResponse) {
        is NetworkResponse.Failure -> {
            Text(
                text = data.error, modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.error)
            )
        }

        NetworkResponse.Idle,
        NetworkResponse.Loading -> {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        is NetworkResponse.Success -> {
            //show users details
        }
    }
    AnimatedVisibility(
        visible = userResponse is NetworkResponse.Success,
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
    ) {
        val user = (userResponse as NetworkResponse.Success).data
        Text(
            text = "Welcome, ${user.name}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(
            modifier = Modifier
                .height(16.dp)
        )
        TextButton(
            onClick = {
                scope.launch {
                    try {
                        account!!.deleteSession("current")
                        //go login page
                        onLogin()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
            },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text(text = "Logout", color = MaterialTheme.colorScheme.error)
        }
    }
    Spacer(
        modifier = Modifier
            .weight(1f)
    )
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