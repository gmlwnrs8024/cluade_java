package com.yettiesoft.shop.web;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * 첫 화면("/")을 HTML로 만들어 돌려준다.
 * "/"로 등록하면 모든 주소가 여기로 들어오므로, "/"가 아닌 주소는 404로 응답한다.
 */
public class HomeHandler implements HttpHandler {

    private static final String HOME_HTML = """
            <!doctype html>
            <html lang="ko">
            <head>
              <meta charset="UTF-8">
              <title>예티 쇼핑몰</title>
            </head>
            <body>
              <h1>예티 쇼핑몰</h1>
              <p>순수 Java로 만든 쇼핑몰입니다.</p>
            </body>
            </html>
            """;

    private static final String NOT_FOUND_HTML = """
            <!doctype html>
            <html lang="ko">
            <head><meta charset="UTF-8"><title>페이지 없음</title></head>
            <body><h1>404 - 페이지를 찾을 수 없습니다</h1><p><a href="/">첫 화면으로</a></p></body>
            </html>
            """;

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (exchange.getRequestURI().getPath().equals("/")) {
            sendHtml(exchange, 200, HOME_HTML);
        } else {
            sendHtml(exchange, 404, NOT_FOUND_HTML);
        }
    }

    private static void sendHtml(HttpExchange exchange, int status, String html) throws IOException {
        byte[] body = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }
}
