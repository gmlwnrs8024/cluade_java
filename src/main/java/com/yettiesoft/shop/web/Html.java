package com.yettiesoft.shop.web;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;

/** 응답을 보내는 도구. HTML 내용은 {@link View}가 템플릿으로 만든다. */
public final class Html {

    private Html() {
    }

    public static void send(HttpExchange exchange, int status, String html) throws IOException {
        byte[] body = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }

    /**
     * 브라우저에게 다른 주소로 이동하라고 응답한다(303 See Other).
     * 폼을 POST로 처리한 뒤 이걸 쓰면, 새로고침해도 폼이 다시 전송되지 않는다.
     */
    public static void redirect(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().set("Location", location);
        exchange.sendResponseHeaders(303, -1); // -1: 본문 없음
        exchange.close();
    }

    /** 이 주소가 지원하지 않는 방식(GET/POST 등)으로 요청했을 때. */
    public static void sendMethodNotAllowed(HttpExchange exchange, String allowed) throws IOException {
        exchange.getResponseHeaders().set("Allow", allowed);
        exchange.sendResponseHeaders(405, -1);
        exchange.close();
    }
}
