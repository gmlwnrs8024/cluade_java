package com.yettiesoft.shop.web;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import com.sun.net.httpserver.HttpExchange;
import com.yettiesoft.shop.member.Member;

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

    /**
     * 모든 화면에 같은 머리말(사이트 이름, 메뉴, 로그인 상태)을 붙인다. body는 이미 만들어진 HTML이어야 한다.
     *
     * @param loginMember 로그인한 회원. 로그인하지 않았으면 빈 값
     */
    public static String page(Optional<Member> loginMember, String title, String body) {
        String account = loginMember
                .map(member -> """
                        <span>%s님</span>
                        <form method="post" action="/logout" class="inline"><button>로그아웃</button></form>"""
                        .formatted(escape(member.name())))
                .orElse("""
                        <a href="/login">로그인</a><a href="/signup">회원가입</a>""");
        return """
                <!doctype html>
                <html lang="ko">
                <head>
                  <meta charset="UTF-8">
                  <title>%s - 예티 쇼핑몰</title>
                  <style>
                    body { font-family: sans-serif; max-width: 720px; margin: 0 auto; padding: 0 16px; }
                    nav { display: flex; gap: 12px; align-items: center; padding: 12px 0; border-bottom: 1px solid #ddd; margin-bottom: 16px; }
                    nav .account { margin-left: auto; display: flex; gap: 12px; align-items: center; }
                    form.inline { display: inline; margin: 0; }
                    .price { font-weight: bold; }
                    .errors { color: #c00; }
                    .notice { color: #070; }
                    label { display: block; margin: 8px 0; }
                  </style>
                </head>
                <body>
                  <nav><a href="/">홈</a><a href="/products">상품</a><span class="account">%s</span></nav>
                %s
                </body>
                </html>
                """.formatted(escape(title), account, body);
    }

    /** 오류 메시지 목록을 빨간 글씨 목록으로 만든다. 없으면 빈 문자열. */
    public static String errorList(List<String> errors) {
        if (errors.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder("  <ul class=\"errors\">\n");
        for (String error : errors) {
            sb.append("    <li>").append(escape(error)).append("</li>\n");
        }
        return sb.append("  </ul>\n").toString();
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

    public static void sendNotFound(HttpExchange exchange, Optional<Member> loginMember) throws IOException {
        send(exchange, 404, page(loginMember, "페이지 없음", """
                  <h1>404 - 페이지를 찾을 수 없습니다</h1>
                  <p><a href="/">첫 화면으로</a></p>
                """));
    }

    /** 이 주소가 지원하지 않는 방식(GET/POST 등)으로 요청했을 때. */
    public static void sendMethodNotAllowed(HttpExchange exchange, String allowed) throws IOException {
        exchange.getResponseHeaders().set("Allow", allowed);
        exchange.sendResponseHeaders(405, -1);
        exchange.close();
    }
}
