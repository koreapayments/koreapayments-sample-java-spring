package kr.co.kpn.firstpay.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * KPN FirstPay Spring Boot 샘플 애플리케이션
 *
 * 실행 방법:
 * - IDE: 이 클래스를 직접 실행
 * - Maven: mvn spring-boot:run
 * - JAR: java -jar firstpay-sample-springboot.jar
 *
 * 접속 URL:
 * - 결제 페이지: http://localhost:8080/payment
 */
@SpringBootApplication
public class FirstPaySampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(FirstPaySampleApplication.class, args);
    }
}
