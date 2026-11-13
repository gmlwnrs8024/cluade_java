package com.yettiesoft.shop.web;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.sun.net.httpserver.HttpExchange;

/** 요청에서 값을 꺼내는 도구: 폼 입력값, 쿠키. */
public final class Requests {

    /** 폼 하나에 이보다 큰 데이터는 받지 않는다(메모리를 지키기 위해). */
    private static final int MAX_FORM_BYTES = 16 * 1024;

    private Requests() {
    }

    /**
     * POST로 보낸 HTML 폼 값을 읽는다.
     * 브라우저는 폼을 "loginId=hong&name=%ED%99%8D" 형태로 보내므로, &와 =로 나누고 %로 바뀐 글자를 되돌린다.
     */
    public static Map<String, String> readForm(HttpExchange exchange) throws IOException {
        String body;
        try (InputStream in = exchange.getRequestBody()) {
            body = new String(in.readNBytes(MAX_FORM_BYTES), StandardCharsets.UTF_8);
        }
        return parseUrlEncoded(body);
    }

    /** 주소의 ? 뒤 값(쿼리 문자열)을 읽는다. 예: /login?joined&next=%2Fcart → {joined="", next="/cart"} */
    public static Map<String, String> readQuery(HttpExchange exchange) {
        String query = exchange.getRequestURI().getRawQuery();
        return query == null ? Map.of() : parseUrlEncoded(query);
    }

    /** 폼 본문과 쿼리 문자열은 형식이 같다: "키=값&키=값", 특수문자는 %XX로 바뀌어 있다. */
    private static Map<String, String> parseUrlEncoded(String text) {
        Map<String, String> form = new HashMap<>();
        for (String pair : text.split("&")) {
            if (pair.isEmpty()) {
                continue;
            }
            int eq = pair.indexOf('=');
            String key = eq < 0 ? pair : pair.substring(0, eq);
            String value = eq < 0 ? "" : pair.substring(eq + 1);
            form.put(URLDecoder.decode(key, StandardCharsets.UTF_8), URLDecoder.decode(value, StandardCharsets.UTF_8));
        }
        return form;
    }

    /** "Cookie: a=1; SESSION=abc" 헤더에서 name에 해당하는 값을 찾는다. */
    public static Optional<String> cookie(HttpExchange exchange, String name) {
        List<String> headers = exchange.getRequestHeaders().getOrDefault("Cookie", List.of());
        for (String header : headers) {
            for (String part : header.split(";")) {
                String trimmed = part.strip();
                if (trimmed.startsWith(name + "=")) {
                    return Optional.of(trimmed.substring(name.length() + 1));
                }
            }
        }
        return Optional.empty();
    }
}
