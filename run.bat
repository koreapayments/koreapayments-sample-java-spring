@echo off
echo 🚀 KPN FirstPay Spring Boot 샘플 실행
echo ======================================
echo 접속: http://localhost:8080/payment
echo.
rem .env 가 있으면 환경변수로 읽는다 (FIRSTPAY_PASS_KEY)
if exist .env for /f "usebackq eol=# tokens=1,* delims==" %%a in (".env") do set "%%a=%%b"
java -jar target\firstpay-sample-springboot.jar
