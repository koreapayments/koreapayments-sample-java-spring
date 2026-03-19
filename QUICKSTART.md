# KPN FirstPay 빠른 시작 가이드 (Spring Boot)

## 1단계: 프로젝트 준비

### ZIP 파일 사용
```bash
unzip kpn-firstpay-sample-springboot-v1.0.0.zip
cd kpn-firstpay-sample-springboot-v1.0.0
```

---

## 2단계: 가맹점 정보 설정

`src/main/resources/application.yml` 파일을 엽니다:

```yaml
firstpay:
  mx-id: testcorp                           # 👈 발급받은 가맹점 ID
  pass-key: 6aMoJujE34XnL9gvUqdKGMqs9GzYaNo6  # 👈 발급받은 패스키
  env: develop                              # develop: 개발계, product: 운영계
```

**테스트 계정 (개발계)**
- mxId: `testcorp`
- passKey: `6aMoJujE34XnL9gvUqdKGMqs9GzYaNo6`

---

## 3단계: 애플리케이션 실행

### Maven 사용
```bash
mvn spring-boot:run
```

### IntelliJ IDEA
1. `FirstPaySampleApplication.java` 파일 열기
2. 메인 메서드 왼쪽의 실행 버튼 클릭

### Eclipse
1. 프로젝트 우클릭
2. Run As > Spring Boot App

---

## 4단계: 테스트

브라우저에서 접속:

```
http://localhost:8080/payment
```

**결제하기** 버튼을 클릭하여 테스트!

---

## 🎉 완료!
---

## 주요 파일 위치

```
src/main/
├── java/.../springboot/
│   ├── FirstPaySampleApplication.java   # 메인 클래스 ⭐
│   ├── config/PaymentProperties.java    # 설정 클래스
│   ├── controller/PaymentController.java # 컨트롤러
│   └── service/PaymentService.java      # 서비스
└── resources/
    ├── application.yml                  # 설정 파일 ⭐
    └── templates/
        ├── payment.html                 # 결제 페이지
        └── result.html                  # 결과 페이지
```

---

## 빠른 설정 변경

### 포트 변경
```yaml
server:
  port: 9090
```

### 운영 환경으로 전환
```yaml
firstpay:
  env: product
```

### 로그 레벨 조정
```yaml
logging:
  level:
    kr.co.kpn.firstpay: DEBUG
```

---

## 문제 해결
- 📖 [상세 가이드](./README.md)
- 
| 문제 | 해결 |
|------|------|
| 포트 충돌 | `application.yml`에서 `server.port` 변경 |
| 템플릿 오류 | `templates/` 디렉토리에 HTML 파일 확인 |
| callHash 오류 | `pass-key` 값 확인 |
| 실행 안됨 | `mvn clean install` 후 재실행 |

---

## 📞 도움이 필요하신가요?

**이메일**: eCommSolutionTeam@kpn.co.kr

문의 시 다음 정보를 포함해 주세요:
- 가맹점 ID
- 에러 로그

---

