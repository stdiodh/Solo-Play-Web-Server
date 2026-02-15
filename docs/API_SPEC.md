# Solo-Play API 명세서

이 문서는 `Solo-Play-Web-Server`의 인증/추천 API를 코드 기준으로 정리한 문서입니다.

기준 파일:
- `auth/controller/MemberController.kt`
- `place/controller/PlaceController.kt`
- `auth/dto/*.kt`
- `common/exception/GlobalExceptionHandler.kt`

## 공통 응답 포맷

```json
{
  "status": "SUCCESS",
  "message": "요청이 성공했습니다.",
  "data": {}
}
```

- `status`: `SUCCESS` | `ERROR`
- `message`: 처리 메시지
- `data`: 응답 데이터(없으면 `null`)

## 공통 에러 응답 포맷

```json
{
  "status": "ERROR",
  "message": "입력값 유효성 검사에 실패했습니다.",
  "data": {
    "email": "이메일 형식이 올바르지 않습니다."
  }
}
```

- 유효성 실패: `400 Bad Request`
- 비즈니스 예외: `400/401/409` (예외 타입별 상이)
- 미처리 예외: `500 Internal Server Error`

## 1. Auth API (`/api/auth`)

| Method | URI | 설명 | Auth |
|---|---|---|---|
| GET | `/check-email-duplicate` | 이메일 중복 확인 | None |
| POST | `/email-verify` | 회원가입 인증코드 발송 | None |
| POST | `/email-confirm` | 인증코드 검증 + proofToken 발급 | None |
| POST | `/signup` | proofToken 기반 회원가입 완료 | None |
| POST | `/login` | 로그인/토큰 발급 | None |

### 1-1) GET `/api/auth/check-email-duplicate`

#### Query
| 이름 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `email` | String | Y | 중복 확인할 이메일 |

#### Response
- `200 OK`: 사용 가능
- `409 CONFLICT`: 이미 사용 중

**200 예시**
```json
{
  "status": "SUCCESS",
  "message": "사용 가능한 이메일입니다.",
  "data": { "isAvailable": true }
}
```

**409 예시**
```json
{
  "status": "ERROR",
  "message": "이미 사용 중인 아이디에요.",
  "data": { "isAvailable": false }
}
```

### 1-2) POST `/api/auth/email-verify`

#### Request Body
| 필드 | 타입 | 필수 | 제약 |
|---|---|---|---|
| `email` | String | Y | 이메일 형식 |

**요청 예시**
```json
{ "email": "user@example.com" }
```

**응답 예시 (200)**
```json
{
  "status": "SUCCESS",
  "message": "인증 코드를 발송했습니다. 이메일을 확인해주세요.",
  "data": null
}
```

### 1-3) POST `/api/auth/email-confirm`

#### Request Body
| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `email` | String | Y | 인증 대상 이메일 |
| `code` | String | Y | 메일로 수신한 인증 코드 |

**요청 예시**
```json
{ "email": "user@example.com", "code": "123456" }
```

**응답 예시 (200)**
```json
{
  "status": "SUCCESS",
  "message": "인증 코드가 확인되었습니다.",
  "data": {
    "isVerified": true,
    "proofToken": "proof-token"
  }
}
```

### 1-4) POST `/api/auth/signup`

#### Request Body
| 필드 | 타입 | 필수 | 제약 |
|---|---|---|---|
| `email` | String | Y | `@Email`, `@NotBlank` |
| `password` | String | Y | 8~20자, 대소문자/숫자/특수문자 조합 |
| `agreement.isOver14` | Boolean | Y | `true`여야 통과 |
| `agreement.isAgreedToTerms` | Boolean | Y | `true`여야 통과 |
| `agreement.isAgreedToMarketing` | Boolean | Y | 선택 동의 |
| `agreement.isConsentedToAds` | Boolean | Y | 선택 동의 |
| `proofToken` | String | Y | `/email-confirm`에서 발급받은 토큰 |

**요청 예시**
```json
{
  "email": "user@example.com",
  "password": "Abcd1234!",
  "agreement": {
    "isOver14": true,
    "isAgreedToTerms": true,
    "isAgreedToMarketing": false,
    "isConsentedToAds": false
  },
  "proofToken": "proof-token"
}
```

**응답 예시 (201)**
```json
{
  "status": "SUCCESS",
  "message": "회원가입이 완료되었습니다.",
  "data": null
}
```

### 1-5) POST `/api/auth/login`

#### Request Body
| 필드 | 타입 | 필수 | 제약 |
|---|---|---|---|
| `email` | String | Y | `@Email`, `@NotBlank` |
| `password` | String | Y | `@NotBlank` |

**요청 예시**
```json
{ "email": "user@example.com", "password": "Abcd1234!" }
```

**응답 예시 (200)**
```json
{
  "status": "SUCCESS",
  "message": "로그인에 성공했습니다.",
  "data": {
    "grantType": "Bearer",
    "accessToken": "...",
    "refreshToken": "..."
  }
}
```

## 2. Place API (`/api/places`)

| Method | URI | 설명 | Auth |
|---|---|---|---|
| GET | `` | 카카오 검색 + 신규 장소 저장 | Admin |
| GET | `/recommendations` | 레벨 기반 장소 추천  | User |

### 2-1) GET `/api/places`

#### Query
| 이름 | 타입 | 필수 | 기본값 | 설명 |
|---|---|---|---|---|
| `keyword` | String | Y | - | 장소 검색 키워드 |
| `page` | Int | N | `1` | 카카오 검색 페이지 |

**응답 예시 (200)**
```json
{
  "status": "SUCCESS",
  "message": "'홍대' 검색 결과, 15 개의 새로운 장소를 저장했습니다.",
  "data": 15
}
```

### 2-2) GET `/api/places/recommendations`

#### Query
| 이름 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `level` | Enum | Y | `ONE`, `TWO`, `THREE` |

**응답 예시 (200)**
```json
{
  "status": "SUCCESS",
  "message": "레벨 혼자놀기 중수 추천 장소 목록입니다.",
  "data": [
    {
      "level": "TWO",
      "imageUrl": "https://...",
      "placeName": "OO카페",
      "displayTitle": "조용히 작업하기 좋은 카페",
      "area": "마포구",
      "displayTags": ["카페", "혼자"]
    }
  ]
}
```
