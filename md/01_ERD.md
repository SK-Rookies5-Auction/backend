# 📊 데이터 모델 정의 (ERD 상세)

본 문서는 실시간 경매 플랫폼의 데이터베이스 구조를 정의하며, AI가 JPA 엔티티 및 DDL을 정확히 생성할 수 있도록 상세 제약 조건을 포함합니다.

## 1. users (회원)
- **설명**: 사용자 정보 및 권한 관리
- **테이블 정보**:
  | 컬럼명 | 타입 | 제약 조건 | 설명 |
  | :--- | :--- | :--- | :--- |
  | `id` | BIGINT | PK, AutoIncrement | 회원 고유 번호 |
  | `nickname` | VARCHAR(50) | UNIQUE, NOT NULL | 서비스 활동 닉네임 |
  | `login_id` | VARCHAR(50) | UNIQUE, NOT NULL | 로그인용 아이디 |
  | `password` | VARCHAR(255) | NOT NULL | BCrypt 암호화된 비밀번호 |
  | `email` | VARCHAR(50) | UNIQUE, NOT NULL | 이메일 주소 |
  | `role` | ENUM | NOT NULL | 권한 (ROLE_USER, ROLE_ADMIN) |
  | `created_at` | TIMESTAMP | NOT NULL | 가입 일시 |

## 2. auctions (경매 물품)
- **설명**: 경매 등록 물품 및 진행 상태 관리
- **테이블 정보**:
  | 컬럼명 | 타입 | 제약 조건 | 설명 |
  | :--- | :--- | :--- | :--- |
  | `id` | BIGINT | PK, AutoIncrement | 경매 고유 번호 |
  | `seller_id` | BIGINT | FK (users.id), NN | 판매자 고유 번호 |
  | `winner_id` | BIGINT | FK (users.id) | 최종 낙찰자 고유 번호 (유찰 시 Null) |
  | `title` | VARCHAR(100) | NOT NULL | 경매 제목 |
  | `description` | TEXT | NOT NULL | 물품 상세 설명 |
  | `category` | ENUM | NOT NULL | WATCH, ART, ELECTRONIC 등 |
  | `start_price` | BIGINT | NOT NULL | 경매 시작가 |
  | `current_price` | BIGINT | NOT NULL | 현재 최고 입찰가 (최초 시작가와 동일) |
  | `status` | ENUM | NOT NULL | READY, LIVE, FINISHED, CANCEL |
  | `start_time` | DATETIME | NOT NULL | 경매 시작 일시 |
  | `end_time` | DATETIME | NOT NULL | 경매 종료 일시 |
  | `view_count` | INT | Default 0 | 조회수 |
  | `like_count` | INT | Default 0 | 찜 횟수 |
  | `version` | BIGINT | NOT NULL | **낙관적 락(Optimistic Lock)용 버전** |
  | `created_at` | TIMESTAMP | NOT NULL | 등록 일시 |

## 3. bids (입찰 기록)
- **설명**: 입찰 이력 추적 및 실시간 최고가 검증용
- **테이블 정보**:
  | 컬럼명 | 타입 | 제약 조건 | 설명 |
  | :--- | :--- | :--- | :--- |
  | `id` | BIGINT | PK, AutoIncrement | 입찰 고유 번호 |
  | `auctions_id` | BIGINT | FK (auctions.id), NN | 해당 경매 번호 |
  | `user_id` | BIGINT | FK (users.id), NN | 입찰자 고유 번호 |
  | `price` | BIGINT | NOT NULL | 입찰 시도 가격 |
  | `updated_at` | TIMESTAMP | NOT NULL | 입찰 일시 |

## 4. pictures (경매 이미지)
- **설명**: S3와 연동된 물품 이미지 관리
- **테이블 정보**:
  | 컬럼명 | 타입 | 제약 조건 | 설명 |
  | :--- | :--- | :--- | :--- |
  | `id` | BIGINT | PK, AutoIncrement | 이미지 고유 번호 |
  | `auction_id` | BIGINT | FK (auctions.id), NN | 소속 경매 물품 번호 |
  | `url` | TEXT | NOT NULL | S3 전체 경로 URL |
  | `key` | TEXT | NOT NULL | S3 Object Key (삭제/수정용) |
  | `is_main` | BOOLEAN | NOT NULL | 대표 썸네일 여부 |
  | `sort_order` | INT | NOT NULL | 이미지 출력 순서 |

## 5. payments (결제)
- **설명**: 낙찰 성공 후 결제 상태 추적
- **테이블 정보**:
  | 컬럼명 | 타입 | 제약 조건 | 설명 |
  | :--- | :--- | :--- | :--- |
  | `id` | BIGINT | PK | 결제 고유 번호 |
  | `user_id` | BIGINT | FK (users.id), NN | 결제자 번호 |
  | `auction_id` | BIGINT | FK (auctions.id), NN | 대상 경매 번호 |
  | `final_price` | BIGINT | NOT NULL | 최종 결제 금액 |
  | `status` | ENUM | NOT NULL | PENDING, COMPLETED, FAILED |
  | `paid_at` | TIMESTAMP | | 결제 완료 일시 |

## 6. comments (Q&A)
- **설명**: 판매자와 구매자 간의 문의 게시판
- **테이블 정보**:
  | 컬럼명 | 타입 | 제약 조건 | 설명 |
  | :--- | :--- | :--- | :--- |
  | `id` | BIGINT | PK | 댓글 고유 번호 |
  | `auction_id` | BIGINT | FK (auctions.id), NN | 경매 상품 번호 |
  | `user_id` | BIGINT | FK (users.id), NN | 작성자 번호 |
  | `content` | TEXT | NOT NULL | 댓글 내용 |
  | `created_at` | TIMESTAMP | NOT NULL | 작성 일시 |

---
### 🔗 테이블 간 관계 (Relationships)
1. **User : Auction** = 1 : N (한 유저는 여러 경매를 등록하거나 낙찰받을 수 있음)
2. **Auction : Bid** = 1 : N (한 경매에 여러 입찰 기록이 쌓임)
3. **Auction : Picture** = 1 : N (한 경매 상품은 여러 장의 사진을 가짐)
4. **Auction : Comment** = 1 : N (한 경매 상품에 여러 문의 댓글이 달림)
5. **Auction : Payment** = 1 : 1 (낙찰된 한 경매는 하나의 결제 정보를 생성함)