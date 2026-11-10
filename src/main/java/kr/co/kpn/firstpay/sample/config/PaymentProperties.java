package kr.co.kpn.firstpay.sample.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * KPN FirstPay 결제 설정 정보
 * application.yml 파일의 firstpay.* 속성을 자동으로 매핑합니다.
 */
@Component
@ConfigurationProperties(prefix = "firstpay")
public class PaymentProperties implements InitializingBean {

    /**
     * 가맹점 ID
     */
    private String mxId = "testcorp";

    /**
     * 패스키
     * 코드·설정 파일에 직접 쓰지 말고 환경변수 FIRSTPAY_PASS_KEY 로 넘기세요 (application.yml 참고).
     * 값이 없으면 애플리케이션이 시작되지 않습니다.
     */
    private String passKey;

    /**
     * 환경 설정 (develop: 개발계, product: 운영계)
     */
    private String env = "develop";

    /**
     * 개발계 URL
     */
    private String devUrl = "https://dev.firstpay.co.kr";

    /**
     * 운영계 URL
     */
    private String prodUrl = "https://pay.firstpay.co.kr";

    /**
     * SDK JavaScript URL
     */
    private String sdkJsUrl = "/js/firstpay_v2.js";

    /**
     * 결제창 오픈 타입 (popup, layer, submit)
     */
    private String openType = "popup";

    /**
     * 언어 설정 (ko, en)
     */
    private String lang = "ko";

    // Getters and Setters

    public String getMxId() {
        return mxId;
    }

    public void setMxId(String mxId) {
        this.mxId = mxId;
    }

    public String getPassKey() {
        return passKey;
    }

    public void setPassKey(String passKey) {
        this.passKey = passKey;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getDevUrl() {
        return devUrl;
    }

    public void setDevUrl(String devUrl) {
        this.devUrl = devUrl;
    }

    public String getProdUrl() {
        return prodUrl;
    }

    public void setProdUrl(String prodUrl) {
        this.prodUrl = prodUrl;
    }

    public String getSdkJsUrl() {
        return sdkJsUrl;
    }

    public void setSdkJsUrl(String sdkJsUrl) {
        this.sdkJsUrl = sdkJsUrl;
    }

    public String getOpenType() {
        return openType;
    }

    public void setOpenType(String openType) {
        this.openType = openType;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    /**
     * passKey 가 없으면 시작을 멈춥니다.
     */
    @Override
    public void afterPropertiesSet() {
        if (passKey == null || passKey.trim().isEmpty()) {
            throw new IllegalStateException("firstpay.pass-key 가 없습니다. 환경변수 FIRSTPAY_PASS_KEY 에 발급받은 passKey 를 설정하세요.");
        }
    }

    /**
     * 현재 환경에 맞는 Base URL을 반환합니다.
     */
    public String getBaseUrl() {
        return "develop".equals(env) ? devUrl : prodUrl;
    }

    /**
     * SDK JavaScript 전체 URL을 반환합니다.
     */
    public String getFullSdkJsUrl() {
        return getBaseUrl() + sdkJsUrl;
    }
}
