package com.kcoinrush.app

import android.os.Bundle
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.view.MotionEvent
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

private val Ink = Color(0xFF0B0714)
private val Panel = Color(0xFF1B1229)
private val Panel2 = Color(0xFF281A3D)
private val Gold = Color(0xFFF4C65E)
private val GoldLight = Color(0xFFFFE6A1)
private val Purple = Color(0xFF9A67E8)
private val Muted = Color(0xFFB9AFC8)

class MainActivity : ComponentActivity() {
    private val dailyTapLimit = 1000
    private val prefs by lazy { getSharedPreferences("kcoinrush", Context.MODE_PRIVATE) }
    private var dailyTaps by mutableIntStateOf(0)
    private var tapDate: String = todayString()
    private var gameScreenActive = false
    private var hasInternet by mutableStateOf(false)
    private lateinit var connectivityManager: ConnectivityManager
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) { refreshInternet() }
        override fun onLost(network: Network) { refreshInternet() }
        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) { refreshInternet() }
    }

    private fun todayString(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun refreshInternet() {
        val network = connectivityManager.activeNetwork
        val caps = connectivityManager.getNetworkCapabilities(network)
        hasInternet = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (gameScreenActive && dailyTaps >= dailyTapLimit) return true
        if (gameScreenActive && event.action == MotionEvent.ACTION_UP) {
            val today = todayString()
            if (today != tapDate) {
                tapDate = today
                dailyTaps = 0
                prefs.edit().putString("tap_date", today).putInt("daily_taps", 0).apply()
            }
            if (dailyTaps < dailyTapLimit) {
                dailyTaps++
                prefs.edit().putInt("daily_taps", dailyTaps).apply()
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onDestroy() {
        if (::connectivityManager.isInitialized) connectivityManager.unregisterNetworkCallback(networkCallback)
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        tapDate = prefs.getString("tap_date", todayString()) ?: todayString()
        dailyTaps = if (tapDate == todayString()) prefs.getInt("daily_taps", 0) else 0
        if (tapDate != todayString()) {
            tapDate = todayString()
            prefs.edit().putString("tap_date", tapDate).putInt("daily_taps", 0).apply()
        }
        refreshInternet()
        connectivityManager.registerNetworkCallback(
            android.net.NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build(),
            networkCallback
        )
        setContent {
            var coins by remember { mutableIntStateOf(prefs.getInt("coins", 0)) }
            var xp by remember { mutableIntStateOf(prefs.getInt("xp", 0)) }
            var level by remember { mutableIntStateOf(prefs.getInt("level", 1)) }
            var screen by remember { mutableStateOf("Home") }
            var game by remember { mutableStateOf("") }
            var missionCount by remember { mutableIntStateOf(prefs.getInt("mission", 0)) }
            var loggedIn by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser != null) }
            DisposableEffect(Unit) {
                val auth = FirebaseAuth.getInstance()
                val listener = FirebaseAuth.AuthStateListener { loggedIn = it.currentUser != null }
                auth.addAuthStateListener(listener)
                onDispose { auth.removeAuthStateListener(listener) }
            }
            SideEffect { gameScreenActive = loggedIn && hasInternet && screen == "Play" && dailyTaps < dailyTapLimit }
            fun award(amount: Int) {
                coins += amount
                xp += 20
                while (xp >= 100 && level < 100) { xp -= 100; level++ }
                missionCount = (missionCount + 1).coerceAtMost(3)
                if (missionCount == 3) { coins += 150; missionCount = 0 }
                prefs.edit().putInt("coins", coins).putInt("xp", xp).putInt("level", level).putInt("mission", missionCount).apply()
            }
            when {
                !hasInternet -> ConnectionRequiredScreen()
                !loggedIn -> FirebaseAuthScreen(onLoginSuccess = { loggedIn = true })
                dailyTaps >= dailyTapLimit && screen == "Play" -> DailyLimitScreen(dailyTapLimit, dailyTaps) { screen = "Home" }
                else -> KCoinRushApp(
                    coins = coins, level = level, xp = xp, missionCount = missionCount,
                    screen = screen, game = game,
                    onNavigate = { screen = it }, onOpenGame = { game = it; screen = "Play" },
                    onBack = { screen = "Home" }, onAward = { award(it) },
                    dailyTaps = dailyTaps, dailyTapLimit = dailyTapLimit
                )
            }
        }
    }
}

@Composable
private fun KCoinRushApp(
    coins: Int, level: Int, xp: Int, missionCount: Int, screen: String, game: String,
    onNavigate: (String) -> Unit, onOpenGame: (String) -> Unit, onBack: () -> Unit, onAward: (Int) -> Unit,
    dailyTaps: Int, dailyTapLimit: Int
) {
    MaterialTheme(colorScheme = darkColorScheme(background = Ink, surface = Panel, primary = Gold, onPrimary = Ink)) {
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF291642), Ink, Ink)))) {
            Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                Header(coins)
                Text("DAILY GAME TAPS: $dailyTaps / $dailyTapLimit", color = GoldLight, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    when (screen) {
                        "Home" -> HomeScreen(coins, level, xp, missionCount, onOpenGame)
                        "Games" -> GamesScreen(onOpenGame)
                        "Friends" -> InfoScreen("👥", "PLAY TOGETHER", "Friends, verified referrals and rewards will be connected after secure accounts are added.")
                        "Profile" -> InfoScreen("👑", "CHAMPION PROFILE", "Level $level  •  ${coins} coins\n\nThis prototype saves progress on this device only.")
                        "Play" -> GameScreen(game, onBack, onAward)
                    }
                }
                if (screen != "Play") BottomNav(screen, onNavigate)
            }
        }
    }
}

@Composable private fun Header(coins: Int) {
    Row(Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text("🪙 KCOIN", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Black)
        Text("RUSH", color = Gold, fontSize = 21.sp, fontWeight = FontWeight.Black)
        Surface(color = Panel2, shape = RoundedCornerShape(14.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF76552F))) {
            Text("🪙 ${coins}", modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp), color = GoldLight, fontWeight = FontWeight.Bold)
        }
    }
}

private data class GameInfo(val name: String, val emoji: String, val detail: String, val key: String)
private val games = listOf(
    GameInfo("COIN RUSH", "🪙", "Tap 20 coins before time runs out", "coin"),
    GameInfo("MEMORY CHALLENGE", "🧠", "Remember and repeat the pattern", "memory"),
    GameInfo("BRAIN CHALLENGE", "🔢", "Solve a quick number puzzle", "brain"),
    GameInfo("GOLD RUNNER", "🏃", "Choose the lane hiding the gold", "runner")
)

@Composable private fun HomeScreen(coins: Int, level: Int, xp: Int, mission: Int, onOpenGame: (String) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 12.dp)) {
        Surface(shape = RoundedCornerShape(24.dp), color = Color.Transparent, border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF63457F))) {
            Box(Modifier.background(Brush.linearGradient(listOf(Color(0xFF382052), Color(0xFF171020)))).padding(18.dp)) {
                Column(Modifier.fillMaxWidth(0.72f)) {
                    Text("BECOME A", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black, lineHeight = 30.sp)
                    Text("CHAMPION!", color = Gold, fontSize = 31.sp, fontWeight = FontWeight.Black, lineHeight = 34.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("PLAY  •  CHALLENGE  •  EARN COINS", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(14.dp))
                    Text("🏆  100 LEVELS · ONE CHAMPION", color = GoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Text("🏆", modifier = Modifier.align(Alignment.CenterEnd), fontSize = 64.sp)
            }
        }
        SectionTitle("CHOOSE YOUR GAME", "4 ARCADE GAMES")
        LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.height(270.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp), userScrollEnabled = false) {
            items(games) { item -> GameCard(item) { onOpenGame(item.key) } }
        }
        SectionTitle("DAILY MISSION", "${mission.coerceAtMost(3)} / 3")
        CardBlock {
            Text("🎯  Complete 3 games today", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text("Reward: 150 bonus coins after 3 completed games", color = Muted, fontSize = 11.sp)
            Spacer(Modifier.height(8.dp)); Progress(mission / 3f)
        }
        SectionTitle("YOUR JOURNEY", "LEVEL $level / 100")
        CardBlock {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Champion progress", color = Color.White, fontSize = 12.sp); Text("$xp / 100 XP", color = Gold, fontSize = 12.sp) }
            Spacer(Modifier.height(8.dp)); Progress(xp / 100f)
        }
    }
}

@Composable private fun GamesScreen(onOpenGame: (String) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        SectionTitle("ARCADE GAMES", "CHOOSE & PLAY")
        games.forEach { GameCard(it) { onOpenGame(it.key) }; Spacer(Modifier.height(10.dp)) }
    }
}

@Composable private fun GameCard(item: GameInfo, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(17.dp), colors = CardDefaults.cardColors(containerColor = Panel), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF443257))) {
        Column(Modifier.padding(13.dp)) {
            Text(item.emoji, fontSize = 30.sp)
            Spacer(Modifier.height(5.dp))
            Text(item.name, color = GoldLight, fontSize = 12.sp, fontWeight = FontWeight.Black)
            Text(item.detail, color = Muted, fontSize = 10.sp, lineHeight = 13.sp)
        }
    }
}

@Composable private fun SectionTitle(title: String, right: String) {
    Row(Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 9.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
        Text(right, color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable private fun CardBlock(content: @Composable ColumnScope.() -> Unit) {
    Surface(color = Panel, shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF59432F))) {
        Column(Modifier.fillMaxWidth().padding(13.dp), content = content)
    }
}

@Composable private fun Progress(value: Float) {
    LinearProgressIndicator(progress = { value.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(7.dp), color = Gold, trackColor = Panel2)
}

@Composable private fun InfoScreen(emoji: String, title: String, message: String) {
    Column(Modifier.fillMaxWidth().padding(top = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        CardBlock {
            Text(emoji, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 48.sp)
            Spacer(Modifier.height(12.dp)); Text(title, modifier = Modifier.fillMaxWidth(), color = Gold, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, fontSize = 20.sp)
            Spacer(Modifier.height(10.dp)); Text(message, modifier = Modifier.fillMaxWidth(), color = Muted, textAlign = TextAlign.Center, fontSize = 13.sp)
        }
    }
}

@Composable private fun BottomNav(current: String, onNavigate: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.SpaceAround) {
        listOf("Home" to "⌂", "Games" to "🎮", "Friends" to "👥", "Profile" to "👤").forEach { (name, icon) ->
            Column(Modifier.clickable { onNavigate(name) }.padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(icon, fontSize = 20.sp); Text(name, color = if (current == name) Gold else Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable private fun GameScreen(game: String, onBack: () -> Unit, onAward: (Int) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Text("← Back", color = Gold, modifier = Modifier.clickable { onBack() }.padding(vertical = 12.dp), fontWeight = FontWeight.Bold)
        when (game) {
            "coin" -> CoinRush(onAward)
            "memory" -> MemoryGame(onAward)
            "brain" -> BrainGame(onAward)
            "runner" -> RunnerGame(onAward)
            else -> Text("Choose a game", color = Color.White)
        }
    }
}

@Composable private fun GamePanel(title: String, instruction: String, content: @Composable ColumnScope.() -> Unit) {
    CardBlock {
        Text(title, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = Gold, fontWeight = FontWeight.Black, fontSize = 20.sp)
        Spacer(Modifier.height(6.dp)); Text(instruction, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = Muted, fontSize = 12.sp)
        Spacer(Modifier.height(18.dp)); content()
    }
}

@Composable private fun GoldButton(label: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Ink), shape = RoundedCornerShape(13.dp)) { Text(label, fontWeight = FontWeight.Black) }
}

@Composable private fun CoinRush(onAward: (Int) -> Unit) {
    var started by remember { mutableStateOf(false) }; var taps by remember { mutableIntStateOf(0) }; var seconds by remember { mutableIntStateOf(15) }; var done by remember { mutableStateOf(false) }
    LaunchedEffect(started, done) { if (started && !done) { while (seconds > 0) { delay(1000); seconds-- }; started = false } }
    GamePanel("🪙 COIN RUSH", "Tap the coin 20 times within 15 seconds.") {
        Text("$taps / 20", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = GoldLight, fontSize = 30.sp, fontWeight = FontWeight.Black)
        Text("Time: $seconds seconds", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = Muted)
        Text("🪙", modifier = Modifier.fillMaxWidth().clickable(enabled = started && !done) { taps++; if (taps >= 20) { done = true; started = false; onAward(50) } }.padding(12.dp), textAlign = TextAlign.Center, fontSize = 76.sp)
        if (done) Text("YOU DID IT! +50 COINS", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = Gold, fontWeight = FontWeight.Bold)
        if (!started) GoldButton(if (done) "PLAY AGAIN" else "START GAME") { taps = 0; seconds = 15; done = false; started = true }
    }
}

@Composable private fun MemoryGame(onAward: (Int) -> Unit) {
    val symbols = listOf("🔴", "🔵", "🟡", "🟢")
    var sequence by remember { mutableStateOf(listOf<Int>()) }; var step by remember { mutableIntStateOf(0) }; var showing by remember { mutableStateOf(false) }; var message by remember { mutableStateOf("Start a round to see the pattern") }
    LaunchedEffect(showing, sequence) { if (showing) { delay(1800); showing = false; message = "Repeat the 3-symbol pattern" } }
    GamePanel("🧠 MEMORY CHALLENGE", message) {
        Text(if (showing) sequence.joinToString("  ") { symbols[it] } else "❔  ❔  ❔", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 30.sp)
        Spacer(Modifier.height(12.dp))
        if (sequence.isEmpty() || (step == 3 && !showing)) GoldButton("START CHALLENGE") { sequence = List(3) { Random.nextInt(4) }; step = 0; showing = true; message = "Memorize the pattern" }
        if (!showing && sequence.isNotEmpty() && step < 3) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) { symbols.forEachIndexed { i, s -> Button(onClick = { if (sequence[step] == i) { step++; if (step == 3) { message = "Perfect memory! +70 coins"; onAward(70) } } else { message = "Wrong pattern. Start again."; sequence = emptyList(); step = 0 } }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Panel2)) { Text(s, fontSize = 20.sp) } } }
        }
        if (step == 3) Text("Completed!", color = Gold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
    }
}

@Composable private fun BrainGame(onAward: (Int) -> Unit) {
    var a by remember { mutableIntStateOf(0) }; var b by remember { mutableIntStateOf(0) }; var solved by remember { mutableStateOf(false) }; var message by remember { mutableStateOf("Press new puzzle to begin") }
    GamePanel("🔢 BRAIN CHALLENGE", message) {
        Text(if (a == 0) "? + ? = ?" else "$a + $b = ?", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = GoldLight, fontSize = 32.sp, fontWeight = FontWeight.Black)
        GoldButton("NEW PUZZLE") { a = Random.nextInt(2, 10); b = Random.nextInt(2, 10); solved = false; message = "Choose the correct answer" }
        if (a != 0 && !solved) {
            val answers = listOf(a + b, a + b + 1, a + b - 1, a + b + 3).shuffled()
            answers.chunked(2).forEach { row -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { row.forEach { value -> Button(onClick = { if (value == a + b) { solved = true; message = "Correct! +60 coins"; onAward(60) } else message = "Not quite. Try again." }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Panel2)) { Text(value.toString(), color = Color.White) } } } }
        }
    }
}

@Composable private fun RunnerGame(onAward: (Int) -> Unit) {
    var goldLane by remember { mutableIntStateOf(-1) }; var message by remember { mutableStateOf("Start the run and find the gold lane") }; var finished by remember { mutableStateOf(false) }
    GamePanel("🏃 GOLD RUNNER", message) {
        Text("⬅️                         ➡️", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 28.sp)
        GoldButton("START RUN") { goldLane = Random.nextInt(2); finished = false; message = "Choose left or right" }
        if (goldLane >= 0 && !finished) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { finished = true; if (goldLane == 0) { message = "Gold found! +80 coins"; onAward(80) } else message = "No gold this time. Try again." }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Panel2)) { Text("LEFT") }
            Button(onClick = { finished = true; if (goldLane == 1) { message = "Gold found! +80 coins"; onAward(80) } else message = "No gold this time. Try again." }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Panel2)) { Text("RIGHT") }
        }
    }
}


@Composable
private fun ConnectionRequiredScreen() {
    MaterialTheme(colorScheme = darkColorScheme(background = Ink, surface = Panel, primary = Gold)) {
        Box(Modifier.fillMaxSize().background(Ink).padding(24.dp), contentAlignment = Alignment.Center) {
            CardBlock {
                Text("🌐", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 48.sp)
                Text("INTERNET REQUIRED", modifier = Modifier.fillMaxWidth(), color = Gold, textAlign = TextAlign.Center, fontWeight = FontWeight.Black, fontSize = 20.sp)
                Text("Please connect to the internet to log in and play KCoinRush.", modifier = Modifier.fillMaxWidth(), color = Muted, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun DailyLimitScreen(limit: Int, used: Int, onHome: () -> Unit) {
    MaterialTheme(colorScheme = darkColorScheme(background = Ink, surface = Panel, primary = Gold)) {
        Box(Modifier.fillMaxSize().background(Ink).padding(24.dp), contentAlignment = Alignment.Center) {
            CardBlock {
                Text("⏰", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 48.sp)
                Text("DAILY TAP LIMIT REACHED", modifier = Modifier.fillMaxWidth(), color = Gold, textAlign = TextAlign.Center, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text("You used $used of $limit game taps today. Come back tomorrow!", modifier = Modifier.fillMaxWidth(), color = Muted, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                GoldButton("BACK TO HOME", onHome)
            }
        }
    }
}
