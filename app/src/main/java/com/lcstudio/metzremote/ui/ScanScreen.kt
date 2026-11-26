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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

/** Adresses NEC courantes des TV (octet 1, octet 2 tels qu'envoyés sur le fil). */
private val ADDRS = listOf(
    0x0E to 0x0E, 0x70 to 0x70, 0x00 to 0xFF, 0x00 to 0xDF, 0x00 to 0xBD, 0x00 to 0x7F,
    0x04 to 0xFB, 0x08 to 0xF7, 0x10 to 0xEF, 0x20 to 0xDF, 0x40 to 0xBF, 0x80 to 0x7F,
    0x01 to 0xFE, 0x02 to 0xFD, 0x86 to 0x05, 0x80 to 0x68, 0x86 to 0x02, 0x83 to 0x55,
    0x18 to 0xE7, 0x1C to 0xE3,
)

private fun rev(x: Int): Int {
    var r = 0
    for (i in 0..7) if (((x shr i) and 1) == 1) r = r or (1 shl (7 - i))
    return r
}

/** Trame NEC « vraie » (bits LSB first) convertie au format MSB first utilisé par IrSender. */
private fun wire(a0: Int, a1: Int, cmd: Int): Long =
    (rev(a0).toLong() shl 24) or (rev(a1).toLong() shl 16) or
        (rev(cmd).toLong() shl 8) or rev(cmd xor 0xFF).toLong()

/**
 * Cherche le code d'un bouton.
 * Mode « Adresses » : essaie les adresses TV courantes x commandes 00..FF (pour trouver Power).
 * Mode « Préfixe » : commandes 00..FF avec le préfixe enregistré (pour les autres boutons).
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
    var addrMode by remember { mutableStateOf(button == IrButton.POWER) }
    var cur by remember { mutableIntStateOf(-1) }
    var ai by remember { mutableIntStateOf(0) }
    var running by remember { mutableStateOf(false) }
    var target by remember { mutableStateOf(button) }

    fun valueAt(c: Int, a: Int): Long =
        if (addrMode) wire(ADDRS[a].first, ADDRS[a].second, c)
        else (prefix.toLong() shl 16) or (c.toLong() shl 8) or (c xor 0xFF).toLong()

    fun fire() {
        if (cur >= 0) sender.send(valueAt(cur, ai))
    }

    fun step(dir: Int) {
        if (addrMode) {
            var c = cur + dir
            var a = ai
            if (c > 255) { c = 0; a = (a + 1) % ADDRS.size }
            if (c < 0) { c = 255; a = (a - 1 + ADDRS.size) % ADDRS.size }
            cur = c
            ai = a
        } else {
            var n = cur
            for (k in 0 until 256) {
                n = (n + dir + 256) % 256
                if (n !in skip) { cur = n; break }
            }
        }
        fire()
    }

    fun setMode(addr: Boolean) {
        running = false
        addrMode = addr
        cur = -1
        ai = 0
    }

    LaunchedEffect(running) {
        while (running) {
            step(1)
            delay(if (addrMode) 180L else 500L)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Pal.Bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) { Icon(Icons.Rounded.ArrowBack, "Retour", tint = Pal.Text) }
            Text("Scanner : ${button.label}", color = Pal.Text, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(10.dp))
        Text(
            "TV allumée, haut du téléphone pointé vers elle (1 à 2 m). Lance le scan. " +
                "Dès que la TV réagit de n'importe quelle façon (volume, menu, source, veille...) : " +
                "Pause, recule avec ◀ (ou −5) jusqu'à la réaction, choisis le bouton concerné, puis enregistre.",
            color = Pal.Muted, fontSize = 14.sp,
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (addrMode) FilledTonalButton(onClick = { setMode(true) }) { Text("Adresses") }
            else TextButton(onClick = { setMode(true) }) { Text("Adresses") }
            if (!addrMode) FilledTonalButton(onClick = { setMode(false) }) { Text("Préfixe %04X".format(prefix)) }
            else TextButton(onClick = { setMode(false) }) { Text("Préfixe %04X".format(prefix)) }
        }
        Spacer(Modifier.height(20.dp))
        Text(
            if (cur < 0) "—" else "%02X".format(cur),
            color = Pal.Accent, fontSize = 64.sp, fontWeight = FontWeight.Bold,
        )
        Text(
            if (addrMode) "Adresse %02X %02X  (%d/%d)".format(ADDRS[ai].first, ADDRS[ai].second, ai + 1, ADDRS.size)
            else "Préfixe %04X".format(prefix),
            color = Pal.Muted, fontSize = 14.sp,
        )
        Spacer(Modifier.height(20.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (addrMode) FilledTonalButton(onClick = { running = false; step(-5) }) { Text("−5") }
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
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { running = false; fire() }, enabled = cur >= 0) { Text("Renvoyer") }
            if (addrMode) TextButton(onClick = {
                ai = (ai + 1) % ADDRS.size
                cur = -1
                step(1)
            }) { Text("Adresse suivante") }
        }
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = {
            val all = IrButton.entries
            target = all[(target.ordinal + 1) % all.size]
        }) { Text("Enregistrer comme : ${target.label}  (toucher pour changer)") }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                val code = valueAt(cur, ai)
                store.set(target, code)
                if (addrMode) store.setPrefix((code shr 16).toInt())
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
