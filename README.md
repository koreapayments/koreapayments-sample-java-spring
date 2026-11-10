# KPN FirstPay 결제 연동 샘플 (Spring Boot)

Spring Boot와 Thymeleaf를 사용한 KPN FirstPay 결제 시스템 연동 샘플입니다.

## 기술 스택

| 분류 | 기술 | 버전 |
|------|------|------|
| 프레임워크 | Spring Boot | 2.7.18 |
| 템플릿 엔진 | Thymeleaf | 3.0.x |
| 빌드 도구 | Maven | 3.x |
| Java | JDK | 1.8+ |
| JSON | org.json | 20230227 |

## 프로젝트 구조

```
src/
├── main/
│   ├── java/kr/co/kpn/firstpay/sample/
│   │   ├── FirstPaySampleApplication.java
│   │   ├── config/
│   │   │   └── PaymentProperties.java
│   │   ├── controller/
│   │   │   └── PaymentController.java
│   │   └── service/
│   │       └── PaymentService.java
│   └── resources/
│       ├── templates/
│       │   ├── payment.html
│       │   ├── auth-callback.html
│       │   └── result.html
│       └── application.yml
```

## 빠른 시작

### 1. passKey 설정 (환경변수)

passKey 는 코드나 `application.yml` 에 적지 않고 **환경변수 `FIRSTPAY_PASS_KEY`** 로만 넘깁니다. 값이 없으면 애플리케이션이 시작되지 않습니다.

```bash
cp .env.example .env     # .env 에 발급받은 passKey 입력 (.env 는 커밋되지 않음)
```

`run.sh` / `run.bat` 은 `.env` 를 읽어 실행합니다. 직접 실행할 때는 환경변수를 먼저 설정하세요.

```bash
export FIRSTPAY_PASS_KEY=발급받은_passKey
```

개발계 테스트 계정(mxId·passKey)은 개발자센터 [테스트 환경](https://developers.firstpay.co.kr/ko/integration/preparation/environment/test-env) 페이지에서 확인합니다. 가맹점 ID 등 나머지 설정은 `src/main/resources/application.yml` 에서 바꿉니다.

### 2. 애플리케이션 실행

#### Maven 사용
```bash
mvn spring-boot:run
```

#### JAR 파일로 실행
```bash
mvn clean package
java -jar target/firstpay-sample-springboot.jar
```

### 3. 브라우저에서 접속

```
http://localhost:8080/payment
```

## 주요 기능

### 결제 요청
- **URL**: `/payment`
- 결제창을 팝업으로 호출하여 인증 진행
- 인증 완료 후 부모창에서 승인 요청 처리

### 결제 승인
- **URL**: `/payment/approve`
- 인증 완료 후 승인 API 호출
- 승인 결과를 result.html에 표시

### 결제 취소
- **URL**: `/payment/cancel`
- 결제 완료 건에 대한 취소 처리
- 결과 페이지에서 취소 버튼 클릭 시 처리

## 보안 안내 (운영 적용 전 확인)

이 샘플은 연동 흐름을 보여주기 위한 코드입니다. 운영에 적용할 때 아래 내용을 지켜 주세요.

- **passKey 는 서버에만**: 환경변수·비밀 저장소로 관리하고, 브라우저 코드·저장소·로그에 남기지 않습니다.
- **승인 전 금액 대조**: 결제창을 열기 전에 주문(주문번호·금액)을 저장하고, 인증 결과(`returnUrl`)의 금액과 승인 응답 금액을 저장한 금액과 대조합니다. 승인 금액이 다르면 즉시 취소합니다. 샘플은 메모리에 저장하지만 운영은 DB 를 사용하세요.
- **approvalUrl 확인**: `https://dev.firstpay.co.kr`(개발) 또는 `https://pay.firstpay.co.kr`(운영) 주소일 때만 승인 요청을 보냅니다. 승인 요청에는 `callHash` 가 들어갑니다.
- **취소는 관리자만**: 취소는 주문번호만 받고 저장된 원거래 값으로 요청합니다. 운영에서는 반드시 관리자 로그인·권한 확인 뒤에만 취소하세요. 샘플에는 인증이 없으므로 외부에 공개된 서버에 그대로 올리지 마세요.
- **로그**: 요청 전문(`callHash`)은 남기지 않고, 응답 전문은 DEBUG 로만 남깁니다.

## 운영 환경 배포

### 1. application.yml 설정

```yaml
firstpay:
  mx-id: ${FIRSTPAY_MX_ID}
  pass-key: ${FIRSTPAY_PASS_KEY}
  env: product

spring:
  thymeleaf:
    cache: true
```

### 2. JAR 파일 빌드

```bash
mvn clean package -DskipTests
```

### 3. 실행

```bash
export FIRSTPAY_MX_ID=your_merchant_id
export FIRSTPAY_PASS_KEY=your_pass_key
java -jar target/firstpay-sample-springboot.jar
```

## 기술 지원

- 이메일: eCommSolutionTeam@kpn.co.kr

---

**버전**: 1.0.0
