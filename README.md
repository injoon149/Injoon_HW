🧾 아이피아 백엔드 과제 수행 결과

Spring Boot 기반으로 회원(Member) – 주문(Order) – 결제(Payment) 도메인을 설계하고, REST API 형태로 구현한 프로젝트입니다.
객체 지향적 설계, 서비스 계층 중심의 비즈니스 로직, 그리고 JUnit 테스트를 통한 검증에 중점을 두었습니다.

🚀 프로젝트 개요
항목	내용
프로젝트명	아이피아 백엔드 과제
개발언어 / 환경	Java 17, Spring Boot 3.x, Gradle
데이터베이스	H2 (In-Memory)
ORM	Spring Data JPA
빌드 도구	Gradle
테스트	JUnit5, Mockito, MockMvc
목표	도메인 모델링 및 REST API 구현, 단위/통합 테스트 수행
🧩 주요 구현 내용
🧍‍♂️ Member (회원)

회원 가입 (POST /api/members)

회원 조회 (GET /api/members/{id})

📦 Order (주문)

주문 생성 (POST /api/orders)

주문 조회 (GET /api/orders/{id})

💳 Payment (결제)

결제 요청 (POST /api/payments)

결제 승인 (POST /api/payments/{id}/approve)

결제 조회 (GET /api/payments/{id})

⚙️ ERD (Entity Relationship Diagram)
Member (1) —— (N) Order (1) —— (1) Payment


Member

주문과 1:N 관계

Order

결제와 1:1 관계

Payment

주문을 FK로 참조 (Unique)

🧱 계층 구조 (Layered Architecture)
com.example.demo
 ├── member
 │   ├── api
 │   ├── domain
 │   ├── repository
 │   └── service
 ├── order
 │   ├── api
 │   ├── domain
 │   ├── repository
 │   └── service
 ├── payment
 │   ├── api
 │   ├── domain
 │   ├── repository
 │   └── service
 └── common
     └── GlobalExceptionHandler.java


domain: 엔티티 및 연관관계 정의

service: 트랜잭션 단위의 비즈니스 로직 구현

api: REST API (요청/응답 DTO 처리)

repository: Spring Data JPA 기반 데이터 접근 계층

🧪 테스트 (TDD 기반 검증)
✅ 단위 테스트 (Unit Test)

대상: Service Layer

도구: JUnit5 + Mockito

내용:

MemberServiceTest

OrderServiceTest

PaymentServiceTest

각 서비스의 비즈니스 로직 및 예외 처리 검증

✅ 통합 테스트 (Integration Test)

대상: Controller Layer

도구: Spring MockMvc

내용:

MemberControllerIntegrationTest

OrderControllerIntegrationTest

PaymentControllerIntegrationTest

실제 요청/응답 흐름 검증 (H2 DB 연동)

⚡ 전역 예외 처리

@RestControllerAdvice 사용

예외에 따라 적절한 HTTP 상태 코드 반환

IllegalArgumentException → 400 또는 404

그 외 서버 오류 → 500

📘 예시 요청
회원 등록
POST /api/members
{
  "name": "홍길동",
  "email": "hong@test.com"
}

주문 생성
POST /api/orders
{
  "memberId": 1,
  "amount": 50000
}

결제 요청
POST /api/payments
{
  "orderId": 1,
  "amount": 50000,
  "method": "CARD"
}

결제 승인
POST /api/payments/1/approve

🧰 실행 방법
# 1. 프로젝트 클론
git clone https://github.com/<your-username>/<repo-name>.git
cd <repo-name>

# 2. 빌드 및 실행
./gradlew clean build
java -jar build/libs/demo-0.0.1-SNAPSHOT.jar

# 3. H2 콘솔 접속
http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:testdb

🧠 설계 포인트

도메인 중심 설계 (DDD-lite)

엔티티에서 연관관계를 명확히 정의하고, 생성 책임을 create() / request() 팩토리 메서드로 분리.

Clean Code 원칙 준수

단일 책임, 의미 있는 네이밍, 불변 객체 중심 구조.

TDD 기반 개발 프로세스

기능 단위별 테스트 선행 → 서비스 구현 → 컨트롤러 통합 테스트로 검증.

전역 예외 처리 및 명확한 상태 코드 반환

사용자 입력 오류(400), 리소스 미존재(404), 시스템 오류(500) 구분.

📊 프로젝트 구성 요약
항목	구현 여부
도메인 모델 설계 및 연관관계 정의	✅
REST API 구현	✅
H2 DB 설정	✅
단위 테스트 / 통합 테스트	✅
전역 예외 처리	✅
README 정리	✅
🧑‍💻 개발자 코멘트

본 프로젝트는 단순 CRUD를 넘어,
**“객체 간 관계를 중심으로 한 도메인 주도 설계(Domain-Driven Design)”**를 목표로 진행했습니다.
비즈니스 로직은 서비스 계층에서 명확히 분리하였으며, 테스트 주도 개발(TDD)로 기능의 신뢰성을 확보했습니다.
가독성과 유지보수성을 높이기 위해 코드 컨벤션과 계층 구조를 엄격히 구분했습니다.
