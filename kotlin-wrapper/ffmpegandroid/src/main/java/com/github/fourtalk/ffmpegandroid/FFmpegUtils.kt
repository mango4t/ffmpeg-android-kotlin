package com.github.fourtalk.ffmpegandroid

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import java.io.*
import java.util.*

internal class NativeCpuHelper {
    external fun isCpuSupported(): Boolean

    @Suppress("DEPRECATION")
    companion object {
        @SuppressLint("ConstantLocale")
        private val locale = Locale.getDefault()

        init {
            if (Build.CPU_ABI.toLowerCase(locale) == "armeabi-v7a")
                System.loadLibrary("cpucheck")
        }

        val supportsFFmpeg: Boolean =
                when (Build.CPU_ABI.toLowerCase(locale)) {
                    "x86", "x86_64", "arm64-v8a" -> true
                    "armeabi-v7a" -> NativeCpuHelper().isCpuSupported()
                    else -> false
                }

        fun assetsDir(context: Context): String =
                when {
                    context.assets.list(Build.CPU_ABI.toLowerCase(locale))?.contains(ffmpegFileName) == true ->
                        Build.CPU_ABI.toLowerCase(locale)

                    // старая схема для apk (без сборки x64)
                    Build.CPU_ABI.toLowerCase(locale).startsWith("arm") ->
                        "armeabi-v7a"
                    Build.CPU_ABI.toLowerCase(locale).startsWith("x86") ->
                        "x86"
                    else ->
                        "armeabi-v7a"
                } // fallback
    }
}

private const val TAG = "FFmpegUtils"
private const val ffmpegFileName = "libffmpeg.so"
private const val h264FileName = "libopenh264.so"
private const val DEFAULT_BUFFER_SIZE = 1024 * 4
private const val EOF = -1

fun copyBinaryFromAssetsToData(context: Context, fileNameFromAssets: String, outputFileName: String): Boolean {
    var rc = false
    var src: InputStream? = null
    try {
        src = context.assets.open(fileNameFromAssets)

        val dst = FileOutputStream(File(getFilesDirectory(context), outputFileName))
        try {
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var n: Int = src.read(buffer)
            while (EOF != n) {
                dst.write(buffer, 0, n)
                n = src.read(buffer)
            }
            dst.flush()
            rc = true
        } finally {
            dst.close()
        }
    } catch (e: Throwable) {
        Log.e(TAG, "issue in coping binary from assets to data. ", e)
    } finally {
        src?.close()
    }
    return rc
}

private fun getFilesDirectory(context: Context): File {
    return context.filesDir
}

private fun getExecuteDir(context: Context): String =
    context.applicationInfo.nativeLibraryDir

internal fun getFFmpeg(context: Context): String =
    getExecuteDir(context) + File.separator + ffmpegFileName

internal fun getH264(context: Context): String =
    getExecuteDir(context) + File.separator + h264FileName

internal fun getFFmpegDefaultEnvironment(context: Context, envp: Map<String, String>?): List<String> {
    val rc = mutableListOf(
            "LD_LIBRARY_PATH=" + getExecuteDir(context),
            "ANDROID_DATA=/data",
            "ANDROID_ROOT=/system")
    if (envp != null)
        rc.addAll(envp.map { "${it.key}=${it.value}" })
    return rc
}

internal fun convertInputStreamToString(inputStream: InputStream): String? {
    try {
        val r = BufferedReader(InputStreamReader(inputStream))
        val sb = StringBuilder()
        var str = r.readLine()
        while (str != null) {
            sb.append(str)
            str = r.readLine()
        }
        return sb.toString()
    } catch (e: IOException) {
        Log.e(TAG, "error converting input stream to string", e)
    }
    return null
}

internal fun isProcessCompleted(process: Process?): Boolean =
    try {
        process?.exitValue()
        true
    } catch (e: IllegalThreadStateException) {
        false
    }

internal fun openContentInputStream(context: Context, uri: Uri): InputStream? =
    try {
        context.contentResolver.openInputStream(uri) ?: FileInputStream(uri.path)
    } catch (ex: Exception) {
        FileInputStream(uri.path)
    }

/** get real InputStream size */
internal fun InputStream.getInputStreamReadLength(): Int {
    try {
        var rc: Int = this.available()
        if (rc > 0)
            return rc
        rc = 0

        val skipBuffer = ByteArray(2048)
        while (true) {
            val readBytes = this.read(skipBuffer, 0, 2048)
            if (readBytes <= 0)
                return rc
            rc += readBytes
        }
    } catch (ex: Exception) {
        Log.e("getInputStreamSize", "#failed to open InputStream -- ${ex.message}")
    }
    return 0
}

fun String.withFileUriPrefix(): String = "file://" + this

fun File.withFileUriPrefix(): Uri = Uri.parse(this.path.withFileUriPrefix())
