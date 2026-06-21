package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.GameType
import com.example.ui.viewmodel.MathViewModel
import com.example.ui.viewmodel.toHanifi
import com.example.ui.theme.*

@Composable
fun KidsHomeScreen(
    viewModel: MathViewModel,
    progress: com.example.data.database.UserProgress,
    onNavigateToParent: () -> Unit
) {
    var showAgeDialog by remember { mutableStateOf(false) }

    // Idle floating animation for beautiful home backgrounds (clouds, balloons, trees)
    val infiniteTransition = rememberInfiniteTransition(label = "balloonFloat")
    val balloonShift by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "balloon"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF81D4FA), // Playful morning Sky
                        Color(0xFFE3F2FD),
                        Color(0xFFFFF9C4)  // Golden horizon grass
                    )
                )
            )
    ) {
        // Background elements (clouds, stars, trees, mascots) using emojis for guaranteed high-fidelity rendering
        Box(modifier = Modifier.fillMaxSize()) {
            Text("☁️", fontSize = 48.sp, modifier = Modifier.offset(x = 30.dp, y = 60.dp + balloonShift.dp))
            Text("☁️", fontSize = 36.sp, modifier = Modifier.offset(x = 240.dp, y = (100.dp - balloonShift.dp)))
            Text("🎈", fontSize = 54.sp, modifier = Modifier.offset(x = 310.dp, y = 200.dp + balloonShift.dp))
            Text("🦒", fontSize = 64.sp, modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-10).dp, y = (-20).dp))
            Text("🌳", fontSize = 72.sp, modifier = Modifier.align(Alignment.BottomStart).offset(x = 10.dp, y = 10.dp))
            Text("⭐", fontSize = 24.sp, modifier = Modifier.offset(x = 80.dp, y = 150.dp))
            Text("⭐", fontSize = 32.sp, modifier = Modifier.offset(x = 190.dp, y = 40.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // High-fidelity Child header
            KidsHomeHeader(progress = progress, onParentClick = onNavigateToParent, onAgeClick = { showAgeDialog = true })

            Spacer(modifier = Modifier.height(8.dp))

            // Scrollable Menu Grid with gorgeous custom colors
            val gamesList = GameType.values().toList()
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(gamesList) { game ->
                    GameMenuItem(
                        game = game,
                        onClick = { viewModel.startNewGame(game) }
                    )
                }

                // Add special menu entries for Achievements, Certificate & Verification
                item {
                    SpecialMenuItem(
                        title = "Certificates", // Level Certificates list
                        emoji = "📜",
                        color = KidsOrange,
                        onClick = { viewModel.navigateTo("certificates") }
                    )
                }
                item {
                    SpecialMenuItem(
                        title = "Verify Cert", // Verification page
                        emoji = "🔍",
                        color = KidsPurple,
                        onClick = { viewModel.navigateTo("verification") }
                    )
                }
            }
        }
    }

    // Modal Age Selection Dialog
    if (showAgeDialog) {
        AlertDialog(
            onDismissRequest = { showAgeDialog = false },
            title = {
                Text(
                    "Select Age Group", // Rohingya: Choose Age Level
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = PrimaryLight,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Select your level group below:", modifier = Modifier.padding(bottom = 12.dp), textAlign = TextAlign.Center) // "Choose your class age style:"
                    
                    val ageGroups = listOf(
                        Triple(6, "Age 5-6", "Beginner (𐴵-𐴶 years)"),
                        Triple(8, "Age 7-8", "Explorer (𐴷-𐴸 years)"),
                        Triple(10, "Age 9-10", "Champion (𐴹-𐴱𐴰 years)"),
                        Triple(12, "Age 11-12", "Math Hero (𐴱𐴱-𐴱𐴲 years)")
                    )

                    ageGroups.forEach { (ageVal, roLabel, desc) ->
                        Button(
                            onClick = {
                                viewModel.modifyProfile(progress.childName, ageVal)
                                showAgeDialog = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .testTag("age_select_${ageVal}"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (progress.age == ageVal) KidsBlue else KidsYellow,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(roLabel, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Text(desc, fontSize = 11.sp, color = Color.DarkGray)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAgeDialog = false }) {
                    Text("Close", color = KidsPink) // OK / Back
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }
}

@Composable
fun KidsHomeHeader(
    progress: com.example.data.database.UserProgress,
    onParentClick: () -> Unit,
    onAgeClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .statusBarsPadding(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Parent dashboard key
                IconButton(
                    onClick = onParentClick,
                    modifier = Modifier
                        .size(44.dp)
                        .background(KidsPurple.copy(alpha = 0.15f), CircleShape)
                        .testTag("parent_dashboard_button")
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Parent Area", tint = KidsPurple)
                }

                // Title
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Math Master 🏆", // Rohingya Math title in script
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryLight,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Math for Ro",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light,
                        color = Color.Gray
                    )
                }

                // Level / Age Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(KidsPink.copy(alpha = 0.15f))
                        .clickable { onAgeClick() }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("age_level_selection")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${progress.age.toHanifi()} Yrs", // age in Hanifi
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = KidsPink
                        )
                        Text(
                            "Age ${progress.age}",
                            fontSize = 9.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Welcome Greeting
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "🦁",
                    fontSize = 38.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Column {
                    Text(
                        "Hello, ${progress.childName}!", // Hello student!
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        "Let's play and master math!", // Rohingya supportive text
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.5f))

            // Rewards display row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                RewardPill(emoji = "⭐", value = progress.stars.toHanifi(), label = "Stars", color = KidsYellow)
                RewardPill(emoji = "💰", value = progress.coins.toHanifi(), label = "Taka", color = KidsOrange)
                RewardPill(emoji = "🏆", value = progress.trophies.toHanifi(), label = "Trophies", color = KidsGreen)
            }
        }
    }
}

@Composable
fun RewardPill(
    emoji: String,
    value: String,
    label: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .background(Color.White)
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(emoji, fontSize = 24.sp, modifier = Modifier.padding(end = 4.dp))
        Column {
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, fontSize = 9.sp, color = Color.Gray)
        }
    }
}

@Composable
fun GameMenuItem(
    game: GameType,
    onClick: () -> Unit
) {
    val config = when (game) {
        GameType.LEARN_NUMBERS -> Pair("🔢", KidsBlue)
        GameType.COUNTING -> Pair("🍎", KidsGreen)
        GameType.ADDITION -> Pair("➕", KidsYellow)
        GameType.SUBTRACTION -> Pair("➖", KidsPink)
        GameType.MULTIPLICATION -> Pair("✖️", KidsOrange)
        GameType.DIVISION -> Pair("➗", KidsPurple)
        GameType.FRACTIONS -> Pair("🍰", KidsBlue)
        GameType.SHAPES -> Pair("⭐", KidsGreen)
        GameType.TIME_CLOCK -> Pair("⏰", KidsOrange)
        GameType.MONEY_COUNTING -> Pair("💵", KidsYellow)
        GameType.MEASUREMENT -> Pair("📏", KidsPink)
        GameType.DAILY_CHALLENGE -> Pair("⚡", KidsPurple)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clickable { onClick() }
            .shadow(4.dp, RoundedCornerShape(26.dp))
            .testTag("game_item_${game.name.lowercase()}"),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            columnAndIconLayout(cfg = config, game = game)
        }
    }
}

@Composable
private fun BoxScope.columnAndIconLayout(
    cfg: Pair<String, Color>,
    game: GameType
) {
    // Watermark icon
    Text(
        text = cfg.first,
        fontSize = 54.sp,
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .offset(x = 10.dp, y = 10.dp)
            .shadow(0.dp) // decorative
            .background(Color.Transparent)
            .shadow(0.dp)
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Dot indicator
        Box(
            modifier = Modifier
                .size(14.dp)
                .background(cfg.second, CircleShape)
        )

        Column {
            Text(
                text = game.label,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black
            )
            Text(
                text = game.rawName,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun SpecialMenuItem(
    title: String,
    emoji: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clickable { onClick() }
            .shadow(4.dp, RoundedCornerShape(26.dp)),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 42.sp)
            Text(
                title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}
