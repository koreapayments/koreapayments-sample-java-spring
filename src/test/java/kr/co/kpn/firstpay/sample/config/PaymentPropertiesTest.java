package kr.co.kpn.firstpay.sample.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentPropertiesTest {

    @Test
    void hasNoPassKeyInCode() {
        assertThat(new PaymentProperties().getPassKey()).isNull();
    }

    @Test
    void failsStartupWithoutPassKey() {
        PaymentProperties props = new PaymentProperties();
        assertThatThrownBy(props::afterPropertiesSet)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("FIRSTPAY_PASS_KEY");

        props.setPassKey("   ");
        assertThatThrownBy(props::afterPropertiesSet).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void startsWithPassKey() throws Exception {
        PaymentProperties props = new PaymentProperties();
        props.setPassKey("test-only-key");
        props.afterPropertiesSet();
    }
}
