package com.yettiesoft.shop.web;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yettiesoft.shop.member.MemberService;
import com.yettiesoft.shop.member.SignUpException;

/**
 * 회원가입. 화면은 templates/signup.html.
 * <ul>
 *   <li>GET  /signup → 가입 폼</li>
 *   <li>POST /signup → 가입 처리. 성공하면 로그인 화면으로 이동, 실패하면 오류와 함께 폼을 다시 보여준다.</li>
 * </ul>
 */
public class SignUpHandler implements HttpHandler {

    private final MemberService memberService;
    private final View view;

    public SignUpHandler(MemberService memberService, View view) {
        this.memberService = memberService;
        this.view = view;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestURI().getPath().equals("/signup")) {
            view.notFound(exchange);
            return;
        }
        switch (exchange.getRequestMethod()) {
            case "GET" -> showForm(exchange, 200, "", "", List.of());
            case "POST" -> signUp(exchange);
            default -> Html.sendMethodNotAllowed(exchange, "GET, POST");
        }
    }

    private void signUp(HttpExchange exchange) throws IOException {
        Map<String, String> form = Requests.readForm(exchange);
        String loginId = form.getOrDefault("loginId", "");
        String name = form.getOrDefault("name", "");
        try {
            memberService.signUp(loginId, form.getOrDefault("password", ""), name);
            Html.redirect(exchange, "/login?joined");
        } catch (SignUpException e) {
            showForm(exchange, 400, loginId, name, e.errors());
        }
    }

    private void showForm(HttpExchange exchange, int status, String loginId, String name, List<String> errors)
            throws IOException {
        view.render(exchange, status, "signup", Map.of("loginId", loginId, "name", name, "errors", errors));
    }
}
