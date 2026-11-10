#!/bin/bash
echo "🚀 KPN FirstPay Spring Boot 샘플 실행"
echo "======================================"
echo "접속: http://localhost:8080/payment"
echo ""
# .env 가 있으면 환경변수로 읽는다 (FIRSTPAY_PASS_KEY). Windows 에서 만든 .env 의 CR 은 지운다
if [ -f .env ]; then set -a; . <(tr -d '\r' < .env); set +a; fi
java -jar target/firstpay-sample-springboot.jar
