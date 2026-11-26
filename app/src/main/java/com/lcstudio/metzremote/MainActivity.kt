package com.lcstudio.metzremote

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lcstudio.metzremote.ui.Pal
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.lcstudio.metzremote.ir.CodeStore
import com.lcstudio.metzremote.ir.IrButton
import com.lcstudio.metzremote.ir.IrSender
import com.lcstudio.metzremote.ui.LcTheme
import com.lcstudio.metzremote.ui.RemoteScreen
import com.lcstudio.metzremote.ui.ScanScreen
import com.lcstudio.metzremote.ui.SettingsScreen
import com.lcstudio.metzremote.ui.Splash

sealed interface Screen {
    data object Remote : Screen
    data object Settings : Screen
    data class Scan(val button: IrButton) : Screen
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Capture des plantages : le détail s'affiche au lancement suivant.
        val prefs = getSharedPreferences("crash", MODE_PRIVATE)
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            prefs.edit().putString("log", Log.getStackTraceString(e)).commit()
            previous?.uncaughtException(t, e)
        }
        super.onCreate(savedInstanceState)

        val crash = prefs.getString("log", null)
        if (crash != null) {
            prefs.edit().remove("log").apply()
            setContent { LcTheme { CrashScreen(crash) } }
            return
        }
        enableEdgeToEdge()
        val store = CodeStore(this)
        val sender = IrSender(this)
        setContent { LcTheme { App(store, sender) } }
    }
}

@Composable
private fun CrashScreen(log: String) {
    Column(
        Modifier.fillMaxSize().background(Pal.Bg).statusBarsPadding()
            .verticalScroll(rememberScrollState()).padding(16.dp),
    ) {
        Text("Plantage détecté — appuie longuement sur le texte pour le copier", color = Pal.Red, fontSize = 15.sp)
        SelectionContainer {
            Text(log.take(3500), color = Pal.Text, fontSize = 11.sp, modifier = Modifier.padding(top = 12.dp))
        }
        Text("Relance ensuite l'appli.", color = Pal.Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun App(store: CodeStore, sender: IrSender) {
    var screen by remember { mutableStateOf<Screen>(Screen.Remote) }
    var splash by remember { mutableStateOf(true) }

    BackHandler(enabled = screen != Screen.Remote) {
        screen = if (screen is Screen.Scan) Screen.Settings else Screen.Remote
    }

    Box(Modifier.fillMaxSize()) {
        when (val s = screen) {
            Screen.Remote -> RemoteScreen(store, sender) { screen = Screen.Settings }
            Screen.Settings -> SettingsScreen(
                store, sender,
                onBack = { screen = Screen.Remote },
                onScan = { screen = Screen.Scan(it) },
            )
            is Screen.Scan -> ScanScreen(s.button, store, sender) { screen = Screen.Settings }
        }
        if (splash) Splash { splash = false }
    }
}
