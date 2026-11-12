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
 * 로그인.
 * <ul>
 *   <li>GET  /login → 로그인 폼 (/login?joined 이면 가입 완료 안내도 보여준다)</li>
 *   <li>POST /login → 로그인 처리. 성공하면 세션 쿠키를 주고 첫 화면으로 이동한다.</li>
 * </ul>
 */
public class LoginHandler implements HttpHandler {

    private final MemberService memberService;
    private final SessionManager sessionManager;

    public LoginHandler(MemberService memberService, SessionManager sessionManager) {
        this.memberService = memberService;
        this.sessionManager = sessionManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Optional<Member> loginMember = sessionManager.currentMember(exchange);
        if (!exchange.getRequestURI().getPath().equals("/login")) {
            Html.sendNotFound(exchange, loginMember);
            return;
        }
        switch (exchange.getRequestMethod()) {
            case "GET" -> {
                boolean joined = "joined".equals(exchange.getRequestURI().getQuery());
                showForm(exchange, loginMember, 200, "", joined, List.of());
            }
            case "POST" -> login(exchange, loginMember);
            default -> Html.sendMethodNotAllowed(exchange, "GET, POST");
        }
    }

    private void login(HttpExchange exchange, Optional<Member> loginMember) throws IOException {
        Map<String, String> form = Requests.readForm(exchange);
        String loginId = form.getOrDefault("loginId", "");
        Optional<Member> member = memberService.login(loginId, form.getOrDefault("password", ""));
        if (member.isPresent()) {
            sessionManager.login(exchange, member.get());
            Html.redirect(exchange, "/");
        } else {
            // 아이디가 없는지 비밀번호가 틀렸는지 구분해서 알려주면, 가입된 아이디를 알아내는 데 쓰일 수 있다.
            showForm(exchange, loginMember, 401, loginId, false, List.of("아이디 또는 비밀번호가 올바르지 않습니다."));
        }
    }

    private void showForm(HttpExchange exchange, Optional<Member> loginMember, int status,
            String loginId, boolean joined, List<String> errors) throws IOException {
        String notice = joined ? "  <p class=\"notice\">회원가입이 완료되었습니다. 로그인해 주세요.</p>\n" : "";
        String body = """
                  <h1>로그인</h1>
                %s%s  <form method="post" action="/login">
                    <label>아이디 <input name="loginId" value="%s" required></label>
                    <label>비밀번호 <input name="password" type="password" required></label>
                    <button>로그인</button>
                  </form>
                  <p>아직 회원이 아니신가요? <a href="/signup">회원가입</a></p>
                """.formatted(notice, Html.errorList(errors), Html.escape(loginId));
        Html.send(exchange, status, Html.page(loginMember, "로그인", body));
    }
}
