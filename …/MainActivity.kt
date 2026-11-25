package com.lcstudio.metzremote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
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
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val store = CodeStore(this)
        val sender = IrSender(this)
        setContent { LcTheme { App(store, sender) } }
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
