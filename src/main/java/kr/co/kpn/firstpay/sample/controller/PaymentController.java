package kr.co.kpn.firstpay.sample.controller;

import kr.co.kpn.firstpay.sample.order.Order;
import kr.co.kpn.firstpay.sample.order.OrderStore;
import kr.co.kpn.firstpay.sample.service.ApprovalUrlPolicy;
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
    private final OrderStore orderStore;

    public PaymentController(PaymentService paymentService, OrderStore orderStore) {
        this.paymentService = paymentService;
        this.orderStore = orderStore;
    }

    @GetMapping("/payment")
    public String paymentPage(Model model) {
        logger.info("=== 결제 페이지 접근 ===");

        // 가맹점 정보
        String mxId = paymentService.getPaymentProperties().getMxId();
        String mxIssueDate = paymentService.getCurrentDateTime();
        String mxIssueNo = paymentService.generateOrderNo();

        // 결제 정보 (금액은 서버가 정합니다. 브라우저가 보낸 금액을 쓰지 마세요)
        String orderName = "테스트 상품";
        long amount = 1000;
        String custName = "홍길동";
        String email = "test@example.com";
        String phoneNo = "01012345678";

        // 주문 저장: 인증 결과·승인 응답의 금액 대조와 취소에 사용 (운영은 DB)
        orderStore.save(new Order(mxIssueNo, mxIssueDate, amount));

        // callHash 생성 (passKey 는 서버에만 있습니다)
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

    /**
     * 결제 인증 결과 수신 (팝업)
     * 받은 값을 부모 창으로 넘기기만 하고, 승인 전 검증은 /payment/approve 에서 합니다.
     */
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

        logger.info("=== 결제 인증 결과 수신 (팝업) === mxIssueNo: {}, code: {}, fdTid: {}", mxIssueNo, code, fdTid);

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

    /**
     * 결제 승인 요청 (팝업에서 받은 인증 결과로 승인)
     */
    @PostMapping("/payment/approve")
    public String approvePayment(
            @RequestParam String mxId,
            @RequestParam String mxIssueNo,
            @RequestParam String mxIssueDate,
            @RequestParam String amount,
            @RequestParam String fdTid,
            @RequestParam String approvalUrl,
            Model model) {

        logger.info("=== 결제 승인 요청 === mxIssueNo: {}, fdTid: {}", mxIssueNo, fdTid);
        return approveAndRender(mxId, mxIssueNo, amount, fdTid, approvalUrl, model);
    }

    /**
     * 결제 인증 결과 수신 후 바로 승인 (submit 방식)
     */
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

        logger.info("=== 결제 인증 결과 수신 === mxIssueNo: {}, code: {}, fdTid: {}", mxIssueNo, code, fdTid);

        if (!"0000".equals(code)) {
            String resultMessage = "결제 인증 실패: " + (message != null ? message : "알 수 없는 오류");
            logger.error(resultMessage);
            Order order = orderStore.find(mxIssueNo);
            addResult(model, false, resultMessage, order, mxIssueNo, code, message, fdTid);
            model.addAttribute("payMethod", "CC");
            return "result";
        }
        return approveAndRender(mxId, mxIssueNo, amount, fdTid, approvalUrl, model);
    }

    /**
     * 승인 전 검증 → 승인 → 승인 금액 대조
     *
     * returnUrl 로 받은 값은 사용자 브라우저를 거쳐 전달되므로 바뀌었을 수 있습니다.
     * 1) 저장해 둔 주문이 있고 금액이 같은지
     * 2) approvalUrl 이 KPN 결제 서버 주소인지
     * 를 확인한 뒤에만 승인하고, 승인 응답 금액이 주문 금액과 다르면 즉시 취소합니다.
     */
    private String approveAndRender(String mxId, String mxIssueNo, String amount, String fdTid, String approvalUrl, Model model) {
        String configuredMxId = paymentService.getPaymentProperties().getMxId();
        Order order = orderStore.find(mxIssueNo);

        String rejectReason = null;
        if (order == null) {
            rejectReason = "주문 정보를 찾을 수 없습니다.";
        } else if (order.getStatus() != Order.Status.READY) {
            rejectReason = "이미 처리된 주문입니다.";
        } else if (!configuredMxId.equals(mxId)) {
            rejectReason = "가맹점 ID가 일치하지 않습니다.";
        } else if (!String.valueOf(order.getAmount()).equals(amount)) {
            rejectReason = "결제 금액이 주문 금액과 다릅니다.";
        } else if (!ApprovalUrlPolicy.isAllowed(approvalUrl)) {
            rejectReason = "허용되지 않은 승인 요청 주소입니다.";
        }
        if (rejectReason != null) {
            logger.warn("승인 요청 거부 - mxIssueNo: {}, 사유: {}", mxIssueNo, rejectReason);
            addResult(model, false, "결제 승인 거부: " + rejectReason, order, mxIssueNo, "9999", rejectReason, fdTid);
            model.addAttribute("payMethod", "CC");
            return "result";
        }

        boolean isSuccess = false;
        String resultMessage;
        JSONObject approvalData = null;

        try {
            approvalData = paymentService.requestApproval(approvalUrl, configuredMxId, mxIssueNo, fdTid);

            if (paymentService.isApprovalSuccess(approvalData)) {
                JSONObject data = approvalData.optJSONObject("data");
                String tid = data.optString("tid");
                String payMethod = data.optString("payMethod");
                long approvedAmount = data.optLong("amount", -1);

                if (approvedAmount != order.getAmount()) {
                    // 승인 금액이 주문 금액과 다르면 즉시 취소합니다
                    logger.error("승인 금액 불일치 - mxIssueNo: {}, 주문: {}, 승인: {} → 즉시 취소", mxIssueNo, order.getAmount(), approvedAmount);
                    paymentService.requestCancel(payMethod, configuredMxId, mxIssueNo, order.getMxIssueDate(), order.getAmount(), tid);
                    orderStore.markCanceled(mxIssueNo);
                    resultMessage = "승인 금액이 주문 금액과 달라 결제를 취소했습니다.";
                } else {
                    orderStore.markApproved(mxIssueNo, tid, payMethod);
                    isSuccess = true;
                    resultMessage = "결제 승인이 완료되었습니다.";
                    logger.info("결제 승인 성공 - mxIssueNo: {}, TID: {}", mxIssueNo, tid);
                }
            } else {
                resultMessage = "결제 승인 실패: " + paymentService.getErrorMessage(approvalData);
                logger.error(resultMessage);
            }
        } catch (Exception e) {
            resultMessage = "결제 승인 중 오류 발생: " + e.getMessage();
            logger.error(resultMessage, e);
        }

        addResult(model, isSuccess, resultMessage, order, mxIssueNo, isSuccess ? "0000" : "9999", resultMessage, fdTid);
        addApprovalData(model, approvalData);
        return "result";
    }

    /**
     * 결제 취소
     *
     * 주문번호만 받고, 금액·거래번호·결제수단은 저장해 둔 원거래 값을 사용합니다.
     * ⚠️ 운영에서는 반드시 관리자 로그인·권한 확인 뒤에만 취소하세요.
     *    이 샘플에는 인증이 없으므로 외부에 공개된 서버에 그대로 올리지 마세요.
     */
    @PostMapping("/payment/cancel")
    public String cancelPayment(@RequestParam String mxIssueNo, Model model) {
        logger.info("=== 결제 취소 요청 === mxIssueNo: {}", mxIssueNo);

        String mxId = paymentService.getPaymentProperties().getMxId();
        Order order = orderStore.find(mxIssueNo);

        boolean isSuccess = false;
        String resultMessage;
        JSONObject cancelData = null;

        if (order == null || order.getStatus() != Order.Status.APPROVED) {
            resultMessage = "결제 취소 실패: 승인된 주문이 아닙니다.";
            logger.warn("취소 거부 - mxIssueNo: {}", mxIssueNo);
        } else {
            try {
                cancelData = paymentService.requestCancel(order.getPayMethod(), mxId, order.getMxIssueNo(),
                        order.getMxIssueDate(), order.getAmount(), order.getTid());

                if (paymentService.isCancelSuccess(cancelData)) {
                    orderStore.markCanceled(mxIssueNo);
                    isSuccess = true;
                    resultMessage = "결제 취소가 완료되었습니다.";
                    logger.info("결제 취소 성공 - mxIssueNo: {}, TID: {}", mxIssueNo, order.getTid());
                } else {
                    resultMessage = "결제 취소 실패: " + paymentService.getErrorMessage(cancelData);
                    logger.error(resultMessage);
                }
            } catch (Exception e) {
                resultMessage = "결제 취소 중 오류 발생: " + e.getMessage();
                logger.error(resultMessage, e);
            }
        }

        addResult(model, isSuccess, resultMessage, order, mxIssueNo, isSuccess ? "0000" : "9999", resultMessage, null);
        model.addAttribute("tid", order != null ? order.getTid() : null);
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

    /** 결과 화면 공통 값. 금액·주문일시는 저장해 둔 주문 값을 씁니다 */
    private void addResult(Model model, boolean isSuccess, String resultMessage, Order order,
                           String mxIssueNo, String code, String message, String fdTid) {
        model.addAttribute("isSuccess", isSuccess);
        model.addAttribute("resultMessage", resultMessage);
        model.addAttribute("mxId", paymentService.getPaymentProperties().getMxId());
        model.addAttribute("mxIssueNo", mxIssueNo);
        model.addAttribute("mxIssueDate", order != null ? order.getMxIssueDate() : "");
        model.addAttribute("amount", order != null ? order.getAmount() : 0L);
        model.addAttribute("code", code);
        model.addAttribute("message", message);
        model.addAttribute("fdTid", fdTid);
    }

    private void addApprovalData(Model model, JSONObject approvalData) {
        if (approvalData == null) {
            model.addAttribute("payMethod", "CC");
            return;
        }
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
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/payment";
    }
}
