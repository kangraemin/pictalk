## TC

| TC | 검증 항목 | 기대 결과 | 상태 |
|----|----------|----------|------|
| TC-01 | data/build.gradle.kts에 android.library + hilt + mediapipe | 3개 의존성 모두 존재 | ✅ |
| TC-02 | GemmaRepositoryImpl이 GemmaRepository 구현 | buildPrompt/parseLabels internal 함수 존재 | ✅ |
| TC-03 | ModelRepositoryImpl이 ModelRepository 구현 | isModelReady/modelPath/downloadModel 구현 | ✅ |
| TC-04 | DataModule @Binds 설정 | GemmaRepository/ModelRepository 바인딩 존재 | ✅ |
| TC-05 | GemmaRepositoryImplTest 8개 테스트 | `./gradlew :data:testDebugUnitTest` 실행 → 8개 통과 | ✅ |
| TC-06 | ModelRepositoryImplTest 3개 테스트 | isModelReady/modelPath 검증 3개 통과 | ✅ |

## 실행출력

TC-01: `grep "android.library\|hilt\|mediapipe" data/build.gradle.kts`
→ alias(libs.plugins.android.library), implementation(libs.hilt.android), implementation(libs.mediapipe.tasks.genai) 확인

TC-02: `grep "buildPrompt\|parseLabels" data/src/main/java/.../GemmaRepositoryImpl.kt`
→ internal fun buildPrompt, internal fun parseLabels 존재 확인

TC-03: `grep "isModelReady\|modelPath\|downloadModel" data/src/main/java/.../ModelRepositoryImpl.kt`
→ 3개 함수 override 확인

TC-04: `grep "Binds\|GemmaRepository\|ModelRepository" data/src/main/java/.../DataModule.kt`
→ @Binds bindGemmaRepository, bindModelRepository 확인

TC-05/06: `./gradlew :data:testDebugUnitTest`
→ BUILD SUCCESSFUL — GemmaRepositoryImplTest: 8개, ModelRepositoryImplTest: 3개 통과 (총 11개)
