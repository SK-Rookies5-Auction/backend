# ⚡ 06. 입찰 및 결제 상태 워크플로우 상세 명세 (Bidding & Trade Workflow)

본 문서는 실시간 경매 플랫폼의 핵심인 입찰 로직과 경매 종료 후 결제/배송으로 이어지는 전체 상태 전이(State Transition) 규칙을 정의합니다.

---

## 1. 실시간 입찰 비즈니스 로직 (Bidding Logic)

### 1.1 입찰 요청 프로세스 (POST /api/v1/auctions/{id}/bids)
- **입찰 유효성 검증 순서 (Validation)**:
    1. **경매 상태 확인**: 해당 경매의 `status`가 반드시 `LIVE`여야 합니다. (`READY`, `FINISHED` 상태 시 입찰 불가)
    2. **입찰 금액 확인**: 요청된 `price`가 현재 최고가(`current_price`)보다 반드시 높아야 합니다. (같거나 낮으면 `INVALID_BID_PRICE` 에러)
    3. **본인 입찰 금지**: 판매자(`seller_id`)와 현재 로그인 유저의 ID가 동일하면 입찰을 차단합니다. (`SELF_BID_NOT_ALLOWED` 에러)
- **최고가 갱신 로직**:
    - 검증 통과 시 `auctions` 테이블의 `current_price`를 새 입찰가로 업데이트합니다.
    - 동시에 `bids` 테이블에 새로운 입찰 기록을 생성합니다.
    - 기존 최고가 입찰자에게는 알림 서비스를 통해 '상위 입찰 발생' 메시지를 발송합니다.

---

## 2. 경매 상태 관리 및 자동화 (Auction Status & Scheduler)

### 2.1 경매 상태 정의 (Auction Status)
- **READY**: 경매 등록 직후부터 시작 예약 시간(`start_time`) 전까지의 상태.
- **LIVE**: 경매가 시작되어 입찰이 가능한 활성화 상태.
- **FINISHED**: 마감 시간(`end_time`) 도달 또는 낙찰자가 확정되어 입찰이 종료된 상태.
- **CANCEL**: 판매자 또는 관리자에 의해 경매가 취소된 상태.

### 2.2 자동 마감 스케줄러 (@Scheduled)
- **작동 방식**: 백엔드 서버에서 주기적으로(예: 매 분 0초) 현재 서버 시간과 각 경매의 `end_time`을 비교합니다.
- **상태 전환**: `end_time`이 지난 `LIVE` 상태의 경매를 찾아 `FINISHED`로 변경합니다.
- **낙찰자 확정**: `bids` 테이블에서 가장 높은 금액을 입찰한 유저의 ID를 `auctions` 테이블의 `winner_id` 필드에 기록합니다. (입찰 기록이 없으면 유찰 처리)

---

## 3. 거래 및 결제 워크플로우 (Trade & Payment Flow)

경매 종료(FINISHED) 후, 낙찰자와 판매자 간의 거래 단계는 `payments` 테이블의 상태를 통해 관리됩니다.

### 3.1 거래 상태 전이 단계
1. **PENDING (결제 대기)**: 경매 종료 직후 낙찰자가 확정되었을 때의 기본 상태.
2. **PAID (결제 완료)**: 낙찰자가 결제 API(`POST /payments`)를 호출하여 성공했을 때의 상태.
3. **SHIPPING (배송 중)**: 판매자가 배송 시작 API(`PATCH /shipping`)를 호출하여 운송장 정보를 입력하거나 상태를 변경했을 때의 상태.
4. **COMPLETED (수령 확인/거래 완료)**: 낙찰자가 물품 수령 후 수령 확인 API(`PATCH /complete`)를 호출했을 때의 최종 상태.

---

## 4. 데이터 정합성 및 동시성 제어 정책 (Concurrency Control)

### 4.1 낙관적 락 (Optimistic Lock) 적용
- **도입 배경**: 경매 마감 직전 수많은 유저가 동시에 최고가를 갱신하려고 할 때, '초과 입찰'이나 '데이터 덮어쓰기' 문제를 방지해야 합니다.
- **구현 방식**:
    - `auctions` 테이블에 `version` 컬럼을 활용합니다.
    - JPA의 `@Version` 어노테이션을 사용하여 최고가 업데이트 시점에 버전이 일치하는지 확인합니다.
    - 충돌 발생(버전 불일치) 시 `ObjectOptimisticLockingFailureException`을 가로채서 사용자에게 `BID_CONFLICT (409)` 에러를 반환하고 재시도를 유도합니다.

### 4.2 트랜잭션 보장
- 입찰 기록 생성과 최고가 업데이트는 반드시 하나의 트랜잭션(`@Transactional`)으로 묶여야 하며, 하나라도 실패 시 전체 롤백되어야 합니다.