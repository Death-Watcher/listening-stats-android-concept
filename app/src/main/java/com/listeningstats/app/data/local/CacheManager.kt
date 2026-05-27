package com.listeningstats.app.data.local

import android.content.Context
import java.io.File

class CacheManager(private val context: Context) {

    private val cacheDir = File(context.cacheDir, "stats_cache").also { it.mkdirs() }

    fun get(key: String): String? {
        val file = File(cacheDir, key)
        if (!file.exists()) return null
        return try { file.readText() } catch (e: Exception) { null }
    }

    fun set(key: String, value: String) {
        val file = File(cacheDir, key)
        try { file.writeText(value) } catch (_: Exception) {}
    }

    fun clear() {
        cacheDir.deleteRecursively()
        cacheDir.mkdirs()
    }

    fun evict(key: String) {
        File(cacheDir, key).delete()
    }
}
