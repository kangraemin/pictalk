## TC

| TC | 검증 항목 | 기대 결과 | 상태 |
|----|----------|----------|------|
| TC-01 | feature/main/build.gradle.kts에 circuit + compose + hilt | 의존성 존재 | ✅ |
| TC-02 | MainScreen sealed interface 존재 | Status/State/Event 정의 | ✅ |
| TC-03 | MainPresenter @AssistedInject + @CircuitInject | Factory 인터페이스 정의 | ✅ |
| TC-04 | Main.kt @CircuitInject Composable | Screen 연결 | ✅ |
| TC-05 | MainScreenStateTest 4개 테스트 | `./gradlew :feature:main:testDebugUnitTest` → 4개 통과 | ✅ |

## 실행출력

TC-01: `grep "circuit\|compose\|hilt" feature/main/build.gradle.kts`
→ circuit.foundation, circuit.codegen, hilt.android, compose.bom 등 확인

TC-02~04: 소스 파일 직접 확인
→ MainScreen(Status/State/Event), MainPresenter(Factory), Main(@CircuitInject) 존재 확인

TC-05: `./gradlew :feature:main:testDebugUnitTest`
→ BUILD SUCCESSFUL — MainScreenStateTest: 4개 통과
