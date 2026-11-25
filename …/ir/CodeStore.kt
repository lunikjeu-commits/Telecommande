package com.lcstudio.metzremote.ir

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

/** Codes IR personnalisés (SharedPreferences). Observable par Compose. */
class CodeStore(context: Context) {
    private val p = context.getSharedPreferences("codes", Context.MODE_PRIVATE)

    private var version by mutableIntStateOf(0)

    var prefix: Int by mutableIntStateOf(p.getInt("prefix", 0x7070))
        private set

    @Suppress("UNUSED_EXPRESSION")
    fun get(b: IrButton): Long? {
        version // abonnement à la recomposition
        return if (p.contains(b.name)) p.getLong(b.name, 0L) else b.default
    }

    fun set(b: IrButton, code: Long) {
        p.edit().putLong(b.name, code).apply()
        version++
    }

    fun setPrefix(v: Int) {
        p.edit().putInt("prefix", v).apply()
        prefix = v
    }

    fun reset() {
        p.edit().clear().apply()
        prefix = 0x7070
        version++
    }
}
