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

### 1. 설정 파일 수정

`src/main/resources/application.yml` 파일을 열어 가맹점 정보를 수정합니다:

```yaml
firstpay:
  mx-id: your_merchant_id      # 발급받은 가맹점 ID
  pass-key: your_pass_key      # 발급받은 패스키
  env: develop                 # develop: 개발계, product: 운영계
```

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
