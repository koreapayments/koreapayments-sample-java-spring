# KPN FirstPay 빠른 시작 가이드 (Spring Boot)

## 1단계: 프로젝트 준비

### ZIP 파일 사용
```bash
unzip kpn-firstpay-sample-springboot-v1.0.0.zip
cd kpn-firstpay-sample-springboot-v1.0.0
```

---

## 2단계: passKey 설정

passKey 는 코드·설정 파일에 적지 않고 환경변수 `FIRSTPAY_PASS_KEY` 로 넘깁니다. 값이 없으면 애플리케이션이 시작되지 않습니다.

```bash
cp .env.example .env     # .env 를 열어 FIRSTPAY_PASS_KEY 값 입력
./run.sh                 # Windows: run.bat
```

IDE 에서 실행할 때는 실행 설정(Run Configuration)의 환경변수에 `FIRSTPAY_PASS_KEY` 를 추가하세요.

**테스트 계정 (개발계)**: mxId `testcorp`, passKey 는 개발자센터 [테스트 환경](https://developers.firstpay.co.kr/ko/integration/preparation/environment/test-env) 페이지에서 확인합니다.

가맹점 ID·환경(`env`) 등 나머지 설정은 `src/main/resources/application.yml` 에서 바꿉니다.

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
| callHash 오류 | 환경변수 `FIRSTPAY_PASS_KEY` 값 확인 |
| 실행 안됨 | `FIRSTPAY_PASS_KEY` 설정 확인 (없으면 시작 실패), `mvn clean install` 후 재실행 |

---

## 📞 도움이 필요하신가요?

**이메일**: eCommSolutionTeam@kpn.co.kr

문의 시 다음 정보를 포함해 주세요:
- 가맹점 ID
- 에러 로그

---

