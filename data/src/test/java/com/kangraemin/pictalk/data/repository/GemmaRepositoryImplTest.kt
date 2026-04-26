package com.kangraemin.pictalk.data.repository

import com.kangraemin.pictalk.domain.repository.ArasaacRepository
import com.kangraemin.pictalk.domain.repository.ModelRepository
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before; import org.junit.Test

class GemmaRepositoryImplTest {
    private lateinit var repo: GemmaRepositoryImpl
    private val arasaacRepository = mockk<ArasaacRepository>(relaxed = true)
    private val modelRepository = mockk<ModelRepository>(relaxed = true)
    @Before fun setUp() { repo = GemmaRepositoryImpl(mockk(relaxed = true), arasaacRepository, modelRepository) }

    @Test fun `buildPrompt includes AAC context reasoning keywords`() {
        val prompt = repo.buildPrompt()
        assertTrue(prompt.contains("AAC"))
        assertTrue(prompt.contains("상황"))
        assertTrue(prompt.contains("한국어"))
    }
    @Test fun `parseLabels splits comma-separated string`() { assertEquals(listOf("먹어요","사과","배고파요","주세요"), repo.parseLabels("먹어요, 사과, 배고파요, 주세요").map { it.text }) }
    @Test fun `parseLabels trims whitespace and trailing dots`() { assertEquals(listOf("먹어요","사과"), repo.parseLabels("  먹어요 ,  사과.  ").map { it.text }) }
    @Test fun `parseLabels filters empty tokens`() { assertEquals(listOf("먹어요"), repo.parseLabels(",, 먹어요,,").map { it.text }) }
    @Test fun `parseLabels takes at most 6 labels`() { assertEquals(6, repo.parseLabels("a,b,c,d,e,f,g,h").size) }
    @Test fun `parseLabels filters labels longer than 10 chars`() { assertFalse(repo.parseLabels("짧은거, 매우매우매우길어서필터됨, 또짧은").any { it.text.length > 10 }) }
    @Test fun `isReady returns false before initialize`() { assertFalse(repo.isReady()) }
}
