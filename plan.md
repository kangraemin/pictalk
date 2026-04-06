# Pictalk Android — Clean Architecture + 멀티모듈

## Context
pictalk GitHub 레포에 Android 멀티모듈 프로젝트 생성.
- 아키텍처: Clean Architecture + 멀티모듈
- UI: Circuit (Slack) — Screen/Presenter/Composable
- DI: Hilt
- 모델: Gemma 4 E4B, 앱 첫 실행 시 자동 다운로드

---

## 모듈 구조

```
pictalk/
├── app/                    ← Android application (:app)
├── domain/                 ← Pure Kotlin library (:domain)
├── data/                   ← Android library (:data)
└── feature/
    └── main/               ← Android library (:feature:main)
```

### 의존성 방향
```
:app  ──→  :feature:main  ──→  :domain
 └──────→  :data          ──→  :domain
```

`:domain`은 Android 의존성 없음 (순수 Kotlin).  
`:data`는 `:domain` 인터페이스를 구현.  
`:feature:main`은 `:domain` 인터페이스만 참조 (`:data` 모름).  
`:app`은 모든 모듈을 조립 (Hilt Component, Circuit, Navigation).

---

## Gradle 설정

### `settings.gradle.kts`
```kotlin
pluginManagement {
    repositories {
        google { content {
            includeGroupByRegex("com\\.android.*")
            includeGroupByRegex("com\\.google.*")
            includeGroupByRegex("androidx.*")
        }}
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "Pictalk"
include(":app")
include(":domain")
include(":data")
include(":feature:main")
```

### `build.gradle.kts` (root)
```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}
```

### `gradle/libs.versions.toml`
```toml
[versions]
agp = "8.7.3"
kotlin = "2.0.21"
ksp = "2.0.21-1.0.28"
hilt = "2.51.1"
coreKtx = "1.15.0"
lifecycleRuntimeKtx = "2.8.7"
activityCompose = "1.9.3"
composeBom = "2024.12.01"
circuit = "0.25.0"
mediapipe = "0.10.22"
coil = "2.7.0"
coroutines = "1.9.0"
okhttp = "4.12.0"
junit = "4.13.2"
junitExt = "1.2.1"
espresso = "3.6.1"
mockk = "1.13.12"
coroutinesTest = "1.9.0"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }
androidx-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4" }
androidx-material3 = { group = "androidx.compose.material3", name = "material3" }
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
circuit-foundation = { group = "com.slack.circuit", name = "circuit-foundation", version.ref = "circuit" }
circuit-codegen = { group = "com.slack.circuit", name = "circuit-codegen", version.ref = "circuit" }
circuit-codegen-annotations = { group = "com.slack.circuit", name = "circuit-codegen-annotations", version.ref = "circuit" }
mediapipe-tasks-genai = { group = "com.google.mediapipe", name = "tasks-genai", version.ref = "mediapipe" }
coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
kotlinx-coroutines-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version.ref = "coroutines" }
okhttp = { group = "com.squareup.okhttp3", name = "okhttp", version.ref = "okhttp" }
junit = { group = "junit", name = "junit", version.ref = "junit" }
junit-ext = { group = "androidx.test.ext", name = "junit", version.ref = "junitExt" }
espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espresso" }
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutinesTest" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlin-parcelize = { id = "org.jetbrains.kotlin.plugin.parcelize", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
```

---

## :domain 모듈

### `domain/build.gradle.kts`
```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
}
```

### `domain/src/main/java/com/kangraemin/pictalk/domain/model/AacLabel.kt`
```kotlin
package com.kangraemin.pictalk.domain.model

data class AacLabel(val text: String)
```

### `domain/src/main/java/com/kangraemin/pictalk/domain/model/DownloadState.kt`
```kotlin
package com.kangraemin.pictalk.domain.model

sealed interface DownloadState {
    data object Idle : DownloadState
    data class Downloading(val progressPercent: Int) : DownloadState
    data object Complete : DownloadState
    data class Error(val message: String) : DownloadState
}
```

### `domain/src/main/java/com/kangraemin/pictalk/domain/repository/GemmaRepository.kt`
```kotlin
package com.kangraemin.pictalk.domain.repository

import com.kangraemin.pictalk.domain.model.AacLabel

interface GemmaRepository {
    suspend fun initialize(modelPath: String)
    suspend fun suggestLabels(imageDescription: String): List<AacLabel>
    fun isReady(): Boolean
}
```

### `domain/src/main/java/com/kangraemin/pictalk/domain/repository/ModelRepository.kt`
```kotlin
package com.kangraemin.pictalk.domain.repository

import com.kangraemin.pictalk.domain.model.DownloadState
import kotlinx.coroutines.flow.Flow

interface ModelRepository {
    fun isModelReady(): Boolean
    fun modelPath(): String
    fun downloadModel(): Flow<DownloadState>
}
```

---

## :data 모듈

### `data/build.gradle.kts`
```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.kangraemin.pictalk.data"
    compileSdk = 35
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation(project(":domain"))
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.mediapipe.tasks.genai)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.okhttp)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
}
```

### `data/src/main/java/com/kangraemin/pictalk/data/repository/GemmaRepositoryImpl.kt`
```kotlin
package com.kangraemin.pictalk.data.repository

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import com.kangraemin.pictalk.domain.model.AacLabel
import com.kangraemin.pictalk.domain.repository.GemmaRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GemmaRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : GemmaRepository {

    private var llmInference: LlmInference? = null

    override fun isReady(): Boolean = llmInference != null

    override suspend fun initialize(modelPath: String) = withContext(Dispatchers.IO) {
        val options = LlmInference.LlmInferenceOptions.builder()
            .setModelPath(modelPath)
            .setMaxTokens(512)
            .build()
        llmInference = LlmInference.createFromOptions(context, options)
    }

    override suspend fun suggestLabels(imageDescription: String): List<AacLabel> = withContext(Dispatchers.IO) {
        val inference = llmInference ?: error("GemmaRepository not initialized")
        val session = LlmInferenceSession.createFromLlmInference(inference)
        val raw = session.generateResponse(buildPrompt(imageDescription))
        session.close()
        parseLabels(raw)
    }

    internal fun buildPrompt(description: String): String = """
        <start_of_turn>user
        You are an AAC assistant for children with autism.
        Given a scene description, output 4-6 simple Korean AAC card labels.
        Labels should be words or short phrases a child might want to communicate.
        Respond ONLY with a comma-separated list. No explanation.

        Scene: $description
        <end_of_turn>
        <start_of_turn>model
    """.trimIndent()

    internal fun parseLabels(raw: String): List<AacLabel> =
        raw.split(",")
            .map { it.trim().trimEnd('.') }
            .filter { it.isNotEmpty() && it.length <= 10 }
            .take(6)
            .map { AacLabel(it) }
}
```

### `data/src/main/java/com/kangraemin/pictalk/data/repository/ModelRepositoryImpl.kt`
```kotlin
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
        const val MODEL_FILENAME = "gemma4-e4b-it-int4.bin"
        // TODO: 실제 모델 호스팅 URL로 교체
        const val MODEL_DOWNLOAD_URL = "https://YOUR_MODEL_HOST/gemma4-e4b-it-int4.bin"
    }
}
```

### `data/src/main/java/com/kangraemin/pictalk/data/di/DataModule.kt`
```kotlin
package com.kangraemin.pictalk.data.di

import com.kangraemin.pictalk.data.repository.GemmaRepositoryImpl
import com.kangraemin.pictalk.data.repository.ModelRepositoryImpl
import com.kangraemin.pictalk.domain.repository.GemmaRepository
import com.kangraemin.pictalk.domain.repository.ModelRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds @Singleton
    abstract fun bindGemmaRepository(impl: GemmaRepositoryImpl): GemmaRepository

    @Binds @Singleton
    abstract fun bindModelRepository(impl: ModelRepositoryImpl): ModelRepository

    companion object {
        @Provides @Singleton
        fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.MINUTES)
            .build()
    }
}
```

---

## :feature:main 모듈

### `feature/main/build.gradle.kts`
```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.kangraemin.pictalk.feature.main"
    compileSdk = 35
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
}

ksp { arg("circuit.codegen.mode", "hilt") }

dependencies {
    implementation(project(":domain"))
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.circuit.foundation)
    ksp(libs.circuit.codegen)
    implementation(libs.circuit.codegen.annotations)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.coil.compose)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
    debugImplementation(libs.androidx.ui.tooling)
}
```

### `feature/main/src/main/java/com/kangraemin/pictalk/feature/main/MainScreen.kt`
```kotlin
package com.kangraemin.pictalk.feature.main

import android.net.Uri
import android.os.Parcelable
import com.kangraemin.pictalk.domain.model.AacLabel
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import kotlinx.parcelize.Parcelize

@Parcelize
data object MainScreen : Screen, Parcelable {

    sealed interface Status {
        data object CheckingModel : Status
        data class Downloading(val progressPercent: Int) : Status
        data object InitializingModel : Status
        data class Ready(val message: String) : Status
        data class Error(val message: String) : Status
    }

    data class State(
        val status: Status = Status.CheckingModel,
        val imageUri: Uri? = null,
        val labels: List<AacLabel> = emptyList(),
        val isInferring: Boolean = false,
        val eventSink: (Event) -> Unit = {},
    ) : CircuitUiState

    sealed interface Event : CircuitUiEvent {
        data object OnRetryDownload : Event
        data class OnImageSelected(val uri: Uri) : Event
        data class OnCardClicked(val label: AacLabel) : Event
    }
}
```

### `feature/main/src/main/java/com/kangraemin/pictalk/feature/main/MainPresenter.kt`
```kotlin
package com.kangraemin.pictalk.feature.main

import android.net.Uri
import androidx.compose.runtime.*
import com.kangraemin.pictalk.domain.model.AacLabel
import com.kangraemin.pictalk.domain.model.DownloadState
import com.kangraemin.pictalk.domain.repository.GemmaRepository
import com.kangraemin.pictalk.domain.repository.ModelRepository
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch

class MainPresenter @AssistedInject constructor(
    private val modelRepository: ModelRepository,
    private val gemmaRepository: GemmaRepository,
) : Presenter<MainScreen.State> {

    @CircuitInject(MainScreen::class, SingletonComponent::class)
    @AssistedFactory
    fun interface Factory {
        fun create(): MainPresenter
    }

    @Composable
    override fun present(): MainScreen.State {
        val scope = rememberCoroutineScope()
        var status by remember { mutableStateOf<MainScreen.Status>(MainScreen.Status.CheckingModel) }
        var imageUri by remember { mutableStateOf<Uri?>(null) }
        var labels by remember { mutableStateOf<List<AacLabel>>(emptyList()) }
        var isInferring by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) { prepareModel { status = it } }

        return MainScreen.State(
            status = status,
            imageUri = imageUri,
            labels = labels,
            isInferring = isInferring,
        ) { event ->
            when (event) {
                is MainScreen.Event.OnRetryDownload -> scope.launch { prepareModel { status = it } }
                is MainScreen.Event.OnImageSelected -> {
                    imageUri = event.uri
                    labels = emptyList()
                    isInferring = true
                    scope.launch {
                        // TODO: 실기기 멀티모달 API 확인 후 bitmap→description 교체
                        runCatching { gemmaRepository.suggestLabels("a scene from the selected photo") }
                            .onSuccess { labels = it }
                            .onFailure { status = MainScreen.Status.Error("추론 실패: ${it.message}") }
                        isInferring = false
                    }
                }
                is MainScreen.Event.OnCardClicked -> Unit // TTS는 Composable에서 처리
            }
        }
    }

    private suspend fun prepareModel(onStatus: (MainScreen.Status) -> Unit) {
        modelRepository.downloadModel().collect { state ->
            when (state) {
                is DownloadState.Idle -> onStatus(MainScreen.Status.CheckingModel)
                is DownloadState.Downloading -> onStatus(MainScreen.Status.Downloading(state.progressPercent))
                is DownloadState.Complete -> {
                    onStatus(MainScreen.Status.InitializingModel)
                    runCatching { gemmaRepository.initialize(modelRepository.modelPath()) }
                        .onSuccess { onStatus(MainScreen.Status.Ready("준비 완료. 사진을 선택하세요.")) }
                        .onFailure { onStatus(MainScreen.Status.Error("모델 초기화 실패: ${it.message}")) }
                }
                is DownloadState.Error -> onStatus(MainScreen.Status.Error(state.message))
            }
        }
    }
}
```

### `feature/main/src/main/java/com/kangraemin/pictalk/feature/main/Main.kt`
```kotlin
package com.kangraemin.pictalk.feature.main

import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.components.SingletonComponent
import java.util.Locale

@CircuitInject(MainScreen::class, SingletonComponent::class)
@Composable
fun Main(state: MainScreen.State, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }

    LaunchedEffect(Unit) { tts = TextToSpeech(context) { }; tts?.language = Locale.KOREAN }
    DisposableEffect(Unit) { onDispose { tts?.shutdown() } }

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { state.eventSink(MainScreen.Event.OnImageSelected(it)) }
    }

    Scaffold(modifier = modifier) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Pictalk", style = MaterialTheme.typography.headlineLarge)

            when (val s = state.status) {
                is MainScreen.Status.CheckingModel, MainScreen.Status.InitializingModel -> {
                    CircularProgressIndicator()
                    Text(
                        if (s is MainScreen.Status.InitializingModel) "모델 초기화 중..." else "확인 중...",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                is MainScreen.Status.Downloading -> {
                    Text("모델 다운로드 중 ${s.progressPercent}%", style = MaterialTheme.typography.bodyMedium)
                    LinearProgressIndicator(
                        progress = { s.progressPercent / 100f },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text("Gemma 4 E4B (~2.4GB) · Wi-Fi 권장",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                is MainScreen.Status.Ready -> {
                    Text(s.message, style = MaterialTheme.typography.bodyMedium)
                    state.imageUri?.let {
                        AsyncImage(model = it, contentDescription = null,
                            modifier = Modifier.fillMaxWidth().height(180.dp))
                    }
                    Button(
                        onClick = { pickImage.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isInferring,
                    ) { Text("사진 선택") }
                    if (state.isInferring) CircularProgressIndicator()
                    if (state.labels.isNotEmpty()) {
                        Text("AAC 카드", style = MaterialTheme.typography.titleMedium)
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(state.labels, key = { it.text }) { label ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().aspectRatio(1f).clickable {
                                        tts?.speak(label.text, TextToSpeech.QUEUE_FLUSH, null, label.text)
                                        state.eventSink(MainScreen.Event.OnCardClicked(label))
                                    },
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Text(label.text, style = MaterialTheme.typography.titleMedium)
                                    }
                                }
                            }
                        }
                    }
                }
                is MainScreen.Status.Error -> {
                    Text(s.message, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { state.eventSink(MainScreen.Event.OnRetryDownload) }) {
                        Text("다시 시도")
                    }
                }
            }
        }
    }
}
```

---

## :app 모듈

### `app/build.gradle.kts`
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.kangraemin.pictalk"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.kangraemin.pictalk"
        minSdk = 26; targetSdk = 35; versionCode = 1; versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
    androidResources { noCompress += listOf("bin", "task") }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":feature:main"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.circuit.foundation)
    androidTestImplementation(libs.junit.ext)
    androidTestImplementation(libs.espresso.core)
}
```

### `app/src/main/java/com/kangraemin/pictalk/app/PictalkApplication.kt`
```kotlin
package com.kangraemin.pictalk.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PictalkApplication : Application()
```

### `app/src/main/java/com/kangraemin/pictalk/di/CircuitModule.kt`
```kotlin
package com.kangraemin.pictalk.di

import com.slack.circuit.foundation.Circuit
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CircuitModule {
    @Multibinds abstract fun presenterFactories(): Set<Presenter.Factory>
    @Multibinds abstract fun uiFactories(): Set<Ui.Factory>

    companion object {
        @Provides @Singleton
        fun provideCircuit(
            presenterFactories: @JvmSuppressWildcards Set<Presenter.Factory>,
            uiFactories: @JvmSuppressWildcards Set<Ui.Factory>,
        ): Circuit = Circuit.Builder()
            .addPresenterFactories(presenterFactories)
            .addUiFactories(uiFactories)
            .build()
    }
}
```

### `app/src/main/java/com/kangraemin/pictalk/MainActivity.kt`
```kotlin
package com.kangraemin.pictalk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kangraemin.pictalk.feature.main.MainScreen
import com.kangraemin.pictalk.ui.theme.PictalkTheme
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var circuit: Circuit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PictalkTheme {
                val backStack = rememberSaveableBackStack(root = MainScreen)
                val navigator = rememberCircuitNavigator(backStack)
                CircuitCompositionLocals(circuit) {
                    NavigableCircuitContent(navigator = navigator, backStack = backStack)
                }
            }
        }
    }
}
```

### `app/src/main/java/com/kangraemin/pictalk/ui/theme/Theme.kt`
```kotlin
package com.kangraemin.pictalk.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val AppTypography = Typography(
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
)

@Composable
fun PictalkTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = lightColorScheme(), typography = AppTypography, content = content)
}
```

### `app/src/main/AndroidManifest.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.CAMERA" />
    <application
        android:name=".app.PictalkApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.Pictalk">
        <activity android:name=".MainActivity" android:exported="true" android:theme="@style/Theme.Pictalk">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

### `app/src/main/res/values/strings.xml`
```xml
<resources><string name="app_name">Pictalk</string></resources>
```

### `app/src/main/res/values/themes.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.Pictalk" parent="android:Theme.Material.Light.NoActionBar" />
</resources>
```

### `app/proguard-rules.pro`
```
-keep class com.google.mediapipe.** { *; }
-keep class com.slack.circuit.** { *; }
```

### `.gitignore`
```
*.iml
.gradle
/local.properties
/.idea
.DS_Store
/build
/captures
.externalNativeBuild
.cxx
local.properties
```

---

## 테스트

### `:data` — `GemmaRepositoryImplTest.kt`
```kotlin
package com.kangraemin.pictalk.data.repository

import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before; import org.junit.Test

class GemmaRepositoryImplTest {
    private lateinit var repo: GemmaRepositoryImpl
    @Before fun setUp() { repo = GemmaRepositoryImpl(mockk(relaxed = true)) }

    @Test fun `buildPrompt includes scene description`() { assertTrue(repo.buildPrompt("child eating apple").contains("child eating apple")) }
    @Test fun `buildPrompt includes Korean and AAC keywords`() { assertTrue(repo.buildPrompt("test").run { contains("Korean") && contains("AAC") }) }
    @Test fun `parseLabels splits comma-separated string`() { assertEquals(listOf("먹어요","사과","배고파요","주세요"), repo.parseLabels("먹어요, 사과, 배고파요, 주세요").map { it.text }) }
    @Test fun `parseLabels trims whitespace and trailing dots`() { assertEquals(listOf("먹어요","사과"), repo.parseLabels("  먹어요 ,  사과.  ").map { it.text }) }
    @Test fun `parseLabels filters empty tokens`() { assertEquals(listOf("먹어요"), repo.parseLabels(",, 먹어요,,").map { it.text }) }
    @Test fun `parseLabels takes at most 6 labels`() { assertEquals(6, repo.parseLabels("a,b,c,d,e,f,g,h").size) }
    @Test fun `parseLabels filters labels longer than 10 chars`() { assertFalse(repo.parseLabels("짧은거, 매우매우매우길어서필터됨, 또짧은").any { it.text.length > 10 }) }
    @Test fun `isReady returns false before initialize`() { assertFalse(repo.isReady()) }
}
```

### `:data` — `ModelRepositoryImplTest.kt`
```kotlin
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
```

### `:feature:main` — `MainScreenStateTest.kt`
```kotlin
package com.kangraemin.pictalk.feature.main

import android.net.Uri
import com.kangraemin.pictalk.domain.model.AacLabel
import org.junit.Assert.*; import org.junit.Test

class MainScreenStateTest {
    @Test fun `default State has CheckingModel status`() { assertTrue(MainScreen.State().status is MainScreen.Status.CheckingModel) }
    @Test fun `Downloading status carries progressPercent`() { assertEquals(42, (MainScreen.Status.Downloading(42)).progressPercent) }
    @Test fun `State with labels is not empty`() { assertEquals(2, MainScreen.State(labels = listOf(AacLabel("먹어요"), AacLabel("사과"))).labels.size) }
    @Test fun `OnImageSelected event carries uri`() { val uri = Uri.parse("content://test"); assertEquals(uri, (MainScreen.Event.OnImageSelected(uri)).uri) }
}
```

---

## 검증
```bash
./gradlew assembleDebug      # 전체 빌드
./gradlew :domain:test       # 도메인 단위 테스트
./gradlew :data:test         # 데이터 단위 테스트 (11개)
./gradlew :feature:main:test # 피처 단위 테스트 (4개)
./gradlew installDebug       # 실기기 설치
```
