package com.lcstudio.metzremote.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Bouton tactile : retour haptique, effet d'appui, répétition si maintenu (volume, flèches...). */
@Composable
fun RemoteKey(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    desc: String = "",
    text: String? = null,
    container: Color = Pal.Surface,
    tint: Color = Pal.Text,
    shape: Shape = CircleShape,
    iconSize: Dp = 30.dp,
    repeat: Boolean = false,
    onPress: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val action by rememberUpdatedState(onPress)
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.94f else 1f, tween(90), label = "key")

    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(shape)
            .background(if (pressed) Pal.Accent.copy(alpha = 0.45f) else container)
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    pressed = true
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    action()
                    val job = if (repeat) scope.launch {
                        delay(450)
                        while (true) { action(); delay(140) }
                    } else null
                    tryAwaitRelease()
                    job?.cancel()
                    pressed = false
                })
            },
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) Icon(icon, desc, tint = tint, modifier = Modifier.size(iconSize))
            if (icon != null && text != null) Spacer(Modifier.width(8.dp))
            if (text != null) Text(text, color = tint, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }
    }
}
