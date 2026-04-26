package com.kangraemin.pictalk.data.repository

import android.content.Context
import com.kangraemin.pictalk.domain.model.AacCategory
import com.kangraemin.pictalk.domain.model.AacLabel
import com.kangraemin.pictalk.domain.model.ArasaacDownloadState
import com.kangraemin.pictalk.domain.model.ArasaacSymbol
import com.kangraemin.pictalk.domain.repository.ArasaacRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArasaacRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient,
) : ArasaacRepository {

    private val arasaacDir = File(context.filesDir, "arasaac")
    private val imageDir = File(arasaacDir, "images").also { it.mkdirs() }
    private val metadataFile = File(arasaacDir, "metadata.json")
    private val completeFile = File(arasaacDir, ".complete")

    private var cachedSymbols: List<ArasaacSymbol>? = null

    override fun isReady(): Boolean = completeFile.exists()

    override fun downloadAll(): Flow<ArasaacDownloadState> = flow {
        emit(ArasaacDownloadState.DownloadingMetadata(0))
        val metadataJson = fetchMetadata()
        arasaacDir.mkdirs()
        metadataFile.writeText(metadataJson)
        emit(ArasaacDownloadState.DownloadingMetadata(100))

        val symbols = parseMetadata(metadataJson)
        cachedSymbols = symbols
        val total = symbols.size
        var downloaded = imageDir.listFiles()?.size ?: 0

        symbols.chunked(20).forEach { chunk ->
            coroutineScope {
                chunk.map { symbol ->
                    async(Dispatchers.IO) {
                        val imageFile = File(imageDir, "${symbol.id}.png")
                        if (!imageFile.exists()) downloadImage(symbol.id, imageFile)
                    }
                }.awaitAll()
            }
            downloaded += chunk.size
            emit(
                ArasaacDownloadState.DownloadingImages(
                    progressPercent = (downloaded * 100 / total),
                    downloaded = downloaded,
                    total = total,
                )
            )
        }
        completeFile.createNewFile()
        emit(ArasaacDownloadState.Complete)
    }.flowOn(Dispatchers.IO)

    private fun fetchMetadata(): String {
        val request = Request.Builder()
            .url("https://api.arasaac.org/api/pictograms/all/ko")
            .build()
        return okHttpClient.newCall(request).execute().use { it.body!!.string() }
    }

    private fun downloadImage(id: Int, dest: File) {
        val url = "https://static.arasaac.com/pictograms/arasaac/$id/${id}_500.png"
        val request = Request.Builder().url(url).build()
        okHttpClient.newCall(request).execute().use { resp ->
            dest.outputStream().use { out -> resp.body!!.byteStream().copyTo(out) }
        }
    }

    private fun parseMetadata(json: String): List<ArasaacSymbol> {
        val array = JSONArray(json)
        val result = mutableListOf<ArasaacSymbol>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val id = obj.getInt("_id")
            val keywords = obj.optJSONArray("keywords")
            val keyword = if (keywords != null && keywords.length() > 0) {
                keywords.getJSONObject(0).optString("keyword", "")
            } else ""
            val tags = mutableListOf<String>()
            obj.optJSONArray("tags")?.let { tagsArr ->
                for (j in 0 until tagsArr.length()) tags.add(tagsArr.getString(j))
            }
            val categories = mutableListOf<String>()
            obj.optJSONArray("categories")?.let { catArr ->
                for (j in 0 until catArr.length()) categories.add(catArr.getString(j))
            }
            if (keyword.isNotEmpty()) {
                result.add(
                    ArasaacSymbol(
                        id = id,
                        keyword = keyword,
                        tags = tags,
                        categories = categories,
                        localImagePath = File(imageDir, "$id.png").absolutePath,
                    )
                )
            }
        }
        return result
    }

    private fun loadSymbols(): List<ArasaacSymbol> {
        cachedSymbols?.let { return it }
        if (!metadataFile.exists()) return emptyList()
        return parseMetadata(metadataFile.readText()).also { cachedSymbols = it }
    }

    override fun findByKeyword(keyword: String): ArasaacSymbol? =
        loadSymbols().firstOrNull {
            it.keyword.contains(keyword, ignoreCase = true) ||
                    keyword.contains(it.keyword, ignoreCase = true)
        }

    override fun getCategories(): List<AacCategory> {
        val symbols = loadSymbols()
        val categoryDefs = listOf(
            Triple("emotion", listOf("feelings", "emotions"), "감정"),
            Triple("food", listOf("food", "drinks"), "음식"),
            Triple("action", listOf("verbs", "actions"), "행동"),
            Triple("place", listOf("places", "school"), "장소"),
            Triple("body", listOf("body", "health"), "신체·건강"),
        )
        return categoryDefs.map { (id, keys, name) ->
            val matched = symbols.filter { sym ->
                sym.categories.any { cat -> keys.any { key -> cat.contains(key, ignoreCase = true) } }
            }.take(20)
            val iconId = matched.firstOrNull()?.id ?: 0
            AacCategory(
                id = id,
                name = name,
                iconSymbolId = iconId,
                labels = matched.map { AacLabel(text = it.keyword, symbolId = it.id, localImagePath = it.localImagePath) },
            )
        }
    }

    override fun getCategory(id: String): AacCategory? = getCategories().find { it.id == id }

    override fun search(query: String): List<ArasaacSymbol> =
        loadSymbols().filter {
            it.keyword.contains(query, ignoreCase = true) ||
                    it.tags.any { tag -> tag.contains(query, ignoreCase = true) }
        }.take(30)
}
