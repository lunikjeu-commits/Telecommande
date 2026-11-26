package com.lcstudio.metzremote.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lcstudio.metzremote.ir.CodeStore
import com.lcstudio.metzremote.ir.IrButton
import com.lcstudio.metzremote.ir.IrSender
import kotlinx.coroutines.delay

/**
 * Cherche le code d'un bouton : envoie les commandes 00..FF une par une (préfixe fixe),
 * en sautant celles déjà utilisées par les autres boutons.
 */
@Composable
fun ScanScreen(button: IrButton, store: CodeStore, sender: IrSender, onClose: () -> Unit) {
    val prefix = store.prefix
    val skip = remember {
        IrButton.entries.filter { it != button }
            .mapNotNull { store.get(it) }
            .filter { (it shr 16).toInt() == prefix }
            .map { ((it shr 8) and 0xFF).toInt() }
            .toSet()
    }
    var cur by remember { mutableIntStateOf(-1) }
    var running by remember { mutableStateOf(false) }

    fun codeOf(b: Int): Long =
        (prefix.toLong() shl 16) or (b.toLong() shl 8) or (b xor 0xFF).toLong()

    fun step(dir: Int) {
        var n = cur
        repeat(256) {
            n = (n + dir + 256) % 256
            if (n !in skip) {
                cur = n
                sender.send(codeOf(n))
                return
            }
        }
    }

    LaunchedEffect(running) {
        while (running) {
            step(1)
            delay(500)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Pal.Bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) { Icon(Icons.Rounded.ArrowBack, "Retour", tint = Pal.Text) }
            Text("Scanner : ${button.label}", color = Pal.Text, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "Pointe le téléphone vers la TV, lance le scan. Dès que la TV réagit, mets en pause, " +
                "reviens en arrière si besoin (◀) jusqu'à la bonne réaction, puis enregistre.",
            color = Pal.Muted, fontSize = 14.sp,
        )
        Spacer(Modifier.height(36.dp))
        Text(
            if (cur < 0) "—" else "0x%02X".format(cur),
            color = Pal.Accent, fontSize = 64.sp, fontWeight = FontWeight.Bold,
        )
        Text("Préfixe %04X".format(prefix), color = Pal.Muted, fontSize = 13.sp)
        Spacer(Modifier.height(36.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilledTonalButton(onClick = { running = false; step(-1) }) {
                Icon(Icons.Rounded.SkipPrevious, "Précédent")
            }
            Button(onClick = { running = !running }) {
                Icon(if (running) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, "Auto")
                Text(if (running) " Pause" else " Scanner")
            }
            FilledTonalButton(onClick = { running = false; step(1) }) {
                Icon(Icons.Rounded.SkipNext, "Suivant")
            }
        }
        TextButton(
            onClick = { running = false; if (cur >= 0) sender.send(codeOf(cur)) },
            enabled = cur >= 0,
        ) { Text("Renvoyer ce code") }
        Spacer(Modifier.weight(1f))
        Button(
            onClick = {
                store.set(button, codeOf(cur))
                onClose()
            },
            enabled = cur >= 0,
            colors = ButtonDefaults.buttonColors(containerColor = Pal.Ok),
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) {
            Icon(Icons.Rounded.Check, null)
            Text("  La TV a réagi : enregistrer", fontSize = 16.sp)
        }
    }
}
