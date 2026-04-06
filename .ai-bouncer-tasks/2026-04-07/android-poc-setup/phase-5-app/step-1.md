## TC

| TC | 검증 항목 | 기대 결과 | 상태 |
|----|----------|----------|------|
| TC-01 | app/build.gradle.kts에 모든 모듈 의존성 | :domain, :data, :feature:main 포함 | ✅ |
| TC-02 | PictalkApplication @HiltAndroidApp | Hilt 앱 컴포넌트 설정 | ✅ |
| TC-03 | CircuitModule @Multibinds + Circuit @Provides | Circuit DI 설정 | ✅ |
| TC-04 | MainActivity @AndroidEntryPoint | Circuit + Hilt 연결 | ✅ |
| TC-05 | ./gradlew assembleDebug | BUILD SUCCESSFUL | ✅ |

## 실행출력

TC-05: `./gradlew assembleDebug`
→ BUILD SUCCESSFUL in 1m
→ :app:assembleDebug 완료
→ libllm_inference_engine_jni.so 포함 (strip warning은 정상 — native 라이브러리)
