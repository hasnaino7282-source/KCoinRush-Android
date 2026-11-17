
package com.kcoinrush.app

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ReferralScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "GUEST"
    val referralCode = "KC" + uid.takeLast(8).uppercase()

    val inviteText = """
        Join KCoinRush!
        Use my referral code: $referralCode
        New players get 500 bonus KCoins!
    """.trimIndent()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("KCoinRush Referrals", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(24.dp))

        Card {
            Column(Modifier.padding(20.dp)) {
                Text("Your Referral Code")
                Text(
                    referralCode,
                    style = MaterialTheme.typography.headlineLarge
                )

                Spacer(Modifier.height(12.dp))

                Button(onClick = {
                    clipboard.setText(AnnotatedString(referralCode))
                }) {
                    Text("Copy Code")
                }

                Button(onClick = {
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, inviteText)
                    }
                    context.startActivity(
                        Intent.createChooser(sendIntent, "Share KCoinRush")
                    )
                }) {
                    Text("Share Invitation")
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Text("Inviter reward: 2,000 KCoins")
        Text("New player bonus: 500 KCoins")
        Text("Successful referrals: 0")

        Spacer(Modifier.height(24.dp))

        OutlinedButton(onClick = onBack) {
            Text("Back")
        }

        Spacer(Modifier.height(12.dp))

        Text(
            "Referral rewards are not active until server verification is connected.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
