#!/bin/bash
echo "🔨 KPN FirstPay Spring Boot 샘플 빌드"
echo "======================================"
mvn clean package -DskipTests
echo ""
echo "✅ 빌드 완료!"
echo "실행: ./run.sh 또는 java -jar target/firstpay-sample-springboot.jar"
