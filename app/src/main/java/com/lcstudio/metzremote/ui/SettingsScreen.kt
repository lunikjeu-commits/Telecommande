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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lcstudio.metzremote.ir.CodeStore
import com.lcstudio.metzremote.ir.IrButton
import com.lcstudio.metzremote.ir.IrSender

@Composable
fun SettingsScreen(
    store: CodeStore,
    sender: IrSender,
    onBack: () -> Unit,
    onScan: (IrButton) -> Unit,
) {
    var prefixTxt by remember { mutableStateOf("%04X".format(store.prefix)) }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .background(Pal.Bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.ArrowBack, "Retour", tint = Pal.Text)
                }
                Text("Réglages", color = Pal.Text, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        item {
          Column {
            Text(
                "Préfixe d'adresse IR (4 chiffres hexa). 7070 = base Skyworth/Coocaa. " +
                    "Change-le seulement si rien ne réagit.",
                color = Pal.Muted, fontSize = 13.sp,
            )
            OutlinedTextField(
                value = prefixTxt,
                onValueChange = { t ->
                    val u = t.uppercase().filter { it in "0123456789ABCDEF" }.take(4)
                    prefixTxt = u
                    if (u.length == 4) store.setPrefix(u.toInt(16))
                },
                label = { Text("Préfixe") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            )
          }
        }
        items(IrButton.entries.toList()) { b ->
            val code = store.get(b)
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Pal.Surface)
                    .padding(start = 14.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(b.label, color = Pal.Text, fontWeight = FontWeight.SemiBold)
                    Text(
                        code?.let { "0x%08X".format(it) } ?: "non configuré",
                        color = if (code == null) Pal.Red else Pal.Muted,
                        fontSize = 12.sp,
                    )
                }
                TextButton(onClick = { code?.let { sender.send(it) } }, enabled = code != null) {
                    Text("Tester")
                }
                TextButton(onClick = { onScan(b) }) { Text("Scanner") }
            }
        }
        item {
          Column {
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = {
                store.reset()
                prefixTxt = "%04X".format(store.prefix)
            }) { Text("Réinitialiser les codes") }
            Text(
                "LC STUDIO · lunikapp",
                color = Pal.Muted, fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 18.dp),
            )
          }
        }
    }
}
