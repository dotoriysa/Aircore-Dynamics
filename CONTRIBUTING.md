# 기여 가이드 (Contribution Guide)

이 프로젝트에 기여해주셔서 감사합니다! 원활한 협업을 위해 몇 가지 규칙을 공유합니다.

## 1. Git 커밋 메시지 컨벤션

우리 프로젝트는 Conventional Commits 명세에 따라 커밋 메시지를 작성합니다.

- **`feat`**: 새로운 기능 추가
- **`fix`**: 버그 수정
- **`refactor`**: 코드 리팩토링 (기능 변경 없음)
- **`style`**: 코드 스타일 변경 (포맷팅, 세미콜론 등)
- **`docs`**: 문서 수정
- **`test`**: 테스트 코드 추가 또는 수정
- **`chore`**: 빌드 관련 파일 수정, 패키지 매니저 설정 변경 등

> 예시: `feat(dashboard): 3D 뷰어 기능 추가`

## 2. 코딩 스타일 (코딩 컨벤션)

- 변수명은 **camelCase**를 사용합니다. (예: `dailyProduction`)
- 클래스명은 **PascalCase**를 사용합니다. (예: `FactoryViewer`)
- (필요하다면 Prettier, ESLint 등 린팅 도구 설정에 대해 명시)

## 3. 브랜치 전략

- 기능 개발: `feature/기능-이름` (예: `feature/login`)
- 버그 수정: `fix/버그-내용` (예: `fix/tab-rendering-bug`)
- 모든 Pull Request는 `main` 브랜치를 향해야 합니다.

## 4. Pull Request (PR) 프로세스

- PR 제목은 커밋 메시지처럼 명확하게 작성합니다.
- 변경 사항에 대한 상세한 설명을 본문에 작성합니다.
- 최소 1명 이상의 동료에게 코드 리뷰를 받아야 합니다.