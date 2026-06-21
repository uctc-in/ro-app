package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ConfettiEffect
import com.example.ui.theme.*
import com.example.ui.viewmodel.GameType
import com.example.ui.viewmodel.MathProblem
import com.example.ui.viewmodel.MathViewModel
import com.example.ui.viewmodel.toHanifi
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GamePlayScreen(
    viewModel: MathViewModel,
    onBackToHome: () -> Unit
) {
    val currentProblem by viewModel.currentProblem.collectAsState()
    val answerStatus by viewModel.answerStatus.collectAsState()
    val selectedIndex by viewModel.selectedAnswerIndex.collectAsState()
    val gameType by viewModel.activeGameType.collectAsState()

    val problemsSolved by viewModel.problemsSolved.collectAsState()
    val problemsTotal by viewModel.problemsTotal.collectAsState()
    val coinsEarned by viewModel.gameCoinsBonus.collectAsState()
    val starsEarned by viewModel.gameStarsBonus.collectAsState()

    val rabbitProgress by viewModel.rabbitProgress.collectAsState()
    val elephantProgress by viewModel.elephantProgress.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE8F5E9), // Light mint sky
                        Color(0xFFFFF3E0), // Warm beige
                        Color(0xFFFFFDE7)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Gameplay Top Bar with Back option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBackToHome,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White, CircleShape)
                        .testTag("exit_game_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Exit Game", tint = KidsPink)
                }

                // Progress Bar in Hanifi
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        "${problemsSolved.toHanifi()} / ${problemsTotal.toHanifi()}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = KidsBlue
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("⭐", fontSize = 16.sp)
                }

                // Sound Toggle Icon
                IconButton(
                    onClick = { viewModel.playSoundEffects = !viewModel.playSoundEffects },
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White, CircleShape)
                ) {
                    Text(if (viewModel.playSoundEffects) "🔊" else "🔇", fontSize = 20.sp)
                }
            }

            // Animal Race Widget
            AnimalRaceTrack(rabbitPos = rabbitProgress, elephantPos = elephantProgress)

            Spacer(modifier = Modifier.height(10.dp))

            // Central Game Card containing problem and visual items
            currentProblem?.let { problem ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .shadow(6.dp, RoundedCornerShape(32.dp)),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Math Instructions (Hanifi Rohingya)
                        Text(
                            text = problem.titleRo,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryDark,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Core Visual Canvas / Grid depending on question visual type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            VisualMathDisplay(problem = problem)
                        }

                        // Feedback Banner (Correct / Wrong)
                        AnimatedVisibility(
                            visible = answerStatus != null,
                            enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                            exit = fadeOut()
                        ) {
                            FeedbackSection(isCorrect = answerStatus == true, correctExplanation = problem.explanationRo)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Grid Options
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth().height(80.dp)
                        ) {
                            itemsIndexed(problem.options) { idx, option ->
                                val isSelected = selectedIndex == idx
                                val isCorrect = problem.correctIndex == idx
                                val btnColor = when {
                                    answerStatus != null && isCorrect -> KidsGreen
                                    answerStatus != null && isSelected && !isCorrect -> KidsPink
                                    else -> KidsBlue
                                }

                                Button(
                                    onClick = { viewModel.selectAnswer(idx) },
                                    enabled = answerStatus == null,
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = btnColor,
                                        disabledContainerColor = btnColor,
                                        contentColor = Color.White,
                                        disabledContentColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(72.dp)
                                        .shadow(3.dp, RoundedCornerShape(16.dp))
                                        .testTag("option_$idx")
                                ) {
                                    Text(
                                        text = option,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }

                        // Action button (Skip or Next)
                        if (answerStatus != null) {
                            Button(
                                onClick = { viewModel.skipOrNext() },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .padding(top = 10.dp)
                                    .fillMaxWidth()
                                    .testTag("next_problem_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = KidsOrange)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Next Problem", fontSize = 18.sp, fontWeight = FontWeight.Bold) // Next problem
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.Default.ArrowForward, contentDescription = "Next")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VisualMathDisplay(problem: MathProblem) {
    val animatedCount = remember { Animatable(0f) }
    LaunchedEffect(problem) {
        animatedCount.snapTo(0f)
        animatedCount.animateTo(
            targetValue = 1f,
            animationSpec = tween(700, easing = EaseOutBack)
        )
    }

    when (problem.visualType) {
        "apples" -> {
            Row(
                modifier = Modifier.fillMaxWidth().wrapContentHeight().scale(animatedCount.value),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(problem.visualCount1) {
                    Text("🍎", fontSize = 38.sp, modifier = Modifier.padding(2.dp))
                }
            }
        }
        "balloons" -> {
            Row(
                modifier = Modifier.fillMaxWidth().wrapContentHeight().scale(animatedCount.value),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val c1 = problem.visualCount1
                val c2 = problem.visualCount2
                repeat(c1) {
                    Text("🎈", fontSize = 34.sp, modifier = Modifier.padding(1.dp))
                }
                if (c2 > 0) {
                    Text("➕", fontSize = 24.sp, modifier = Modifier.padding(horizontal = 8.dp), color = KidsPink)
                    repeat(c2) {
                        Text("🎈", fontSize = 34.sp, modifier = Modifier.padding(1.dp))
                    }
                }
            }
        }
        "candies" -> {
            Row(
                modifier = Modifier.fillMaxWidth().wrapContentHeight().scale(animatedCount.value),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val c1 = problem.visualCount1
                val c2 = problem.visualCount2
                repeat(c1) {
                    Text("🍬", fontSize = 32.sp, modifier = Modifier.padding(1.dp))
                }
                if (c2 > 0) {
                    Text("➖", fontSize = 24.sp, modifier = Modifier.padding(horizontal = 8.dp), color = KidsPink)
                    repeat(c2) {
                        Text("🍬", fontSize = 32.sp, modifier = Modifier.padding(1.dp))
                    }
                }
            }
        }
        "toys" -> {
            Row(
                modifier = Modifier.fillMaxWidth().wrapContentHeight().scale(animatedCount.value),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Multiplication grid display
                val rows = problem.visualCount1
                val cols = problem.visualCount2
                Column {
                    repeat(rows) {
                        Row {
                            repeat(cols) {
                                Text("🚗", fontSize = 28.sp, modifier = Modifier.padding(2.dp))
                            }
                        }
                    }
                }
            }
        }
        "shapes" -> {
            val sColor = KidsPink
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .scale(animatedCount.value),
                contentAlignment = Alignment.Center
            ) {
                when (problem.shapeType) {
                    "circle" -> {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(color = sColor, radius = size.minDimension / 2.5f)
                        }
                    }
                    "square" -> {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRect(
                                color = sColor,
                                topLeft = Offset(size.width * 0.15f, size.height * 0.15f),
                                size = androidx.compose.ui.geometry.Size(size.width * 0.7f, size.height * 0.7f)
                            )
                        }
                    }
                    "triangle" -> {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val path = androidx.compose.ui.graphics.Path().apply {
                                moveTo(size.width / 2, size.height * 0.15f)
                                lineTo(size.width * 0.15f, size.height * 0.85f)
                                lineTo(size.width * 0.85f, size.height * 0.85f)
                                close()
                            }
                            drawPath(path = path, color = sColor)
                        }
                    }
                    "star" -> {
                        Text("⭐", fontSize = 100.sp)
                    }
                    "heart" -> {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = "heart",
                            tint = sColor,
                            modifier = Modifier.size(100.dp)
                        )
                    }
                    else -> {
                        Text("🎁", fontSize = 80.sp)
                    }
                }
            }
        }
        "clock" -> {
            // Analog Clock face drawn on custom canvas
            val hr = problem.customClockHour
            val mn = problem.customClockMinute
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(animatedCount.value)
                    .border(4.dp, KidsBlue, CircleShape)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension / 2
                    val center = Offset(size.width / 2, size.height / 2)

                    // Draw reference hour markers (12, 3, 6, 9)
                    drawCircle(color = Color.Black, radius = 5f, center = center)

                    // Minute arm
                    val minAngle = (mn * 6f) - 90f // 6 degrees/min
                    val minLen = radius * 0.85f
                    val minTargetX = center.x + minLen * cos(Math.toRadians(minAngle.toDouble())).toFloat()
                    val minTargetY = center.y + minLen * sin(Math.toRadians(minAngle.toDouble())).toFloat()
                    drawLine(color = Color.Black, start = center, end = Offset(minTargetX, minTargetY), strokeWidth = 5f)

                    // Hour arm
                    val hrAngle = (hr * 30f + mn * 0.5f) - 90f // 30 degrees/hr
                    val hrLen = radius * 0.6f
                    val hrTargetX = center.x + hrLen * cos(Math.toRadians(hrAngle.toDouble())).toFloat()
                    val hrTargetY = center.y + hrLen * sin(Math.toRadians(hrAngle.toDouble())).toFloat()
                    drawLine(color = KidsPink, start = center, end = Offset(hrTargetX, hrTargetY), strokeWidth = 10f)
                }

                // Digital text support watermark helper below clock face
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("⏰", fontSize = 24.sp, modifier = Modifier.padding(bottom = 12.dp))
                }
            }
        }
        "fractions" -> {
            // Partitioned circle with shaded numerator
            val activeParts = problem.fractionNumerator
            val totalParts = problem.fractionDenominator
            Box(
                modifier = Modifier
                     .size(150.dp)
                     .scale(animatedCount.value),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val radius = size.minDimension / 2.5f

                    // Draw outer border circle
                    drawCircle(color = Color.Black, radius = radius, center = center, style = Stroke(width = 4f))

                    // Draw shaded slices and separator lines
                    val angleOffset = 360f / totalParts
                    for (i in 0 until totalParts) {
                        val startAngle = i * angleOffset - 90f
                        if (i < activeParts) {
                            // Shade fraction
                            drawArc(
                                color = KidsYellow,
                                startAngle = startAngle,
                                sweepAngle = angleOffset,
                                useCenter = true,
                                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                                topLeft = Offset(center.x - radius, center.y - radius)
                            )
                        }

                        // Slice line
                        val targetX = center.x + radius * cos(Math.toRadians(startAngle.toDouble())).toFloat()
                        val targetY = center.y + radius * sin(Math.toRadians(startAngle.toDouble())).toFloat()
                        drawLine(color = Color.Black, start = center, end = Offset(targetX, targetY), strokeWidth = 3f)
                    }
                }
            }
        }
        "money" -> {
            // Display money bills (custom Taka boxes)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .scale(animatedCount.value),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                problem.moneyBills.forEach { amt ->
                    Card(
                        modifier = Modifier
                            .padding(6.dp)
                            .size(width = 85.dp, height = 55.dp)
                            .border(2.dp, KidsGreen, RoundedCornerShape(8.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("${amt.toHanifi()} Taka", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = KidsGreen) // Rohingya Taka
                            Text("$amt TAKA", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
        "measurement" -> {
            // Side-by-side comparison bar representation
            val b1 = problem.visualCount1
            val b2 = problem.visualCount2
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .scale(animatedCount.value),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("𐴱", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(30.dp))
                    Box(
                        modifier = Modifier
                            .height(30.dp)
                            .fillMaxWidth(fraction = (b1 / 10f).coerceAtMost(1f))
                            .clip(RoundedCornerShape(8.dp))
                            .background(KidsPink)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("𐴲", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(30.dp))
                    Box(
                        modifier = Modifier
                            .height(30.dp)
                            .fillMaxWidth(fraction = (b2 / 10f).coerceAtMost(1f))
                            .clip(RoundedCornerShape(8.dp))
                            .background(KidsBlue)
                    )
                }
            }
        }
        else -> {
            // High intensity text calculation card
            Column(
                modifier = Modifier.fillMaxWidth().scale(animatedCount.value),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        problem.operand1,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        problem.operatorSymbol,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = KidsOrange
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        problem.operand2,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun FeedbackSection(isCorrect: Boolean, correctExplanation: String) {
    val messageRo = if (isCorrect) "Excellent!" else "You can do it!"
    val color = if (isCorrect) KidsGreen else KidsPink
    val emoji = if (isCorrect) "🎉" else "💡"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(emoji, fontSize = 28.sp, modifier = Modifier.padding(end = 8.dp))
            Column {
                Text(
                    text = messageRo,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                if (!isCorrect && correctExplanation.isNotEmpty()) {
                    Text(
                        text = "Correct Answer: $correctExplanation", // Explanation header
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}

@Composable
fun AnimalRaceTrack(rabbitPos: Float, elephantPos: Float) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(2.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFAED581).copy(alpha = 0.25f)) // Playful bright green pasture
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Track 1 for Player (Rabbit)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
            ) {
                // Divider line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White)
                        .align(Alignment.BottomCenter)
                )

                // Finish line check
                Text("🏁", fontSize = 20.sp, modifier = Modifier.align(Alignment.CenterEnd))

                // Rabbit animation positioning
                val animatedRabbitX by animateFloatAsState(
                    targetValue = rabbitPos / 10f,
                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                    label = "rabbitX"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = animatedRabbitX.coerceIn(0.1f, 0.95f))
                        .align(Alignment.CenterStart),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text("🐇", fontSize = 24.sp)
                }
            }

            // Track 2 for Opponent (Elephant)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
            ) {
                Text("🏁", fontSize = 20.sp, modifier = Modifier.align(Alignment.CenterEnd))

                val animatedElephantX by animateFloatAsState(
                    targetValue = elephantPos / 10f,
                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                    label = "elephantX"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = animatedElephantX.coerceIn(0.1f, 0.95f))
                        .align(Alignment.CenterStart),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text("🐘", fontSize = 24.sp)
                }
            }
        }
    }
}
