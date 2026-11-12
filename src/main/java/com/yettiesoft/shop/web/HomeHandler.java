package com.yettiesoft.shop.web;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * 첫 화면("/"). 화면 내용은 templates/home.html.
 * "/"로 등록하면 다른 핸들러에 연결되지 않은 모든 주소가 여기로 들어오므로, "/"가 아닌 주소는 404로 응답한다.
 */
public class HomeHandler implements HttpHandler {

    private final View view;

    public HomeHandler(View view) {
        this.view = view;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (exchange.getRequestURI().getPath().equals("/")) {
            view.render(exchange, 200, "home");
        } else {
            view.notFound(exchange);
        }
    }
}
