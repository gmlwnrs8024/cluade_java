package com.yettiesoft.shop.web;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yettiesoft.shop.member.Member;
import com.yettiesoft.shop.member.MemberService;

/**
 * 로그인. 화면은 templates/login.html.
 * <ul>
 *   <li>GET  /login → 로그인 폼 (/login?joined 이면 가입 완료 안내도 보여준다)</li>
 *   <li>POST /login → 로그인 처리. 성공하면 세션 쿠키를 주고 이동한다.</li>
 * </ul>
 * 로그인이 필요한 화면에서 넘어온 경우 /login?next=/cart 처럼 돌아갈 주소를 받아서, 로그인 후 그리로 보낸다.
 */
public class LoginHandler implements HttpHandler {

    /**
     * 돌아갈 주소로 허용하는 모양: 우리 사이트 안의 경로("/"로 시작)만.
     * "//evil.com" 같은 주소를 그대로 따라가면 로그인 직후 다른 사이트로 보내는 데 악용될 수 있다(오픈 리다이렉트).
     */
    private static final Pattern SAFE_NEXT = Pattern.compile("/(?!/)[A-Za-z0-9/_.?=&%-]*");

    private final MemberService memberService;
    private final SessionManager sessionManager;
    private final View view;

    public LoginHandler(MemberService memberService, SessionManager sessionManager, View view) {
        this.memberService = memberService;
        this.sessionManager = sessionManager;
        this.view = view;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestURI().getPath().equals("/login")) {
            view.notFound(exchange);
            return;
        }
        switch (exchange.getRequestMethod()) {
            case "GET" -> {
                Map<String, String> query = Requests.readQuery(exchange);
                boolean joined = query.containsKey("joined");
                showForm(exchange, 200, "", safeNext(query.get("next")), joined, List.of());
            }
            case "POST" -> login(exchange);
            default -> Html.sendMethodNotAllowed(exchange, "GET, POST");
        }
    }

    private void login(HttpExchange exchange) throws IOException {
        Map<String, String> form = Requests.readForm(exchange);
        String loginId = form.getOrDefault("loginId", "");
        String next = safeNext(form.get("next"));
        Optional<Member> member = memberService.login(loginId, form.getOrDefault("password", ""));
        if (member.isPresent()) {
            sessionManager.login(exchange, member.get());
            Html.redirect(exchange, next);
        } else {
            // 아이디가 없는지 비밀번호가 틀렸는지 구분해서 알려주면, 가입된 아이디를 알아내는 데 쓰일 수 있다.
            showForm(exchange, 401, loginId, next, false, List.of("아이디 또는 비밀번호가 올바르지 않습니다."));
        }
    }

    /** 안전한 주소면 그대로, 아니면(없거나 다른 사이트) 첫 화면 "/". */
    static String safeNext(String next) {
        return next != null && SAFE_NEXT.matcher(next).matches() ? next : "/";
    }

    private void showForm(HttpExchange exchange, int status, String loginId, String next, boolean joined,
            List<String> errors) throws IOException {
        view.render(exchange, status, "login",
                Map.of("loginId", loginId, "next", next, "joined", joined, "errors", errors));
    }
}
