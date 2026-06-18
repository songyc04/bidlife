# BIDLIFE 프로젝트 메모리

## 프로젝트 정보
- **프로젝트명**: BIDLIFE (온라인 경매 플랫폼)
- **기술 스택**: Spring Boot 4.0.6, MySQL, Thymeleaf
- **포트**: 7001

## 개발 규칙

### HTML 파일 작성 규칙
- 모든 HTML 파일의 footer 섹션은 다음 텍스트로 통일:
  ```html
  <p>&copy; 2026 BIDLIFE. All rights reserved. 2026-1 Spring boot Final Project.</p>
  ```
- 새로 작성하는 모든 HTML 파일에도 동일한 footer 텍스트 적용

### 주요 기능
- 이메일 인증 (gmail.com, naver.com, test.com)
- 실시간 경매 시스템
- 입찰 및 즉시 구매 기능
- 찜하기 기능
- 알림 시스템
- 판매자 프로필 페이지

### 파일 구조
- Entity: 데이터베이스 엔티티
- Repository: JPA Repository
- Service: 비즈니스 로직
- Controller: 요청 처리
- templates: Thymeleaf HTML 템플릿
- static/css: 스타일시트
- static/js: JavaScript 파일
