# 실시간 경매 플랫폼 API 상세 명세서 

본 문서는 실시간 경매 플랫폼의 클라이언트-서버 간 통신 규격을 정의합니다. 모든 API는 RESTful 원칙을 준수하며, `Authorization` 헤더에 JWT Access Token이 필요합니다.

---

## 1. 공통 규격 (Common Specification)
- **Base URL**: `http://localhost:8080/api/v1`
- **인증 헤더**: `Authorization: Bearer {JWT_ACCESS_TOKEN}`
- **공통 응답 형식**:
  ```json
  {
    "success": true,
    "data": { },
    "message": "작업이 성공적으로 완료되었습니다.",
    "timestamp": "2026-05-07T16:00:00"
  }
  ```

---

## 2. 인증 및 계정 (Auth & User)

### 2.1 회원가입 (Signup)
- **Method/Path**: `POST /auth/signup`
- **Request Body**:
  ```json
  {
    "login_id": "user123",
    "password": "password123!",
    "nickname": "경매왕",
    "email": "user@example.com"
  }
  ```
- **상세**: 비밀번호는 서버 측에서 BCrypt 단방향 암호화 처리 필수.

### 2.2 로그인 (Login)
- **Method/Path**: `POST /auth/login`
- **Request Body**: `{ "login_id": "user123", "password": "password123!" }`
- **Response Data**:
  ```json
  {
    "accessToken": "eyJhbGci...",
    "user": {
      "id": 1,
      "nickname": "경매왕",
      "role": "USER",
      "email": "user@example.com"
    }
  }
  ```

---

## 3. 경매 서비스 (Auction Service)

### 3.1 경매 목록 조회 (페이징 & 필터)
- **Method/Path**: `GET /auctions`
- **Query Params**:
    - `page`: 페이지 번호 (기본 0)
    - `size`: 페이지당 개수 (기본 10)
    - `category`: 카테고리 코드 (WATCH, ART 등)
    - `status`: 경매 상태 (LIVE, FINISHED 등)
- **Response Data**: `content` 배열 내에 `id, title, current_price, status, main_picture_url` 포함.

### 3.2 경매 상세 조회
- **Method/Path**: `GET /auctions/{id}`
- **Response Data**:
  ```json
  {
    "id": 10,
    "title": "빈티지 카메라",
    "description": "1950년대 제품입니다.",
    "seller": { "id": 1, "nickname": "골동품수집가" },
    "category": "WATCH",
    "start_price": 50000,
    "current_price": 55000,
    "status": "LIVE",
    "start_time": "2026-05-07T18:00:00",
    "end_time": "2026-05-10T18:00:00",
    "view_count": 124,
    "like_count": 52,
    "pictures": [ { "url": "...", "main": true } ]
  }
  ```

### 3.3 경매 물품 등록
- **Method/Path**: `POST /auctions`
- **Request Body**:
  ```json
  {
    "title": "제목",
    "description": "설명",
    "category": "WATCH",
    "start_price": 10000,
    "end_time": "2026-05-10T18:00:00",
    "pictures": [ { "url": "s3_url", "main": true } ]
  }
  ```

---

## 4. 입찰 및 워크플로우 (Bidding & Trade) - [팀장님 담당 핵심]

### 4.1 입찰하기 (Bid) - **동시성 제어 필수**
- **Method/Path**: `POST /auctions/{id}/bids`
- **Request Body**: `{ "price": 60000 }`
- **핵심 로직**:
    1. **검증**: `입찰가 > current_price` 인지 확인.
    2. **검증**: `seller_id != login_user_id` (본인 상품 입찰 불가).
    3. **검증**: 경매 상태가 `LIVE`인지 확인.
    4. **처리**: `Optimistic Lock(version)`을 사용하여 `auctions` 테이블의 `current_price` 업데이트 및 `bids` 이력 추가.

### 4.2 거래 단계별 상태 변경
- **낙찰자 결제**: `POST /payments` -> Request: `{ "auction_id" }` (상태 PAID 변경)
- **판매자 배송**: `PATCH /auctions/{id}/shipping` (상태 SHIPPING 변경)
- **낙찰자 수령확인**: `PATCH /auctions/{id}/complete` (상태 COMPLETED 변경)

---

## 5. 마이페이지 및 알림 (MyPage & Notification)

### 5.1 마이페이지 요약 정보
- **Method/Path**: `GET /api/v1/users/me/summary`
- **Response**: `{ "bidding_count", "won_count", "hosted_count", "watchlist_count" }`

### 5.2 알림 목록 및 읽음 처리
- **조회**: `GET /api/v1/notifications` (타입: OUTBID, CLOSING_SOON 등)
- **읽음**: `PATCH /api/v1/notifications/{id}`

---

## 6. 에러 코드 상세 정의 (Exception Handler용)

| 에러 코드 | HTTP | 설명 |
| :--- | :--- | :--- |
| `INVALID_INPUT_VALUE` | 400 | 입력값 검증 실패 (Bean Validation) |
| `INVALID_BID_PRICE` | 400 | 현재 최고가보다 낮은 입찰 시도 |
| `SELF_BID_NOT_ALLOWED` | 400 | 자신의 경매 물품에 입찰 시도 |
| `UNAUTHORIZED` | 401 | 유효하지 않은 JWT 토큰 |
| `ACCESS_DENIED` | 403 | 권한 부족 (예: 판매자가 아닌데 배송 처리) |
| `RESOURCE_NOT_FOUND` | 404 | 경매물품 혹은 사용자를 찾을 수 없음 |
| `BID_CONFLICT` | 409 | **동시 입찰 충돌** (낙관적 락 버전 불일치) |
| `ALREADY_PROCESSED` | 409 | 이미 결제/배송/수령이 완료된 상태 |