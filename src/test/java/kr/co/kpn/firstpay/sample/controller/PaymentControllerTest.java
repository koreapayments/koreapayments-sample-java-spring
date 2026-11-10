package kr.co.kpn.firstpay.sample.controller;

import kr.co.kpn.firstpay.sample.config.PaymentProperties;
import kr.co.kpn.firstpay.sample.order.Order;
import kr.co.kpn.firstpay.sample.order.OrderStore;
import kr.co.kpn.firstpay.sample.service.PaymentService;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

@WebMvcTest(PaymentController.class)
@Import(OrderStore.class)
class PaymentControllerTest {

    private static final String APPROVAL_URL = "https://dev.firstpay.co.kr/approval/T1";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private OrderStore orderStore;

    @MockBean
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        orderStore.clear();
        PaymentProperties props = new PaymentProperties();
        props.setMxId("testcorp");
        props.setPassKey("test-only-key");
        when(paymentService.getPaymentProperties()).thenReturn(props);
        orderStore.save(new Order("ORDER1", "20261110120000", 1000L));
    }

    private static JSONObject approvalResponse(long amount) {
        return new JSONObject()
                .put("code", "200")
                .put("data", new JSONObject().put("replyCode", "0000").put("tid", "TID1").put("payMethod", "CC").put("amount", amount));
    }

    @Test
    void approvalRejectsUnknownOrder() throws Exception {
        mvc.perform(post("/payment/approve")
                        .param("mxId", "testcorp").param("mxIssueNo", "NOPE").param("mxIssueDate", "20261110120000")
                        .param("amount", "1000").param("fdTid", "F1").param("approvalUrl", APPROVAL_URL))
                .andExpect(model().attribute("isSuccess", false));
        verify(paymentService, never()).requestApproval(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void approvalRejectsAmountDifferentFromSavedOrder() throws Exception {
        mvc.perform(post("/payment/approve")
                        .param("mxId", "testcorp").param("mxIssueNo", "ORDER1").param("mxIssueDate", "20261110120000")
                        .param("amount", "10").param("fdTid", "F1").param("approvalUrl", APPROVAL_URL))
                .andExpect(model().attribute("isSuccess", false));
        verify(paymentService, never()).requestApproval(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void approvalRejectsNonKpnApprovalUrl() throws Exception {
        mvc.perform(post("/payment/approve")
                        .param("mxId", "testcorp").param("mxIssueNo", "ORDER1").param("mxIssueDate", "20261110120000")
                        .param("amount", "1000").param("fdTid", "F1").param("approvalUrl", "https://evil.example.com/approval/T1"))
                .andExpect(model().attribute("isSuccess", false));
        verify(paymentService, never()).requestApproval(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void approvalWithDifferentApprovedAmountIsCanceledImmediately() throws Exception {
        when(paymentService.requestApproval(eq(APPROVAL_URL), eq("testcorp"), eq("ORDER1"), eq("F1"))).thenReturn(approvalResponse(999L));
        when(paymentService.isApprovalSuccess(any())).thenReturn(true);

        mvc.perform(post("/payment/approve")
                        .param("mxId", "testcorp").param("mxIssueNo", "ORDER1").param("mxIssueDate", "20261110120000")
                        .param("amount", "1000").param("fdTid", "F1").param("approvalUrl", APPROVAL_URL))
                .andExpect(model().attribute("isSuccess", false));

        verify(paymentService).requestCancel("CC", "testcorp", "ORDER1", "20261110120000", 1000L, "TID1");
        assertThat(orderStore.find("ORDER1").getStatus()).isNotEqualTo(Order.Status.APPROVED);
    }

    @Test
    void approvalSuccessMarksOrderApproved() throws Exception {
        when(paymentService.requestApproval(eq(APPROVAL_URL), eq("testcorp"), eq("ORDER1"), eq("F1"))).thenReturn(approvalResponse(1000L));
        when(paymentService.isApprovalSuccess(any())).thenReturn(true);

        mvc.perform(post("/payment/approve")
                        .param("mxId", "testcorp").param("mxIssueNo", "ORDER1").param("mxIssueDate", "20261110120000")
                        .param("amount", "1000").param("fdTid", "F1").param("approvalUrl", APPROVAL_URL))
                .andExpect(model().attribute("isSuccess", true));

        Order order = orderStore.find("ORDER1");
        assertThat(order.getStatus()).isEqualTo(Order.Status.APPROVED);
        assertThat(order.getTid()).isEqualTo("TID1");
        verify(paymentService, never()).requestCancel(anyString(), anyString(), anyString(), anyString(), anyLong(), anyString());
    }

    @Test
    void cancelRejectsOrderThatIsNotApproved() throws Exception {
        mvc.perform(post("/payment/cancel").param("mxIssueNo", "ORDER1"))
                .andExpect(model().attribute("isSuccess", false));
        verify(paymentService, never()).requestCancel(anyString(), anyString(), anyString(), anyString(), anyLong(), anyString());
    }

    @Test
    void cancelUsesSavedOrderValuesNotRequestValues() throws Exception {
        orderStore.markApproved("ORDER1", "TID1", "CC");
        when(paymentService.isCancelSuccess(any())).thenReturn(true);
        when(paymentService.requestCancel(anyString(), anyString(), anyString(), anyString(), anyLong(), anyString()))
                .thenReturn(new JSONObject().put("code", "200").put("data", new JSONObject().put("replyCode", "0000")));

        mvc.perform(post("/payment/cancel")
                        .param("mxIssueNo", "ORDER1")
                        // 아래 값은 무시되어야 한다
                        .param("amount", "1").param("tid", "OTHER").param("payMethod", "VA").param("mxId", "other"))
                .andExpect(model().attribute("isSuccess", true));

        verify(paymentService).requestCancel("CC", "testcorp", "ORDER1", "20261110120000", 1000L, "TID1");
        assertThat(orderStore.find("ORDER1").getStatus()).isEqualTo(Order.Status.CANCELED);
    }
}
