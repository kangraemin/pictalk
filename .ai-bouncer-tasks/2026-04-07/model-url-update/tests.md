| TC | 검증 항목 | 기대 결과 | 상태 |
|----|----------|----------|------|
| TC-01 | MODEL_FILENAME 값 | `gemma-4-E4B-it.litertlm` | ✅ |
| TC-02 | MODEL_DOWNLOAD_URL 값 | `https://huggingface.co/litert-community/...` HuggingFace URL | ✅ |
| TC-03 | :data:compileDebugKotlin | BUILD SUCCESSFUL | ✅ |
| TC-04 | :data:testDebugUnitTest | 11개 통과 | ✅ |

## 실행출력

TC-01/02: `grep "MODEL_FILENAME\|MODEL_DOWNLOAD_URL" data/src/main/java/.../ModelRepositoryImpl.kt`
→ gemma-4-E4B-it.litertlm, huggingface.co/litert-community/... 확인

TC-03/04: `./gradlew :data:testDebugUnitTest`
→ BUILD SUCCESSFUL — 11개 통과
※ ModelRepositoryImplTest 파일명 하드코딩을 MODEL_FILENAME 상수 참조로 함께 수정
