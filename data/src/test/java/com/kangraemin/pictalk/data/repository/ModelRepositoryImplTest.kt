package com.kangraemin.pictalk.data.repository

import android.content.Context
import io.mockk.every; import io.mockk.mockk
import okhttp3.OkHttpClient
import org.junit.Assert.*
import org.junit.Before; import org.junit.Test
import java.io.File

class ModelRepositoryImplTest {
    private lateinit var repo: ModelRepositoryImpl
    private lateinit var tempDir: File

    @Before fun setUp() {
        tempDir = createTempDir()
        repo = ModelRepositoryImpl(mockk<Context> { every { filesDir } returns tempDir }, OkHttpClient())
    }

    @Test fun `isModelReady returns false when file does not exist`() { assertFalse(repo.isModelReady()) }
    @Test fun `isModelReady returns true when model file exists`() { File(tempDir, "gemma4-e4b-it-int4.bin").writeText("fake"); assertTrue(repo.isModelReady()) }
    @Test fun `modelPath points to filesDir`() { assertTrue(repo.modelPath().startsWith(tempDir.absolutePath) && repo.modelPath().endsWith("gemma4-e4b-it-int4.bin")) }
}
