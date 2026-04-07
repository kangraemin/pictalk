package com.kangraemin.pictalk.data.repository

import android.content.Context
import com.kangraemin.pictalk.domain.model.DownloadState
import com.kangraemin.pictalk.domain.repository.ModelRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient,
) : ModelRepository {

    private val modelFile: File get() = File(context.filesDir, MODEL_FILENAME)

    override fun isModelReady(): Boolean = modelFile.exists() && modelFile.length() > 0

    override fun modelPath(): String = modelFile.absolutePath

    override fun downloadModel(): Flow<DownloadState> = flow {
        if (isModelReady()) { emit(DownloadState.Complete); return@flow }

        emit(DownloadState.Downloading(0))
        val response = runCatching {
            okHttpClient.newCall(Request.Builder().url(MODEL_DOWNLOAD_URL).build()).execute()
        }.getOrElse {
            emit(DownloadState.Error("네트워크 오류: ${it.message}")); return@flow
        }

        if (!response.isSuccessful) {
            emit(DownloadState.Error("다운로드 실패: HTTP ${response.code}")); return@flow
        }

        val body = response.body ?: run {
            emit(DownloadState.Error("응답 본문이 비어 있습니다")); return@flow
        }

        val totalBytes = body.contentLength()
        val tempFile = File(context.filesDir, "$MODEL_FILENAME.tmp")

        runCatching {
            body.byteStream().use { input ->
                tempFile.outputStream().use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var downloaded = 0L
                    var lastPercent = -1
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        downloaded += read
                        if (totalBytes > 0) {
                            val percent = (downloaded * 100 / totalBytes).toInt()
                            if (percent != lastPercent) { lastPercent = percent; emit(DownloadState.Downloading(percent)) }
                        }
                    }
                }
            }
            tempFile.renameTo(modelFile)
            emit(DownloadState.Complete)
        }.onFailure { tempFile.delete(); emit(DownloadState.Error("저장 실패: ${it.message}")) }
    }.flowOn(Dispatchers.IO)

    companion object {
        const val MODEL_FILENAME = "gemma-4-E4B-it.litertlm"
        const val MODEL_DOWNLOAD_URL = "https://huggingface.co/litert-community/gemma-4-E4B-it-litert-lm/resolve/main/gemma-4-E4B-it.litertlm"
    }
}
