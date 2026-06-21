package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ConfettiEffect
import com.example.ui.theme.*
import com.example.ui.viewmodel.MathViewModel
import com.example.ui.viewmodel.toHanifi
import kotlin.random.Random

@Composable
fun GameOverScreen(
    viewModel: MathViewModel,
    onBackToHome: () -> Unit
) {
    val solved by viewModel.problemsSolved.collectAsState()
    val total by viewModel.problemsTotal.collectAsState()
    val coinsEarned by viewModel.gameCoinsBonus.collectAsState()
    val starsEarned by viewModel.gameStarsBonus.collectAsState()

    // Chest states
    var chest1Opened by remember { mutableStateOf(false) }
    var chest2Opened by remember { mutableStateOf(false) }
    var chest3Opened by remember { mutableStateOf(false) }

    var reward1Text by remember { mutableStateOf("🎁") }
    var reward2Text by remember { mutableStateOf("🎁") }
    var reward3Text by remember { mutableStateOf("🎁") }

    val percentage = if (total > 0) (solved * 100) / total else 0

    // Congratulation script text based on percentage
    val congratsRo = when {
        percentage >= 90 -> "𐴔𐴝𐴃𐴜-𐴇𐴠𐴌𐴡! 𐴋𐴠𐴌 𐴁𐴠𐴃𐴠𐴓!" // Math Hero! Awesome!
        percentage >= 70 -> "𐴋𐴠𐴌 𐴎𐴝𐴔𐴠𐴓! (Great Job!)"
        else -> "𐴃𐴞𐴔𐴞 𐴉𐴝𐴌𐴞𐴁𐴠! (You Can Do It!)"
    }

    val bannerColor = when {
        percentage >= 90 -> KidsYellow
        percentage >= 70 -> KidsBlue
        else -> KidsPink
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFEE58).copy(alpha = 0.2f), // Glowing champion hue
                        Color(0xFFE3F2FD),
                        Color(0xFFE8F5E9)
                    )
                )
            )
    ) {
        // Falling confetti!
        if (percentage >= 70) {
            ConfettiEffect()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.statusBarsPadding()
            ) {
                Text(
                    text = if (percentage >= 70) "🏆" else "🌟",
                    fontSize = 72.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = congratsRo,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Accuracy: ${percentage.toHanifi()}%", // accuracy percent in Hanifi
                    fontSize = 18.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Interactive Treasure Chest Hunt Cabinet
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .shadow(4.dp, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "𐴇𐴞𐴔𐴝𐴉𐴞 𐴃𐴝𐴔𐴝𐴔: 𐴑𐴡𐴃𐴠 𐴓𐴠𐴔𐴝𐴃𐴜!", // Choose a magic gift!
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = KidsOrange,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        // Chest 1
                        TreasureChestWidget(
                            isOpen = chest1Opened,
                            displaySymbol = reward1Text,
                            onClick = {
                                if (!chest1Opened) {
                                    chest1Opened = true
                                    reward1Text = listOf("🪙 +𐴱𐴰", "💎 Star Pro", "🏆 Medal").random()
                                }
                            },
                            testLabel = "chest_1"
                        )

                        // Chest 2
                        TreasureChestWidget(
                            isOpen = chest2Opened,
                            displaySymbol = reward2Text,
                            onClick = {
                                if (!chest2Opened) {
                                    chest2Opened = true
                                    reward2Text = listOf("🪙 +𐴲𐴰", "✨ Sparkle", "🎗 Badge").random()
                                }
                            },
                            testLabel = "chest_2"
                        )

                        // Chest 3
                        TreasureChestWidget(
                            isOpen = chest3Opened,
                            displaySymbol = reward3Text,
                            onClick = {
                                if (!chest3Opened) {
                                    chest3Opened = true
                                    reward3Text = listOf("🪙 +𐴵", "🏅 Silver", "👑 Hero Crown").random()
                                }
                            },
                            testLabel = "chest_3"
                        )
                    }
                }
            }

            // Primary Stat summary card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    GameOverStatPill(symbol = "🪙", amount = coinsEarned.toHanifi(), label = "Coins Gained", color = KidsOrange)
                    GameOverStatPill(symbol = "⭐", amount = starsEarned.toHanifi(), label = "Stars Gained", color = KidsYellow)
                }
            }

            // Return to dashboard button
            Button(
                onClick = onBackToHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .shadow(3.dp, RoundedCornerShape(18.dp))
                    .testTag("game_over_home_button"),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KidsBlue)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Home, contentDescription = "Home")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("𐴄𐴠𐴓𐴝𐴌 𐴎𐴝𐴔𐴝𐴃𐴜", fontSize = 20.sp, fontWeight = FontWeight.Bold) // Play again/Home
                }
            }
        }
    }
}

@Composable
fun TreasureChestWidget(
    isOpen: Boolean,
    displaySymbol: String,
    onClick: () -> Unit,
    testLabel: String
) {
    val scaleFactor = remember { Animatable(1f) }

    LaunchedEffect(isOpen) {
        if (isOpen) {
            scaleFactor.animateTo(1.3f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            scaleFactor.animateTo(1.0f, animationSpec = tween(150))
        }
    }

    Box(
        modifier = Modifier
            .size(80.dp)
            .shadow(if (isOpen) 0.dp else 2.dp, RoundedCornerShape(16.dp))
            .background(if (isOpen) KidsGreen.copy(alpha = 0.15f) else Color.White, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .scale(scaleFactor.value)
            .testTag(testLabel),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displaySymbol,
            fontSize = if (isOpen) 20.sp else 42.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

@Composable
fun GameOverStatPill(
    symbol: String,
    amount: String,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(symbol, fontSize = 38.sp)
        Text(amount, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = color)
        Text(label, fontSize = 11.sp, color = Color.Gray)
    }
}
