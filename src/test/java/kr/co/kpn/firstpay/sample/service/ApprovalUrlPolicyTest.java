package kr.co.kpn.firstpay.sample.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApprovalUrlPolicyTest {

    @Test
    void allowsHttpsKpnHosts() {
        assertThat(ApprovalUrlPolicy.isAllowed("https://dev.firstpay.co.kr/approval/abc")).isTrue();
        assertThat(ApprovalUrlPolicy.isAllowed("https://pay.firstpay.co.kr/approval/abc")).isTrue();
    }

    @Test
    void rejectsOtherSchemesHostsAndTricks() {
        assertThat(ApprovalUrlPolicy.isAllowed(null)).isFalse();
        assertThat(ApprovalUrlPolicy.isAllowed("")).isFalse();
        assertThat(ApprovalUrlPolicy.isAllowed("not a url")).isFalse();
        assertThat(ApprovalUrlPolicy.isAllowed("http://dev.firstpay.co.kr/approval/abc")).isFalse();
        assertThat(ApprovalUrlPolicy.isAllowed("https://evil.example.com/approval/abc")).isFalse();
        assertThat(ApprovalUrlPolicy.isAllowed("https://dev.firstpay.co.kr.evil.example.com/approval/abc")).isFalse();
        assertThat(ApprovalUrlPolicy.isAllowed("https://dev.firstpay.co.kr@evil.example.com/approval/abc")).isFalse();
        assertThat(ApprovalUrlPolicy.isAllowed("https://user@dev.firstpay.co.kr/approval/abc")).isFalse();
        assertThat(ApprovalUrlPolicy.isAllowed("https://dev.firstpay.co.kr:8443/approval/abc")).isFalse();
        assertThat(ApprovalUrlPolicy.isAllowed("https://127.0.0.1/approval/abc")).isFalse();
    }
}
