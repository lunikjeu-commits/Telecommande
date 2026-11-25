package com.lcstudio.metzremote.ir

import android.content.Context
import android.hardware.ConsumerIrManager

/** Émission infrarouge NEC (38 kHz) via l'émetteur IR du téléphone. */
class IrSender(context: Context) {
    private val mgr =
        context.getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager

    val available: Boolean get() = mgr?.hasIrEmitter() == true

    fun send(code: Long) {
        val m = mgr ?: return
        if (!m.hasIrEmitter()) return
        m.transmit(38000, pattern(code))
    }

    companion object {
        /** Trame NEC : en-tête 9000/4500 µs, 32 bits (bit 31 en premier), marque finale. */
        fun pattern(code: Long): IntArray {
            val p = IntArray(67)
            p[0] = 9000
            p[1] = 4500
            var i = 2
            for (bit in 31 downTo 0) {
                p[i++] = 560
                p[i++] = if (((code shr bit) and 1L) == 1L) 1690 else 560
            }
            p[i] = 560
            return p
        }
    }
}
