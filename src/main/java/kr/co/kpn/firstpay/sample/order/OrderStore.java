package kr.co.kpn.firstpay.sample.order;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 주문 저장소 (샘플용 메모리 저장소)
 *
 * ⚠️ 운영에서는 반드시 DB에 저장하세요.
 *    메모리 저장소는 서버를 재시작하면 비워지고, 서버가 여러 대이면 서로 공유되지 않습니다.
 */
@Component
public class OrderStore {

    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    public void save(Order order) {
        orders.put(order.getMxIssueNo(), order);
    }

    /** 주문번호로 찾습니다. 없으면 null */
    public Order find(String mxIssueNo) {
        return mxIssueNo == null ? null : orders.get(mxIssueNo);
    }

    public void markApproved(String mxIssueNo, String tid, String payMethod) {
        Order order = find(mxIssueNo);
        if (order != null) {
            order.approve(tid, payMethod);
        }
    }

    public void markCanceled(String mxIssueNo) {
        Order order = find(mxIssueNo);
        if (order != null) {
            order.cancel();
        }
    }

    /** 테스트용 */
    public void clear() {
        orders.clear();
    }
}
