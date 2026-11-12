package com.yettiesoft.shop.web;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yettiesoft.shop.member.Member;
import com.yettiesoft.shop.member.MemberService;
import com.yettiesoft.shop.member.SignUpException;

/**
 * 회원가입.
 * <ul>
 *   <li>GET  /signup → 가입 폼</li>
 *   <li>POST /signup → 가입 처리. 성공하면 로그인 화면으로 이동, 실패하면 오류와 함께 폼을 다시 보여준다.</li>
 * </ul>
 */
public class SignUpHandler implements HttpHandler {

    private final MemberService memberService;
    private final SessionManager sessionManager;

    public SignUpHandler(MemberService memberService, SessionManager sessionManager) {
        this.memberService = memberService;
        this.sessionManager = sessionManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Optional<Member> loginMember = sessionManager.currentMember(exchange);
        if (!exchange.getRequestURI().getPath().equals("/signup")) {
            Html.sendNotFound(exchange, loginMember);
            return;
        }
        switch (exchange.getRequestMethod()) {
            case "GET" -> showForm(exchange, loginMember, 200, "", "", List.of());
            case "POST" -> signUp(exchange, loginMember);
            default -> Html.sendMethodNotAllowed(exchange, "GET, POST");
        }
    }

    private void signUp(HttpExchange exchange, Optional<Member> loginMember) throws IOException {
        Map<String, String> form = Requests.readForm(exchange);
        String loginId = form.getOrDefault("loginId", "");
        String password = form.getOrDefault("password", "");
        String name = form.getOrDefault("name", "");
        try {
            memberService.signUp(loginId, password, name);
            Html.redirect(exchange, "/login?joined");
        } catch (SignUpException e) {
            // 입력했던 아이디와 이름은 다시 채워 준다. 비밀번호는 화면에 다시 내보내지 않는다.
            showForm(exchange, loginMember, 400, loginId, name, e.errors());
        }
    }

    private void showForm(HttpExchange exchange, Optional<Member> loginMember, int status,
            String loginId, String name, List<String> errors) throws IOException {
        String body = """
                  <h1>회원가입</h1>
                %s  <form method="post" action="/signup">
                    <label>아이디 <input name="loginId" value="%s" required></label>
                    <label>비밀번호 <input name="password" type="password" required></label>
                    <label>이름 <input name="name" value="%s" required></label>
                    <button>가입하기</button>
                  </form>
                """.formatted(Html.errorList(errors), Html.escape(loginId), Html.escape(name));
        Html.send(exchange, status, Html.page(loginMember, "회원가입", body));
    }
}
