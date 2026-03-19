package kr.co.kpn.firstpay.sample.service;

import kr.co.kpn.firstpay.sample.config.PaymentProperties;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentProperties paymentProperties;
    private final RestTemplate restTemplate;

    public PaymentService(PaymentProperties paymentProperties) {
        this.paymentProperties = paymentProperties;
        this.restTemplate = new RestTemplate();
    }

    public String generateSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 알고리즘을 찾을 수 없습니다.", e);
        }
    }

    public String generateCallHashForPayment(String mxId, String mxIssueNo, long amount) {
        String data = mxId + mxIssueNo + amount + paymentProperties.getPassKey();
        return generateSHA256(data);
    }

    public String generateCallHashForApproval(String mxId, String mxIssueNo) {
        String data = mxId + mxIssueNo + paymentProperties.getPassKey();
        return generateSHA256(data);
    }

    public String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format(new Date());
    }

    public String generateOrderNo() {
        return paymentProperties.getMxId() + "_" + getCurrentDateTime();
    }

    public JSONObject requestApproval(String approvalUrl, String mxId, String mxIssueNo, String fdTid) {
        try {
            logger.info("=== 결제 승인 요청 ===");
            logger.info("approvalUrl: {}", approvalUrl);
            logger.info("mxId: {}", mxId);
            logger.info("mxIssueNo: {}", mxIssueNo);
            logger.info("fdTid: {}", fdTid);

            // 승인용 callHash 생성
            String callHash = generateCallHashForApproval(mxId, mxIssueNo);

            // 승인 요청 데이터 생성
            JSONObject requestData = new JSONObject();
            requestData.put("mxId", mxId);
            requestData.put("mxIssueNo", mxIssueNo);
            requestData.put("fdTid", fdTid);
            requestData.put("callHash", callHash);

            logger.info("요청 데이터: {}", requestData.toString(2));

            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(requestData.toString(), headers);

            // 승인 요청
            ResponseEntity<String> response = restTemplate.exchange(
                    approvalUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            logger.info("응답 코드: {}", response.getStatusCodeValue());
            logger.info("응답 데이터: {}", response.getBody());

            // 응답 파싱
            JSONObject responseJson = new JSONObject(response.getBody());

            return responseJson;

        } catch (Exception e) {
            logger.error("결제 승인 요청 중 오류 발생", e);
            throw new RuntimeException("결제 승인 요청 중 오류 발생: " + e.getMessage(), e);
        }
    }

    public boolean isApprovalSuccess(JSONObject approvalResponse) {
        if (approvalResponse == null) {
            return false;
        }

        // HTTP 통신 성공 여부
        if (!"200".equals(approvalResponse.optString("code"))) {
            return false;
        }

        // 실제 승인 성공 여부
        JSONObject data = approvalResponse.optJSONObject("data");
        if (data == null) {
            return false;
        }

        return "0000".equals(data.optString("replyCode"));
    }

    public String getErrorMessage(JSONObject approvalResponse) {
        if (approvalResponse == null) {
            return "승인 응답이 없습니다.";
        }

        if (!"200".equals(approvalResponse.optString("code"))) {
            return approvalResponse.optString("message", "알 수 없는 오류");
        }

        JSONObject data = approvalResponse.optJSONObject("data");
        if (data != null) {
            return data.optString("replyMessage", "알 수 없는 오류");
        }

        return "알 수 없는 오류";
    }

    public JSONObject requestCancel(String payMethod, String mxId, String mxIssueNo, String mxIssueDate, long amount, String tid) {
        try {
            logger.info("=== 결제 취소 요청 ===");
            logger.info("payMethod: {}", payMethod);
            logger.info("mxId: {}", mxId);
            logger.info("mxIssueNo: {}", mxIssueNo);
            logger.info("mxIssueDate: {}", mxIssueDate);
            logger.info("amount: {}", amount);
            logger.info("tid: {}", tid);

            // 취소용 callHash 생성
            String callHash = generateCallHashForCancel(mxId, mxIssueNo, amount);

            // 취소 API URL 생성
            String cancelUrl = paymentProperties.getBaseUrl() + "/cancel";

            // 취소 요청 데이터 생성
            JSONObject requestData = new JSONObject();
            requestData.put("payMethod", payMethod);
            requestData.put("mxId", mxId);
            requestData.put("mxIssueNo", mxIssueNo);
            requestData.put("mxIssueDate", mxIssueDate);
            requestData.put("amount", amount);
            requestData.put("callHash", callHash);

            if (tid != null && !tid.isEmpty()) {
                requestData.put("tid", tid);
            }

            logger.info("요청 데이터: {}", requestData.toString(2));

            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(requestData.toString(), headers);

            // 취소 요청
            ResponseEntity<String> response = restTemplate.exchange(
                    cancelUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            logger.info("응답 코드: {}", response.getStatusCodeValue());
            logger.info("응답 데이터: {}", response.getBody());

            // 응답 파싱
            JSONObject responseJson = new JSONObject(response.getBody());

            return responseJson;

        } catch (Exception e) {
            logger.error("결제 취소 요청 중 오류 발생", e);
            throw new RuntimeException("결제 취소 요청 중 오류 발생: " + e.getMessage(), e);
        }
    }

    public String generateCallHashForCancel(String mxId, String mxIssueNo, long amount) {
        String data = mxId + mxIssueNo + amount + paymentProperties.getPassKey();
        return generateSHA256(data);
    }

    public boolean isCancelSuccess(JSONObject cancelResponse) {
        if (cancelResponse == null) {
            return false;
        }

        // HTTP 통신 성공 여부 (결제 API 공통 형식 - code: "200")
        if (!"200".equals(cancelResponse.optString("code"))) {
            return false;
        }

        // 실제 취소 성공 여부 (data 내의 replyCode: "0000")
        JSONObject data = cancelResponse.optJSONObject("data");
        if (data == null) {
            return false;
        }

        return "0000".equals(data.optString("replyCode"));
    }

    public PaymentProperties getPaymentProperties() {
        return paymentProperties;
    }
}
