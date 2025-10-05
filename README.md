# 🧾 아이피아 백엔드 과제 수행 결과

Spring Boot 기반으로 **회원(Member) – 주문(Order) – 결제(Payment)** 도메인을 설계하고 REST API 형태로 구현한 프로젝트입니다.  
객체 지향적 모델링, 서비스 계층 중심의 비즈니스 로직, 그리고 JUnit 테스트를 통한 검증에 중점을 두었습니다.

---

## 🚀 프로젝트 개요

| 항목 | 내용 |
|------|------|
| **프로젝트명** | 아이피아 백엔드 과제 |
| **개발언어 / 환경** | Java 17, Spring Boot 3.x, Gradle |
| **데이터베이스** | H2 (In-Memory) |
| **ORM** | Spring Data JPA |
| **빌드 도구** | Gradle |
| **테스트** | JUnit5, Mockito, Spring MockMvc |
| **목표** | 도메인 모델링 및 REST API 구현, 단위/통합 테스트 수행 |

---

## 🧩 주요 구현 기능

### 🧍‍♂️ Member (회원)
- 회원 가입 → `POST /api/members`
- 회원 조회 → `GET /api/members/{id}`

### 📦 Order (주문)
- 주문 생성 → `POST /api/orders`
- 주문 조회 → `GET /api/orders/{id}`

### 💳 Payment (결제)
- 결제 요청 → `POST /api/payments`
- 결제 승인 → `POST /api/payments/{id}/approve`
- 결제 조회 → `GET /api/payments/{id}`

---

## ⚙️ ERD (Entity Relationship Diagram)

```text
Member (1) —— (N) Order (1) —— (1) Payment
<img width="868" height="454" alt="image" src="https://github.com/user-attachments/assets/f6336e00-11ec-4873-8291-1c05596d6797" />


