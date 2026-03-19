package kr.co.kpn.firstpay.sample.controller;

import kr.co.kpn.firstpay.sample.service.PaymentService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/payment")
    public String paymentPage(Model model) {
        logger.info("=== 결제 페이지 접근 ===");

        // 가맹점 정보
        String mxId = paymentService.getPaymentProperties().getMxId();
        String mxIssueDate = paymentService.getCurrentDateTime();
        String mxIssueNo = paymentService.generateOrderNo();

        // 결제 정보
        String orderName = "테스트 상품";
        long amount = 1000;
        String custName = "홍길동";
        String email = "test@example.com";
        String phoneNo = "01012345678";

        // callHash 생성
        String callHash = paymentService.generateCallHashForPayment(mxId, mxIssueNo, amount);

        // Model에 데이터 추가
        model.addAttribute("mxId", mxId);
        model.addAttribute("mxIssueNo", mxIssueNo);
        model.addAttribute("mxIssueDate", mxIssueDate);
        model.addAttribute("orderName", orderName);
        model.addAttribute("amount", amount);
        model.addAttribute("custName", custName);
        model.addAttribute("email", email);
        model.addAttribute("phoneNo", phoneNo);
        model.addAttribute("callHash", callHash);

        // FirstPay SDK 설정
        model.addAttribute("sdkUrl", paymentService.getPaymentProperties().getFullSdkJsUrl());
        model.addAttribute("env", paymentService.getPaymentProperties().getEnv());
        model.addAttribute("openType", paymentService.getPaymentProperties().getOpenType());
        model.addAttribute("lang", paymentService.getPaymentProperties().getLang());

        return "payment";
    }

    @PostMapping("/payment/auth-callback")
    public String authCallback(
            @RequestParam String mxId,
            @RequestParam String mxIssueNo,
            @RequestParam String mxIssueDate,
            @RequestParam String amount,
            @RequestParam(required = false) String mallApReserved,
            @RequestParam String code,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String fdTid,
            @RequestParam(required = false) String approvalUrl,
            Model model) {

        logger.info("=== 결제 인증 결과 수신 (팝업) ===");
        logger.info("mxId: {}", mxId);
        logger.info("mxIssueNo: {}", mxIssueNo);
        logger.info("amount: {}", amount);
        logger.info("code: {}", code);
        logger.info("fdTid: {}", fdTid);
        model.addAttribute("mxId", mxId);
        model.addAttribute("mxIssueNo", mxIssueNo);
        model.addAttribute("mxIssueDate", mxIssueDate);
        model.addAttribute("amount", amount);
        model.addAttribute("code", code);
        model.addAttribute("message", message != null ? message : "");
        model.addAttribute("fdTid", fdTid != null ? fdTid : "");
        model.addAttribute("approvalUrl", approvalUrl != null ? approvalUrl : "");

        return "auth-callback";
    }

    @PostMapping("/payment/approve")
    public String approvePayment(
            @RequestParam String mxId,
            @RequestParam String mxIssueNo,
            @RequestParam String mxIssueDate,
            @RequestParam String amount,
            @RequestParam String fdTid,
            @RequestParam String approvalUrl,
            Model model) {

        logger.info("=== 결제 승인 요청 ===");
        logger.info("mxId: {}", mxId);
        logger.info("mxIssueNo: {}", mxIssueNo);
        logger.info("mxIssueDate: {}", mxIssueDate);
        logger.info("amount: {}", amount);
        logger.info("fdTid: {}", fdTid);

        boolean isSuccess = false;
        String resultMessage = "";
        JSONObject approvalData = null;

        try {
            approvalData = paymentService.requestApproval(approvalUrl, mxId, mxIssueNo, fdTid);

            if (paymentService.isApprovalSuccess(approvalData)) {
                isSuccess = true;
                resultMessage = "결제 승인이 완료되었습니다.";
                JSONObject data = approvalData.optJSONObject("data");
                logger.info("결제 승인 성공 - TID: {}", data.optString("tid"));
            } else {
                resultMessage = "결제 승인 실패: " + paymentService.getErrorMessage(approvalData);
                logger.error(resultMessage);
            }

        } catch (Exception e) {
            resultMessage = "결제 승인 중 오류 발생: " + e.getMessage();
            logger.error(resultMessage, e);
        }

        // Model에 데이터 추가
        model.addAttribute("isSuccess", isSuccess);
        model.addAttribute("resultMessage", resultMessage);
        model.addAttribute("mxId", mxId);
        model.addAttribute("mxIssueNo", mxIssueNo);
        model.addAttribute("mxIssueDate", mxIssueDate);
        model.addAttribute("amount", amount);
        model.addAttribute("code", isSuccess ? "0000" : "9999");
        model.addAttribute("message", resultMessage);
        model.addAttribute("fdTid", fdTid);

        if (approvalData != null) {
            model.addAttribute("approvalData", approvalData.toString(2));
            JSONObject data = approvalData.optJSONObject("data");
            if (data != null) {
                model.addAttribute("tid", data.optString("tid"));
                model.addAttribute("payMethod", data.optString("payMethod"));
                model.addAttribute("authDate", data.optString("authDate"));
                model.addAttribute("authNo", data.optString("authNo"));
                model.addAttribute("issName", data.optString("issName"));
                model.addAttribute("vactNo", data.optString("vactNo"));
                model.addAttribute("bankName", data.optString("bankName"));
                model.addAttribute("expireDate", data.optString("expireDate"));
            }
        } else {
            model.addAttribute("payMethod", "CC");
        }

        return "result";
    }

    @PostMapping("/payment/result")
    public String paymentResult(
            @RequestParam String mxId,
            @RequestParam String mxIssueNo,
            @RequestParam String mxIssueDate,
            @RequestParam String amount,
            @RequestParam(required = false) String mallApReserved,
            @RequestParam String code,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String fdTid,
            @RequestParam(required = false) String approvalUrl,
            Model model) {

        logger.info("=== 결제 인증 결과 수신 ===");
        logger.info("mxId: {}", mxId);
        logger.info("mxIssueNo: {}", mxIssueNo);
        logger.info("amount: {}", amount);
        logger.info("code: {}", code);
        logger.info("fdTid: {}", fdTid);

        boolean isSuccess = "0000".equals(code);
        String resultMessage = "";
        JSONObject approvalData = null;

        if (isSuccess) {
            try {
                approvalData = paymentService.requestApproval(approvalUrl, mxId, mxIssueNo, fdTid);

                if (paymentService.isApprovalSuccess(approvalData)) {
                    resultMessage = "결제 승인이 완료되었습니다.";
                    JSONObject data = approvalData.optJSONObject("data");
                    logger.info("결제 승인 성공 - TID: {}", data.optString("tid"));
                } else {
                    isSuccess = false;
                    resultMessage = "결제 승인 실패: " + paymentService.getErrorMessage(approvalData);
                    logger.error(resultMessage);
                }

            } catch (Exception e) {
                isSuccess = false;
                resultMessage = "결제 승인 중 오류 발생: " + e.getMessage();
                logger.error(resultMessage, e);
            }
        } else {
            // 인증 실패
            resultMessage = "결제 인증 실패: " + (message != null ? message : "알 수 없는 오류");
            logger.error(resultMessage);
        }

        // Model에 데이터 추가
        model.addAttribute("isSuccess", isSuccess);
        model.addAttribute("resultMessage", resultMessage);
        model.addAttribute("mxId", mxId);
        model.addAttribute("mxIssueNo", mxIssueNo);
        model.addAttribute("mxIssueDate", mxIssueDate);
        model.addAttribute("amount", amount);
        model.addAttribute("code", code);
        model.addAttribute("message", message);
        model.addAttribute("fdTid", fdTid);

        if (approvalData != null) {
            model.addAttribute("approvalData", approvalData.toString(2));
            JSONObject data = approvalData.optJSONObject("data");
            if (data != null) {
                model.addAttribute("tid", data.optString("tid"));
                model.addAttribute("payMethod", data.optString("payMethod"));
                model.addAttribute("authDate", data.optString("authDate"));
                model.addAttribute("authNo", data.optString("authNo"));
                model.addAttribute("issName", data.optString("issName"));
                model.addAttribute("vactNo", data.optString("vactNo"));
                model.addAttribute("bankName", data.optString("bankName"));
                model.addAttribute("expireDate", data.optString("expireDate"));
            }
        } else {
            model.addAttribute("payMethod", "CC");
        }

        return "result";
    }

    @PostMapping("/payment/cancel")
    public String cancelPayment(
            @RequestParam String payMethod,
            @RequestParam String mxId,
            @RequestParam String mxIssueNo,
            @RequestParam String mxIssueDate,
            @RequestParam String amount,
            @RequestParam String tid,
            Model model) {

        logger.info("=== 결제 취소 요청 ===");
        logger.info("payMethod: {}", payMethod);
        logger.info("mxId: {}", mxId);
        logger.info("mxIssueNo: {}", mxIssueNo);
        logger.info("mxIssueDate: {}", mxIssueDate);
        logger.info("amount: {}", amount);
        logger.info("tid: {}", tid);

        boolean isSuccess = false;
        String resultMessage = "";
        JSONObject cancelData = null;

        try {
            long amountValue = Long.parseLong(amount);
            cancelData = paymentService.requestCancel(payMethod, mxId, mxIssueNo, mxIssueDate, amountValue, tid);

            if (paymentService.isCancelSuccess(cancelData)) {
                isSuccess = true;
                resultMessage = "결제 취소가 완료되었습니다.";
                JSONObject data = cancelData.optJSONObject("data");
                logger.info("결제 취소 성공 - TID: {}", data.optString("tid"));
            } else {
                resultMessage = "결제 취소 실패: " + paymentService.getErrorMessage(cancelData);
                logger.error(resultMessage);
            }

        } catch (Exception e) {
            resultMessage = "결제 취소 중 오류 발생: " + e.getMessage();
            logger.error(resultMessage, e);
        }

        // Model에 데이터 추가
        model.addAttribute("isSuccess", isSuccess);
        model.addAttribute("resultMessage", resultMessage);
        model.addAttribute("mxId", mxId);
        model.addAttribute("mxIssueNo", mxIssueNo);
        model.addAttribute("mxIssueDate", mxIssueDate);
        model.addAttribute("amount", amount);
        model.addAttribute("code", isSuccess ? "0000" : "9999");
        model.addAttribute("message", resultMessage);
        model.addAttribute("tid", tid);
        model.addAttribute("isCanceled", true);

        if (cancelData != null) {
            model.addAttribute("approvalData", cancelData.toString(2));
            JSONObject data = cancelData.optJSONObject("data");
            if (data != null) {
                model.addAttribute("payMethod", data.optString("payMethod"));
                model.addAttribute("authNo", data.optString("authNo"));
                model.addAttribute("acqName", data.optString("acqName"));
                model.addAttribute("issName", data.optString("issName"));
                model.addAttribute("ccNo", data.optString("ccNo"));
                model.addAttribute("authDate", data.optString("authDate"));
                model.addAttribute("oTid", data.optString("oTid"));
            }
        }

        return "result";
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/payment";
    }
}
