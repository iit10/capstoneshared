@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package ca.gbc.comp3074.snapcal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SnapCalApp() }
    }
}

/* --------------------------- Navigation --------------------------- */

sealed class Screen(val route: String, val label: String, val icon: @Composable (() -> Unit)?) {
    data object Login      : Screen("login",      "Login",      null)
    data object Dashboard  : Screen("dashboard",  "Home",       { Icon(Icons.Default.Home, null) })
    data object Scan       : Screen("scan",       "Scan",       { Icon(Icons.Default.CameraAlt, null) })
    data object Manual     : Screen("manual",     "Log",        { Icon(Icons.Default.Edit, null) })
    data object Progress   : Screen("progress",   "Progress",   { Icon(Icons.Default.Timeline, null) })
    data object Garmin     : Screen("garmin",     "Settings",   { Icon(Icons.Default.Settings, null) })
}

@Composable
fun SnapCalApp() {
    val nav = rememberNavController()
    MaterialTheme(colorScheme = lightColorScheme()) {
        Scaffold(
            bottomBar = {
                val current = nav.currentBackStackEntryAsState().value?.destination?.route
                if (current != Screen.Login.route) SnapCalBottomBar(nav)
            }
        ) { inner ->
            NavHost(
                navController = nav,
                startDestination = Screen.Login.route,
                modifier = Modifier.padding(inner)
            ) {
                composable(Screen.Login.route)     { LoginScreen(onLogin = { nav.navigate(Screen.Dashboard.route) }) }
                composable(Screen.Dashboard.route) { DashboardScreen(
                    onScan = { nav.navigate(Screen.Scan.route) },
                    onManual = { nav.navigate(Screen.Manual.route) },
                    onProgress = { nav.navigate(Screen.Progress.route) }
                ) }
                composable(Screen.Scan.route)      { ScanScreen(onSaved = { nav.navigateUp() }) }
                composable(Screen.Manual.route)    { ManualMealScreen(onSaved = { nav.navigateUp() }) }
                composable(Screen.Progress.route)  { ProgressScreen() }
                composable(Screen.Garmin.route)    { GarminScreen() }
            }
        }
    }
}

@Composable
fun SnapCalBottomBar(nav: NavHostController) {
    val items = listOf(Screen.Dashboard, Screen.Manual, Screen.Progress, Screen.Garmin)
    val currentRoute = nav.currentBackStackEntryAsState().value?.destination?.route
    NavigationBar {
        items.forEach { screen ->
            NavigationBarItem(
                selected = currentRoute == screen.route,
                onClick = {
                    nav.navigate(screen.route) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(Screen.Dashboard.route) { saveState = true }
                    }
                },
                icon = { screen.icon?.invoke() },
                label = { Text(screen.label) }
            )
        }
    }
}

/* --------------------------- Reusable UI --------------------------- */

@Composable
fun AppTopBar(
    title: String,
    nav: NavHostController? = null,
    showBack: Boolean = false
) {
    TopAppBar(
        title = { Text(text = title, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            if (showBack && nav != null) {
                IconButton(onClick = { nav.navigateUp() }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                }
            }
        }
    )
}

@Composable
fun CardBlock(
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        if (title != null) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
        }
        content()
    }
}

/* --------------------------- Screens --------------------------- */

/** 1) Login / Registration */
@Composable
fun LoginScreen(onLogin: () -> Unit) {
    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.height(16.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("SnapCal", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text("Log in to continue", color = Color.Gray)
                Spacer(Modifier.height(24.dp))
                OutlinedTextField(value = "", onValueChange = {}, label = { Text("Email") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = "", onValueChange = {}, label = { Text("Password") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))
                Button(onClick = onLogin, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(14.dp)) {
                    Text("Login")
                }
                TextButton(onClick = { /* TODO: Sign up flow placeholder */ }) { Text("Sign Up") }
                TextButton(onClick = { /* TODO: Forgot password placeholder */ }) { Text("Forgot password?") }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

/** 2) Home / Dashboard */
@Composable
fun DashboardScreen(onScan: () -> Unit, onManual: () -> Unit, onProgress: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { AppTopBar("Dashboard") }
        item {
            CardBlock(title = "Calories (in vs out)") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Canvas(modifier = Modifier.size(96.dp)) {
                        val stroke = 14.dp.toPx()
                        drawArc(
                            color = Color(0xFFE5E5E5),
                            startAngle = 0f, sweepAngle = 360f,
                            useCenter = false, style = Stroke(width = stroke)
                        )
                        drawArc(
                            color = Color(0xFF4A90E2),
                            startAngle = -90f, sweepAngle = 234f, // ≈ 65%
                            useCenter = false, style = Stroke(width = stroke)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("In: 1,650 kcal   Out: 700 kcal")
                        Text("Balance: –950 kcal", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(12.dp).background(Color(0xFF4A90E2), RoundedCornerShape(2.dp)))
                            Spacer(Modifier.width(6.dp))
                            Text("Consumed", color = Color.Gray)
                            Spacer(Modifier.width(16.dp))
                            Box(Modifier.size(12.dp).background(Color(0xFFE5E5E5), RoundedCornerShape(2.dp)))
                            Spacer(Modifier.width(6.dp))
                            Text("Remaining", color = Color.Gray)
                        }
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                CardBlock(modifier = Modifier.weight(1f), title = "Steps") {
                    Text("6,842", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Goal: 8,000", color = Color.Gray)
                }
                CardBlock(modifier = Modifier.weight(1f), title = "Workouts") {
                    Text("Cycling 45 min", fontWeight = FontWeight.SemiBold)
                    Text("Burned: 520 kcal", color = Color.Gray)
                }
            }
        }
        item {
            CardBlock(title = "Quick Actions") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onScan, modifier = Modifier.weight(1f)) { Text("Scan Label") }
                    OutlinedButton(onManual, modifier = Modifier.weight(1f)) { Text("Log Meal") }
                    OutlinedButton(onProgress, modifier = Modifier.weight(1f)) { Text("Progress") }
                }
            }
        }
    }
}

/** 3) OCR Scan Meal */
@Composable
fun ScanScreen(onSaved: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        AppTopBar("Scan Product / Receipt")
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0F172A)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(0.9f)
                        .fillMaxHeight(0.75f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1F2937)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Align label inside the frame\nGood lighting improves accuracy", color = Color(0xFFCBD5E1))
                }
            }
            Button(onClick = { /* simulate capture */ }) {
                Icon(Icons.Default.CameraAlt, null); Spacer(Modifier.width(8.dp)); Text("Capture")
            }

            CardBlock(title = "Detected Item") {
                Text("Greek Yogurt 2% (170 g)", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Nutriple("Calories", "130 kcal")
                    Nutriple("Protein", "12 g")
                    Nutriple("Carbs", "5 g")
                    Nutriple("Fat", "4 g")
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { /* edit data */ }) {
                        Icon(Icons.Default.Edit, null); Spacer(Modifier.width(6.dp)); Text("Edit")
                    }
                    Button(onClick = onSaved) {
                        Icon(Icons.Default.Check, null); Spacer(Modifier.width(6.dp)); Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun Nutriple(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.Gray, style = MaterialTheme.typography.labelMedium)
        Text(value, fontWeight = FontWeight.Bold)
    }
}

/** 4) Manual Meal Entry */
@Composable
fun ManualMealScreen(onSaved: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        AppTopBar("Add Meal Manually")
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField("", {}, label = { Text("Food name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField("", {}, label = { Text("Portion size (e.g., 100 g)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField("", {}, label = { Text("Calories (kcal)") }, modifier = Modifier.fillMaxWidth())
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField("", {}, label = { Text("Protein (g)") }, modifier = Modifier.weight(1f))
                OutlinedTextField("", {}, label = { Text("Carbs (g)") }, modifier = Modifier.weight(1f))
                OutlinedTextField("", {}, label = { Text("Fat (g)") }, modifier = Modifier.weight(1f))
            }
            OutlinedTextField("Breakfast", {}, label = { Text("Meal type") }, modifier = Modifier.fillMaxWidth(), readOnly = true)
            Button(onClick = onSaved, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(14.dp)) {
                Text("Save Meal")
            }
        }
    }
}

/** 5) Progress Tracking */
@Composable
fun ProgressScreen() {
    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { AppTopBar("Progress") }
        item {
            CardBlock(title = "Weight Trend (last 4 weeks)") {
                Canvas(Modifier.fillMaxWidth().height(120.dp)) {
                    val w = size.width
                    val h = size.height
                    drawLine(Color(0xFFE0E0E0), start = androidx.compose.ui.geometry.Offset(0f, h-2), end = androidx.compose.ui.geometry.Offset(w, h-2))
                    val points = listOf(0.05f to 0.75f, 0.2f to 0.7f, 0.35f to 0.72f, 0.5f to 0.68f, 0.7f to 0.7f, 0.9f to 0.64f)
                    for (i in 0 until points.lastIndex) {
                        val (x1, y1) = points[i]; val (x2, y2) = points[i+1]
                        drawLine(Color(0xFF3B82F6),
                            start = androidx.compose.ui.geometry.Offset(w*x1, h*y1),
                            end = androidx.compose.ui.geometry.Offset(w*x2, h*y2),
                            strokeWidth = 6f)
                    }
                }
            }
        }
        item {
            CardBlock(title = "Calories In vs Out (weekly)") {
                val days = listOf("M","T","W","T","F","S","S")
                val inVals = listOf(1800,2000,1700,1900,1600,1750,1850)
                val outVals= listOf(1500,1600,1400,1550,1300,1450,1500)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    days.indices.forEach { i ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(Modifier.width(10.dp).height((inVals[i]/20).dp).background(Color(0xFF60A5FA)))
                            Spacer(Modifier.height(4.dp))
                            Box(Modifier.width(10.dp).height((outVals[i]/20).dp).background(Color(0xFFF87171)))
                            Spacer(Modifier.height(6.dp))
                            Text(days[i], color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
        item {
            CardBlock(title = "Macronutrient Breakdown") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Canvas(Modifier.size(80.dp)) {
                        val sizePx = Size(size.width, size.height)
                        drawArc(Color(0xFFF87171), startAngle = 0f,   sweepAngle = 50f,  useCenter = true, size = sizePx)
                        drawArc(Color(0xFF34D399), startAngle = 50f,  sweepAngle = 150f, useCenter = true, size = sizePx)
                        drawArc(Color(0xFF60A5FA), startAngle = 200f, sweepAngle = 160f, useCenter = true, size = sizePx)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Protein 30%")
                        Text("Carbs 45%")
                        Text("Fat 25%")
                    }
                }
            }
        }
    }
}

/** 6) Garmin Integration */
@Composable
fun GarminScreen() {
    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { AppTopBar("Garmin Integration") }
        item {
            CardBlock {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Connect to Garmin", fontWeight = FontWeight.Bold)
                        Text("Sync workouts, steps, heart rate", color = Color.Gray)
                    }
                    var enabled by remember { mutableStateOf(true) }
                    Switch(checked = enabled, onCheckedChange = { enabled = it })
                }
            }
        }
        item {
            CardBlock(title = "Recent Workouts") {
                val items = listOf(
                    "Sep 26 • Cycling" to "45 min • 520 kcal",
                    "Sep 25 • Running" to "30 min • 340 kcal",
                    "Sep 23 • Strength" to "40 min • 280 kcal"
                )
                items.forEach { (title, meta) ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(meta, color = Color.Gray)
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
        item {
            CardBlock(title = "Heart Rate Summary") {
                Text("Resting HR: 58 bpm")
                Text("Average HR (workouts): 138 bpm")
                Text("Max HR (last workout): 172 bpm")
            }
        }
        item {
            CardBlock(title = "Calories Burned (Weekly)") {
                val days = listOf("M","T","W","T","F","S","S")
                val vals = listOf(520,340,280,600,450,700,300)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    days.indices.forEach { i ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(Modifier.width(16.dp).height((vals[i]/10).dp).background(Color(0xFF3B82F6)))
                            Spacer(Modifier.height(6.dp))
                            Text(days[i], color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
