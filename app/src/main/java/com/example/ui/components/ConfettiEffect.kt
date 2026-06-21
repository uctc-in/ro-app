package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*
import kotlin.random.Random

data class ConfettiPiece(
    val xPercent: Float,
    var yPercent: Float,
    val speed: Float,
    val color: Color,
    val size: Float,
    val rotation: Float,
    val rotationSpeed: Float
)

@Composable
fun ConfettiEffect(modifier: Modifier = Modifier) {
    val confettiColors = listOf(KidsYellow, KidsBlue, KidsPink, KidsGreen, KidsOrange, KidsPurple, Color.White)
    val PiecesCount = 45

    val pieces = remember {
        List(PiecesCount) {
            ConfettiPiece(
                xPercent = Random.nextFloat(),
                yPercent = -Random.nextFloat() * 0.5f,
                speed = 0.5f + Random.nextFloat() * 1.5f,
                color = confettiColors.random(),
                size = 10f + Random.nextFloat() * 15f,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = 2f + Random.nextFloat() * 5f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val ticker by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confettiTicker"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        pieces.forEach { piece ->
            // Scale or position
            val yPos = (piece.yPercent + (ticker * piece.speed)) % 1.2f
            val xOffset = (piece.xPercent * size.width)
            val yOffset = (yPos * size.height)

            if (yPos in 0f..1.1f) {
                drawRect(
                    color = piece.color,
                    topLeft = Offset(xOffset, yOffset),
                    size = androidx.compose.ui.geometry.Size(piece.size, piece.size)
                )
            }
        }
    }
}
