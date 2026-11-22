package com.kcoinrush.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ReferralScreen(
    referralCode: String,
    invitedFriends: Int,
    onCopyCode: () -> Unit,
    onShareInvite: () -> Unit,
    onBack: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "KCoinRush Referrals",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(24.dp))

        Card {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Your Referral Code")

                Text(
                    referralCode,
                    style = MaterialTheme.typography.headlineLarge
                )

                Spacer(Modifier.height(12.dp))

                Button(onClick = onCopyCode) {
                    Text("Copy Code")
                }

                Button(onClick = onShareInvite) {
                    Text("Share Invitation")
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Text("Inviter reward: 2,000 KCoins")
        Text("New player bonus: 500 KCoins")
        Text("Successful referrals: $invitedFriends")

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
