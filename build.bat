@echo off
echo 🔨 KPN FirstPay Spring Boot 샘플 빌드
echo ======================================
call mvn clean package -DskipTests
echo.
echo ✅ 빌드 완료!
echo 실행: run.bat 또는 java -jar target\firstpay-sample-springboot.jar
