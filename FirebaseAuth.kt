
package com.kcoinrush.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun FirebaseAuthScreen(
    onLoginSuccess: () -> Unit
) {
    val auth = remember { FirebaseAuth.getInstance() }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isRegister by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "KCoinRush",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            enabled = !loading,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                if (email.isBlank() || password.length < 6) {
                    message = "Enter a valid email and password (6+ characters)."
                } else {
                    loading = true

                    val task = if (isRegister) {
                        auth.createUserWithEmailAndPassword(
                            email.trim(), password
                        )
                    } else {
                        auth.signInWithEmailAndPassword(
                            email.trim(), password
                        )
                    }

                    task.addOnCompleteListener { result ->
                        loading = false

                        if (result.isSuccessful) {
                            onLoginSuccess()
                        } else {
                            message = result.exception?.localizedMessage
                                ?: "Authentication failed."
                        }
                    }
                }
            }
        ) {
            Text(
                if (loading) "Please wait..."
                else if (isRegister) "Create account"
                else "Login"
            )
        }

        TextButton(onClick = {
            isRegister = !isRegister
            message = ""
        }) {
            Text(
                if (isRegister) "Already registered? Login"
                else "Create a new account"
            )
        }

        Text(message)
    }
}
