package com.lcstudio.metzremote.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private const val NAME = "LC STUDIO"

/** Splash LC STUDIO : logo 0,5 s → texte lettre par lettre 0,8 s → pause 0,5 s → fondu. */
@Composable
fun Splash(onDone: () -> Unit) {
    val logo = remember { Animatable(0f) }
    val typed = remember { Animatable(0f) }
    val exit = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        logo.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
        typed.animateTo(1f, tween(800, easing = LinearEasing))
        delay(500)
        exit.animateTo(0f, tween(350))
        onDone()
    }

    Box(
        Modifier
            .fillMaxSize()
            .alpha(exit.value)
            .background(Brush.verticalGradient(listOf(Pal.Bg, Pal.Bg2))),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            LcMark(
                Modifier
                    .size(124.dp)
                    .graphicsLayer {
                        alpha = logo.value
                        val s = 0.8f + 0.2f * logo.value
                        scaleX = s
                        scaleY = s
                    },
            )
            Spacer(Modifier.height(30.dp))
            val n = (NAME.length * typed.value).roundToInt()
            Text(
                buildAnnotatedString {
                    append(NAME.take(n))
                    withStyle(SpanStyle(color = Color.Transparent)) { append(NAME.drop(n)) }
                },
                color = Pal.Text,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 7.sp,
            )
        }
    }
}

/** Monogramme vectoriel : « L » et « C » imbriqués dans un cadre arrondi. */
@Composable
fun LcMark(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val s = size.minDimension / 100f
        val g = Brush.linearGradient(
            listOf(Color(0xFF8FA8FF), Color(0xFF5CE1E6)),
            start = Offset.Zero,
            end = Offset(size.width, size.height),
        )
        drawRoundRect(
            brush = Brush.linearGradient(listOf(Color(0xFF1F2B52), Color(0xFF121A33))),
            cornerRadius = CornerRadius(26 * s, 26 * s),
        )
        drawRoundRect(
            brush = g,
            topLeft = Offset(2 * s, 2 * s),
            size = Size(96 * s, 96 * s),
            cornerRadius = CornerRadius(24 * s, 24 * s),
            style = Stroke(width = 3 * s),
        )
        val stroke = Stroke(width = 8 * s, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val l = Path().apply {
            moveTo(30 * s, 28 * s)
            lineTo(30 * s, 72 * s)
            lineTo(54 * s, 72 * s)
        }
        drawPath(l, brush = g, style = stroke)
        drawArc(
            brush = g,
            startAngle = 45f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = Offset(36 * s, 28 * s),
            size = Size(44 * s, 44 * s),
            style = stroke,
        )
    }
}
