package com.ruben.d4helltide

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId

private const val HELLTIDES_URL = "https://helltides.com/"
private const val SCHEDULE_URL = "https://helltides.com/schedule"
private const val WORLD_BOSS_URL = "https://helltides.com/worldboss"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { D4HelltideApp() }
    }
}

enum class AppTab(val label: String, val letter: String) {
    DASHBOARD("Helltide", "H"),
    LIVE_MAP("Live Map", "M"),
    GUIDE("Guide", "G")
}

@Composable
fun D4HelltideApp() {
    var selectedTab by rememberSaveable { mutableStateOf(AppTab.DASHBOARD) }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFC94444),
            secondary = Color(0xFFD7A45C),
            background = Color(0xFF090909),
            surface = Color(0xFF111111)
        )
    ) {
        Scaffold(
            containerColor = Color(0xFF090909),
            bottomBar = {
                NavigationBar(containerColor = Color(0xFF111111)) {
                    AppTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = {
                                Surface(
                                    modifier = Modifier.size(28.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (selectedTab == tab) Color(0xFF7B2222) else Color(0xFF252525)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(tab.letter, fontWeight = FontWeight.Bold)
                                    }
                                }
                            },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                when (selectedTab) {
                    AppTab.DASHBOARD -> DashboardScreen(onOpenMap = { selectedTab = AppTab.LIVE_MAP })
                    AppTab.LIVE_MAP -> LiveMapScreen()
                    AppTab.GUIDE -> GuideScreen()
                }
            }
        }
    }
}

data class HelltideClock(
    val active: Boolean,
    val remainingSeconds: Int,
    val progress: Float
)

private fun getHelltideClock(now: Instant = Instant.now()): HelltideClock {
    val local = now.atZone(ZoneId.systemDefault())
    val secondsIntoHour = local.minute * 60 + local.second
    val activeSeconds = 55 * 60
    val active = secondsIntoHour < activeSeconds
    val remaining = if (active) activeSeconds - secondsIntoHour else 60 * 60 - secondsIntoHour
    val progress = if (active) secondsIntoHour / activeSeconds.toFloat() else (secondsIntoHour - activeSeconds) / 300f
    return HelltideClock(active, remaining, progress.coerceIn(0f, 1f))
}

@Composable
fun DashboardScreen(onOpenMap: () -> Unit) {
    var clock by remember { mutableStateOf(getHelltideClock()) }
    var cinders by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            clock = getHelltideClock()
            delay(1000)
        }
    }

    val minutes = clock.remainingSeconds / 60
    val seconds = clock.remainingSeconds % 60
    val timeText = "%02d:%02d".format(minutes, seconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("D4 Helltide Companion", fontSize = 27.sp, fontWeight = FontWeight.Black)
        Text("Fast Helltide information without digging through browser tabs.", color = Color(0xFFB7B7B7))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF211313)),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                Modifier.fillMaxWidth().padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    if (clock.active) "HELLTIDE ACTIVE" else "HELLTIDE BREAK",
                    color = if (clock.active) Color(0xFFFF7777) else Color(0xFFD7A45C),
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(timeText, fontSize = 48.sp, fontWeight = FontWeight.Black)
                Text(
                    if (clock.active) "remaining" else "until the next Helltide",
                    color = Color(0xFFB7B7B7)
                )
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = onOpenMap,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8A2828))
                ) {
                    Text("OPEN LIVE HELLTIDE MAP", fontWeight = FontWeight.Bold)
                }
            }
        }

        SectionCard("Cinder Tracker") {
            Text("Track your current Aberrant Cinders while you farm.", color = Color(0xFFB7B7B7))
            Spacer(Modifier.height(8.dp))
            Text(cinders.toString(), fontSize = 38.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CinderButton("+25", Modifier.weight(1f)) { cinders += 25 }
                CinderButton("+50", Modifier.weight(1f)) { cinders += 50 }
                CinderButton("+100", Modifier.weight(1f)) { cinders += 100 }
                CinderButton("Reset", Modifier.weight(1f)) { cinders = 0 }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                if (cinders >= 250) "You have enough for a 250-Cinder Mystery Chest." else "${250 - cinders} more Cinders to reach 250.",
                fontWeight = FontWeight.SemiBold,
                color = if (cinders >= 250) Color(0xFFD7A45C) else Color(0xFFCCCCCC)
            )
        }

        SectionCard("Helltide Priorities") {
            PriorityRow("1", "Accursed Ritual / Blood Maiden", "Strong repeatable farming and loot.")
            Divider(color = Color(0xFF303030))
            PriorityRow("2", "Mystery Chests", "Use the live map to locate active chest positions.")
            Divider(color = Color(0xFF303030))
            PriorityRow("3", "Local Events", "Efficient Cinder farming between objectives.")
        }

        Text(
            "Timer logic is native and works offline. Live location data is displayed from Helltides.com in the Live Map tab.",
            color = Color(0xFF777777),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun CinderButton(text: String, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier,
        contentPadding = ButtonDefaults.ContentPadding,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2A2A))
    ) {
        Text(text, fontSize = 12.sp, maxLines = 1)
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151515)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(title, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun PriorityRow(number: String, title: String, detail: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(shape = RoundedCornerShape(30.dp), color = Color(0xFF6F2424), modifier = Modifier.size(34.dp)) {
            Box(contentAlignment = Alignment.Center) { Text(number, fontWeight = FontWeight.Bold) }
        }
        Spacer(Modifier.size(10.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(detail, color = Color(0xFFAFAFAF), fontSize = 13.sp)
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LiveMapScreen() {
    val context = LocalContext.current
    var webView by remember { mutableStateOf<WebView?>(null) }
    var loading by remember { mutableStateOf(true) }

    BackHandler(enabled = webView?.canGoBack() == true) {
        webView?.goBack()
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF111111)).padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Live Helltide Map", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Powered by Helltides.com", color = Color(0xFF9E9E9E), fontSize = 12.sp)
            }
            Text(
                "Refresh",
                modifier = Modifier.clickable { webView?.reload() }.padding(10.dp),
                color = Color(0xFFFF8181),
                fontWeight = FontWeight.Bold
            )
        }

        Box(Modifier.fillMaxSize()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.loadsImagesAutomatically = true
                        settings.useWideViewPort = true
                        settings.loadWithOverviewMode = true
                        settings.setSupportZoom(true)
                        settings.builtInZoomControls = true
                        settings.displayZoomControls = false
                        webChromeClient = WebChromeClient()
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                loading = false
                            }
                        }
                        loadUrl(HELLTIDES_URL)
                        webView = this
                    }
                },
                update = { webView = it }
            )
            if (loading) {
                Surface(
                    modifier = Modifier.align(Alignment.TopCenter).padding(10.dp),
                    color = Color(0xDD1D1D1D),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Loading live map...", Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
fun GuideScreen() {
    val context = LocalContext.current

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Helltide Quick Guide", fontSize = 27.sp, fontWeight = FontWeight.Black)

        SectionCard("What the map shows") {
            Text("Mystery Chest locations, Accursed Ritual/Blood Maiden locations, and local events are available through the live Helltides.com map.")
        }

        SectionCard("Current timing") {
            Text("Helltides begin at the top of the hour, remain active for 55 minutes, and are inactive for the final 5 minutes.")
        }

        SectionCard("Mystery Chests") {
            Text("Mystery Chests cost 250 Aberrant Cinders. Helltides.com currently documents two active Mystery Chests in most regions and three in Kehjistan.")
        }

        SectionCard("Blood Maiden") {
            Text("Accursed Rituals require three Baneful Hearts to summon the Blood Maiden. Contributing a heart can improve your rewards from the encounter.")
        }

        SectionCard("Other useful trackers") {
            LinkButton("Open Event Schedule") { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(SCHEDULE_URL))) }
            Spacer(Modifier.height(8.dp))
            LinkButton("Open World Boss Tracker") { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(WORLD_BOSS_URL))) }
        }

        SectionCard("About this app") {
            Text(
                "D4 Helltide Companion is an unofficial fan companion. Diablo IV and related names and assets belong to Blizzard Entertainment. This app does not copy Helltides.com map assets; the Live Map tab loads their public website directly.",
                color = Color(0xFFB7B7B7)
            )
        }
    }
}

@Composable
private fun LinkButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF292929))
    ) {
        Text(text, textAlign = TextAlign.Center)
    }
}
