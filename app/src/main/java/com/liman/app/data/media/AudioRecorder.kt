package com.liman.app.data.media

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

/**
 * Basit sesli not kaydedici. Ses, uygulamanın özel dosya alanına (filesDir)
 * yazılır; cihaz dışına çıkmaz.
 */
class AudioRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null

    fun start(): Boolean = runCatching {
        val file = File(context.filesDir, "voice_${System.currentTimeMillis()}.m4a")
        outputFile = file
        val rec = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        rec.setAudioSource(MediaRecorder.AudioSource.MIC)
        rec.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        rec.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        rec.setAudioEncodingBitRate(96_000)
        rec.setAudioSamplingRate(44_100)
        rec.setOutputFile(file.absolutePath)
        rec.prepare()
        rec.start()
        recorder = rec
        true
    }.getOrDefault(false)

    /** Kaydı bitirir; dosya yolunu döndürür (başarısızsa null). */
    fun stop(): String? {
        val rec = recorder ?: return null
        val path = outputFile?.absolutePath
        runCatching { rec.stop() }
        runCatching { rec.release() }
        recorder = null
        outputFile = null // kaydedilen dosyayı cancel() silmesin
        return path
    }

    fun cancel() {
        runCatching { recorder?.stop() }
        runCatching { recorder?.release() }
        recorder = null
        outputFile?.delete()
        outputFile = null
    }
}
