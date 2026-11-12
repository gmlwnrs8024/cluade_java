package com.yettiesoft.shop.web;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * POST /logout → 로그아웃하고 첫 화면으로 이동한다.
 * GET으로 받지 않는 이유: GET은 링크나 이미지 주소만으로도 보내지므로, 다른 사이트가 몰래 로그아웃시킬 수 있다.
 */
public class LogoutHandler implements HttpHandler {

    private final SessionManager sessionManager;
    private final View view;

    public LogoutHandler(SessionManager sessionManager, View view) {
        this.sessionManager = sessionManager;
        this.view = view;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestURI().getPath().equals("/logout")) {
            view.notFound(exchange);
            return;
        }
        if (!exchange.getRequestMethod().equals("POST")) {
            Html.sendMethodNotAllowed(exchange, "POST");
            return;
        }
        sessionManager.logout(exchange);
        Html.redirect(exchange, "/");
    }
}
