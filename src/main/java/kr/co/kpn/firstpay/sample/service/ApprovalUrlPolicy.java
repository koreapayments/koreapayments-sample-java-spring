package kr.co.kpn.firstpay.sample.service;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 승인 요청 주소(approvalUrl) 검증
 *
 * approvalUrl 은 인증 결과(returnUrl)로 사용자 브라우저를 거쳐 전달되므로 바뀌었을 수 있습니다.
 * 승인 요청에는 passKey 로 만든 callHash 가 들어가므로, KPN 결제 서버가 아닌 곳으로 보내면
 * 해시가 유출되고 가맹점 서버가 임의 주소를 호출하게 됩니다(SSRF).
 * 따라서 https 이고 KPN 결제 서버 주소일 때만 호출합니다.
 */
public final class ApprovalUrlPolicy {

    private static final Set<String> ALLOWED_HOSTS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "dev.firstpay.co.kr",   // 개발
            "pay.firstpay.co.kr"    // 운영
    )));

    private ApprovalUrlPolicy() {
    }

    public static boolean isAllowed(String approvalUrl) {
        if (approvalUrl == null || approvalUrl.isEmpty()) {
            return false;
        }
        try {
            URI uri = new URI(approvalUrl);
            return "https".equals(uri.getScheme())
                    && uri.getUserInfo() == null
                    && (uri.getPort() == -1 || uri.getPort() == 443)
                    && uri.getHost() != null
                    && ALLOWED_HOSTS.contains(uri.getHost());
        } catch (Exception e) {
            return false;
        }
    }
}
