package com.yettiesoft.shop.web;

import java.io.IOException;
import java.util.Optional;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yettiesoft.shop.member.Member;

/**
 * 첫 화면("/")을 HTML로 만들어 돌려준다.
 * "/"로 등록하면 다른 핸들러에 연결되지 않은 모든 주소가 여기로 들어오므로, "/"가 아닌 주소는 404로 응답한다.
 */
public class HomeHandler implements HttpHandler {

    private final SessionManager sessionManager;

    public HomeHandler(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Optional<Member> loginMember = sessionManager.currentMember(exchange);
        if (!exchange.getRequestURI().getPath().equals("/")) {
            Html.sendNotFound(exchange, loginMember);
            return;
        }
        String greeting = loginMember
                .map(member -> Html.escape(member.name()) + "님, 환영합니다.")
                .orElse("순수 Java로 만든 쇼핑몰입니다.");
        String body = """
                  <h1>예티 쇼핑몰</h1>
                  <p>%s</p>
                  <p><a href="/products">상품 보러 가기</a></p>
                """.formatted(greeting);
        Html.send(exchange, 200, Html.page(loginMember, "홈", body));
    }
}
