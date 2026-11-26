package com.lcstudio.metzremote.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Input
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lcstudio.metzremote.ir.CodeStore
import com.lcstudio.metzremote.ir.IrButton
import com.lcstudio.metzremote.ir.IrSender
import kotlinx.coroutines.delay

@Composable
fun RemoteScreen(store: CodeStore, sender: IrSender, onSettings: () -> Unit) {
    var msg by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(msg) {
        if (msg != null) { delay(2500); msg = null }
    }

    fun fire(b: IrButton) {
        val code = store.get(b)
        when {
            !sender.available -> msg = "Aucun émetteur infrarouge détecté"
            code == null -> msg = "${b.label} : code à trouver dans Réglages"
            else -> sender.send(code)
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Pal.Bg, Pal.Bg2)))
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RemoteKey(
                    Modifier.size(68.dp), Icons.Rounded.PowerSettingsNew, "Power",
                    container = Pal.Red, tint = Color.White,
                ) { fire(IrButton.POWER) }
                RemoteKey(
                    Modifier.width(124.dp).height(56.dp), Icons.Rounded.Input, "Source",
                    text = "Source", shape = RoundedCornerShape(28.dp),
                ) { fire(IrButton.SOURCE) }
                RemoteKey(
                    Modifier.size(52.dp), Icons.Rounded.Settings, "Réglages",
                    container = Color.Transparent, tint = Pal.Muted,
                ) { onSettings() }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RemoteKey(Modifier.size(60.dp), Icons.Rounded.ArrowBack, "Retour") { fire(IrButton.BACK) }
                RemoteKey(Modifier.size(60.dp), Icons.Rounded.Home, "Home") { fire(IrButton.HOME) }
                RemoteKey(
                    Modifier.width(140.dp).height(60.dp), Icons.Rounded.PlayArrow, "YouTube",
                    text = "YouTube", container = Pal.Yt, tint = Color.White,
                    shape = RoundedCornerShape(30.dp),
                ) { fire(IrButton.YOUTUBE) }
            }

            DPad(::fire)

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                UpDownPill("VOL", IrButton.VOL_UP, IrButton.VOL_DOWN, Icons.Rounded.Add, Icons.Rounded.Remove, ::fire)
                RemoteKey(Modifier.size(64.dp), Icons.Rounded.VolumeOff, "Muet") { fire(IrButton.MUTE) }
                UpDownPill("CH", IrButton.CH_UP, IrButton.CH_DOWN, Icons.Rounded.KeyboardArrowUp, Icons.Rounded.KeyboardArrowDown, ::fire)
            }
        }

        msg?.let {
            Text(
                it,
                color = Pal.Text,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 18.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Pal.Surface)
                    .padding(horizontal = 18.dp, vertical = 10.dp),
            )
        }
    }
}

@Composable
private fun DPad(fire: (IrButton) -> Unit) {
    Box(
        Modifier.size(252.dp).clip(CircleShape).background(Pal.Surface),
        contentAlignment = Alignment.Center,
    ) {
        RemoteKey(
            Modifier.align(Alignment.TopCenter).padding(top = 6.dp).size(80.dp),
            Icons.Rounded.KeyboardArrowUp, "Haut", container = Color.Transparent,
            iconSize = 42.dp, repeat = true,
        ) { fire(IrButton.UP) }
        RemoteKey(
            Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp).size(80.dp),
            Icons.Rounded.KeyboardArrowDown, "Bas", container = Color.Transparent,
            iconSize = 42.dp, repeat = true,
        ) { fire(IrButton.DOWN) }
        RemoteKey(
            Modifier.align(Alignment.CenterStart).padding(start = 6.dp).size(80.dp),
            Icons.Rounded.KeyboardArrowLeft, "Gauche", container = Color.Transparent,
            iconSize = 42.dp, repeat = true,
        ) { fire(IrButton.LEFT) }
        RemoteKey(
            Modifier.align(Alignment.CenterEnd).padding(end = 6.dp).size(80.dp),
            Icons.Rounded.KeyboardArrowRight, "Droite", container = Color.Transparent,
            iconSize = 42.dp, repeat = true,
        ) { fire(IrButton.RIGHT) }
        RemoteKey(
            Modifier.size(96.dp), text = "OK", container = Pal.Accent, tint = Color.White,
        ) { fire(IrButton.OK) }
    }
}

@Composable
private fun UpDownPill(
    title: String,
    up: IrButton,
    down: IrButton,
    upIcon: ImageVector,
    downIcon: ImageVector,
    fire: (IrButton) -> Unit,
) {
    Column(
        Modifier
            .width(80.dp)
            .height(200.dp)
            .clip(RoundedCornerShape(40.dp))
            .background(Pal.Surface),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        RemoteKey(
            Modifier.fillMaxWidth().weight(1f), upIcon, up.label,
            container = Color.Transparent, shape = RoundedCornerShape(40.dp),
            iconSize = 34.dp, repeat = true,
        ) { fire(up) }
        Text(title, color = Pal.Muted, fontSize = 12.sp, letterSpacing = 2.sp)
        RemoteKey(
            Modifier.fillMaxWidth().weight(1f), downIcon, down.label,
            container = Color.Transparent, shape = RoundedCornerShape(40.dp),
            iconSize = 34.dp, repeat = true,
        ) { fire(down) }
    }
}
