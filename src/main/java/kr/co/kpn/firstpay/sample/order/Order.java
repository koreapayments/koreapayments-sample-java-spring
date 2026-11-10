package kr.co.kpn.firstpay.sample.order;

/**
 * 가맹점 주문 정보
 * 결제창을 열기 전에 저장해 두고, 인증 결과(returnUrl)·승인 응답의 금액을 이 값과 대조합니다.
 * 취소도 이 값으로 요청합니다(브라우저가 보낸 값을 쓰지 않음).
 */
public class Order {

    public enum Status { READY, APPROVED, CANCELED }

    private final String mxIssueNo;
    private final String mxIssueDate;
    private final long amount;
    private volatile Status status = Status.READY;
    private volatile String tid;
    private volatile String payMethod;

    public Order(String mxIssueNo, String mxIssueDate, long amount) {
        this.mxIssueNo = mxIssueNo;
        this.mxIssueDate = mxIssueDate;
        this.amount = amount;
    }

    public String getMxIssueNo() {
        return mxIssueNo;
    }

    public String getMxIssueDate() {
        return mxIssueDate;
    }

    public long getAmount() {
        return amount;
    }

    public Status getStatus() {
        return status;
    }

    public String getTid() {
        return tid;
    }

    public String getPayMethod() {
        return payMethod;
    }

    void approve(String tid, String payMethod) {
        this.tid = tid;
        this.payMethod = payMethod;
        this.status = Status.APPROVED;
    }

    void cancel() {
        this.status = Status.CANCELED;
    }
}
