package com.yettiesoft.shop.web;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yettiesoft.shop.member.Member;
import com.yettiesoft.shop.member.MemberService;

/**
 * 로그인. 화면은 templates/login.html.
 * <ul>
 *   <li>GET  /login → 로그인 폼 (/login?joined 이면 가입 완료 안내도 보여준다)</li>
 *   <li>POST /login → 로그인 처리. 성공하면 세션 쿠키를 주고 첫 화면으로 이동한다.</li>
 * </ul>
 */
public class LoginHandler implements HttpHandler {

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
                boolean joined = "joined".equals(exchange.getRequestURI().getQuery());
                showForm(exchange, 200, "", joined, List.of());
            }
            case "POST" -> login(exchange);
            default -> Html.sendMethodNotAllowed(exchange, "GET, POST");
        }
    }

    private void login(HttpExchange exchange) throws IOException {
        Map<String, String> form = Requests.readForm(exchange);
        String loginId = form.getOrDefault("loginId", "");
        Optional<Member> member = memberService.login(loginId, form.getOrDefault("password", ""));
        if (member.isPresent()) {
            sessionManager.login(exchange, member.get());
            Html.redirect(exchange, "/");
        } else {
            // 아이디가 없는지 비밀번호가 틀렸는지 구분해서 알려주면, 가입된 아이디를 알아내는 데 쓰일 수 있다.
            showForm(exchange, 401, loginId, false, List.of("아이디 또는 비밀번호가 올바르지 않습니다."));
        }
    }

    private void showForm(HttpExchange exchange, int status, String loginId, boolean joined, List<String> errors)
            throws IOException {
        view.render(exchange, status, "login", Map.of("loginId", loginId, "joined", joined, "errors", errors));
    }
}
