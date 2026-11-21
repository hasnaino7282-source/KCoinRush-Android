package com.kcoinrush.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ReferralBackground = Color(0xFF0B0714)
private val ReferralPanel = Color(0xFF1B1229)
private val ReferralGold = Color(0xFFF4C65E)
private val ReferralMuted = Color(0xFFB9AFC8)

/**
 * Referral UI only. Connect secure referral validation and coin rewards on a trusted
 * backend before granting any rewards. Do not award coins from this screen alone.
 */
@Composable
fun ReferralScreen(
    referralCode: String,
    invitedFriends: Int,
    onCopyCode: () -> Unit,
    onShareInvite: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReferralBackground)
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            Text(
                text = "← Back",
                color = ReferralGold,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        // Back action is presented as a button so it is accessible and clickable.
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = ReferralPanel),
            modifier = Modifier.fillMaxWidth()
        ) { Text("BACK TO HOME", color = ReferralGold) }

        Spacer(Modifier.height(20.dp))
        Text("👥", fontSize = 48.sp)
        Text(
            "INVITE FRIENDS",
            color = ReferralGold,
            fontSize = 25.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Share KCoinRush with friends and play together!",
            color = ReferralMuted,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = ReferralPanel)
        ) {
            Column(Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("YOUR REFERRAL CODE", color = ReferralMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(referralCode, color = ReferralGold, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onCopyCode,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ReferralGold)
                ) { Text("COPY REFERRAL CODE", color = ReferralBackground, fontWeight = FontWeight.Black) }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onShareInvite,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7650B5))
                ) { Text("SHARE INVITE", color = Color.White, fontWeight = FontWeight.Bold) }
            }
        }

        Spacer(Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = ReferralPanel)
        ) {
            Column(Modifier.fillMaxWidth().padding(18.dp)) {
                Text("🎁 REFERRAL REWARDS", color = ReferralGold, fontWeight = FontWeight.Black, fontSize = 16.sp)
                Spacer(Modifier.height(10.dp))
                Text("You invite a friend: 2,000 KCoins", color = Color.White, fontWeight = FontWeight.Bold)
                Text("New player welcome reward: 500 KCoins", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(14.dp))
                Text("INVITE PROGRESS", color = ReferralMuted, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(Modifier.height(6.dp))
                Text("$invitedFriends / 5 friends", color = ReferralGold, fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text(
                    "Rewards are a preview only. Referral verification and coin payments are not active until a secure server is connected.",
                    color = ReferralMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}
