package com.yettiesoft.shop.web;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.sun.net.httpserver.HttpExchange;
import com.yettiesoft.shop.member.Member;
import com.yettiesoft.shop.member.MemberRepository;

/**
 * 로그인 상태를 기억한다.
 * <p>
 * HTTP는 요청마다 따로 처리되어서, 서버는 지난번 요청을 보낸 사람이 누구였는지 모른다. 그래서
 * <ol>
 *   <li>로그인에 성공하면 아무도 추측할 수 없는 무작위 문자열(세션 ID)을 만들어 "세션 ID → 회원 번호"로 기억해 두고</li>
 *   <li>그 세션 ID를 쿠키로 브라우저에 준다.</li>
 *   <li>브라우저는 이후 모든 요청에 그 쿠키를 자동으로 붙여 보내므로, 서버는 쿠키를 보고 누구인지 알 수 있다.</li>
 * </ol>
 * 세션은 메모리에만 있으므로 서버를 다시 켜면 모두 로그아웃된다.
 */
public class SessionManager {

    static final String COOKIE_NAME = "SESSION";

    private final Map<String, Long> sessions = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private final MemberRepository memberRepository;

    public SessionManager(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /** 요청에 붙어 온 쿠키로 로그인한 회원을 찾는다. 로그인하지 않았으면 빈 값. */
    public Optional<Member> currentMember(HttpExchange exchange) {
        return Requests.cookie(exchange, COOKIE_NAME)
                .map(sessions::get)
                .flatMap(memberRepository::findById);
    }

    /** 새 세션을 만들고 응답에 쿠키를 붙인다. 응답 헤더를 보내기 전에 불러야 한다. */
    public void login(HttpExchange exchange, Member member) {
        // 로그인 전에 쓰던 세션 ID는 버리고 항상 새로 만든다(남이 미리 심어 둔 세션 ID를 쓰지 않도록).
        Requests.cookie(exchange, COOKIE_NAME).ifPresent(sessions::remove);
        String sessionId = newSessionId();
        sessions.put(sessionId, member.id());
        // HttpOnly: 자바스크립트로 쿠키를 읽을 수 없게 한다.
        // SameSite=Lax: 다른 사이트에서 몰래 보내는 POST 요청에는 쿠키를 붙이지 않게 한다.
        exchange.getResponseHeaders().add("Set-Cookie",
                COOKIE_NAME + "=" + sessionId + "; Path=/; HttpOnly; SameSite=Lax");
    }

    /** 세션을 지우고 브라우저의 쿠키도 지우게 한다(Max-Age=0). */
    public void logout(HttpExchange exchange) {
        Requests.cookie(exchange, COOKIE_NAME).ifPresent(sessions::remove);
        exchange.getResponseHeaders().add("Set-Cookie",
                COOKIE_NAME + "=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax");
    }

    private String newSessionId() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
