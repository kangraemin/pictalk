package com.kangraemin.pictalk.data.repository

import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before; import org.junit.Test

class GemmaRepositoryImplTest {
    private lateinit var repo: GemmaRepositoryImpl
    @Before fun setUp() { repo = GemmaRepositoryImpl(mockk(relaxed = true)) }

    @Test fun `buildPrompt includes Korean and AAC keywords`() { assertTrue(repo.buildPrompt().run { contains("Korean") && contains("AAC") }) }
    @Test fun `parseLabels splits comma-separated string`() { assertEquals(listOf("먹어요","사과","배고파요","주세요"), repo.parseLabels("먹어요, 사과, 배고파요, 주세요").map { it.text }) }
    @Test fun `parseLabels trims whitespace and trailing dots`() { assertEquals(listOf("먹어요","사과"), repo.parseLabels("  먹어요 ,  사과.  ").map { it.text }) }
    @Test fun `parseLabels filters empty tokens`() { assertEquals(listOf("먹어요"), repo.parseLabels(",, 먹어요,,").map { it.text }) }
    @Test fun `parseLabels takes at most 6 labels`() { assertEquals(6, repo.parseLabels("a,b,c,d,e,f,g,h").size) }
    @Test fun `parseLabels filters labels longer than 10 chars`() { assertFalse(repo.parseLabels("짧은거, 매우매우매우길어서필터됨, 또짧은").any { it.text.length > 10 }) }
    @Test fun `isReady returns false before initialize`() { assertFalse(repo.isReady()) }
}
