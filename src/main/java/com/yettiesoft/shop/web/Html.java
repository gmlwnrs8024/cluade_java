package com.yettiesoft.shop.web;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;

/** 여러 핸들러가 같이 쓰는 HTML 도구: 공통 레이아웃, 특수문자 처리, 응답 보내기. */
public final class Html {

    private Html() {
    }

    /**
     * &lt; &gt; &amp; 같은 문자를 HTML에서 글자로 보이도록 바꾼다.
     * 상품 이름처럼 데이터에서 온 값은 반드시 이걸 거쳐야 한다.
     * 안 그러면 이름에 &lt;script&gt;가 들어 있을 때 브라우저가 그걸 코드로 실행한다.
     */
    public static String escape(String text) {
        StringBuilder sb = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            switch (c) {
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '&' -> sb.append("&amp;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&#39;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    /** 모든 화면에 같은 머리말(사이트 이름, 메뉴)을 붙인다. body는 이미 만들어진 HTML이어야 한다. */
    public static String page(String title, String body) {
        return """
                <!doctype html>
                <html lang="ko">
                <head>
                  <meta charset="UTF-8">
                  <title>%s - 예티 쇼핑몰</title>
                  <style>
                    body { font-family: sans-serif; max-width: 720px; margin: 0 auto; padding: 0 16px; }
                    nav { padding: 12px 0; border-bottom: 1px solid #ddd; margin-bottom: 16px; }
                    nav a { margin-right: 12px; }
                    .price { font-weight: bold; }
                  </style>
                </head>
                <body>
                  <nav><a href="/">홈</a><a href="/products">상품</a></nav>
                %s
                </body>
                </html>
                """.formatted(escape(title), body);
    }

    public static void send(HttpExchange exchange, int status, String html) throws IOException {
        byte[] body = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }

    public static void sendNotFound(HttpExchange exchange) throws IOException {
        send(exchange, 404, page("페이지 없음", """
                  <h1>404 - 페이지를 찾을 수 없습니다</h1>
                  <p><a href="/">첫 화면으로</a></p>
                """));
    }
}
