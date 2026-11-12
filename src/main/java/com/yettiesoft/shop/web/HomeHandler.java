package com.yettiesoft.shop.web;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * 첫 화면("/")을 HTML로 만들어 돌려준다.
 * "/"로 등록하면 다른 핸들러에 연결되지 않은 모든 주소가 여기로 들어오므로, "/"가 아닌 주소는 404로 응답한다.
 */
public class HomeHandler implements HttpHandler {

    private static final String HOME_BODY = """
              <h1>예티 쇼핑몰</h1>
              <p>순수 Java로 만든 쇼핑몰입니다.</p>
              <p><a href="/products">상품 보러 가기</a></p>
            """;

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (exchange.getRequestURI().getPath().equals("/")) {
            Html.send(exchange, 200, Html.page("홈", HOME_BODY));
        } else {
            Html.sendNotFound(exchange);
        }
    }
}
