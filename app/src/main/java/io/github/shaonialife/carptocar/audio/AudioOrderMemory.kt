package io.github.shaonialife.carptocar.audio

import android.content.Context
import android.content.SharedPreferences

/** 按接收端记住「音频必须提前到 RECORD 之前」（补发 RECORD 会被回 500 的那类盒子）。 */
object AudioOrderMemory {
    private const val PREFS = "audio_order"

    @Volatile
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }

    fun prefersAudioFirst(identity: String): Boolean {
        if (identity.isBlank() || identity == "unknown") return false
        return prefs?.getBoolean(key(identity), false) ?: false
    }

    fun markRecordRejected(identity: String) {
        if (identity.isBlank() || identity == "unknown") return
        prefs?.edit()?.putBoolean(key(identity), true)?.apply()
    }

    private fun key(identity: String): String = identity.lowercase().replace(' ', '_').take(80)
}