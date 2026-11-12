package com.yettiesoft.shop.web;

import java.io.IOException;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

/**
 * 모든 요청 앞에서 실행되어, 핸들러에서 예외가 나면 오류 내용을 출력하고 500 응답을 보낸다.
 * <p>
 * 이게 없으면 HttpServer는 예외를 조용히 삼키고 연결만 끊어서, 브라우저에는 빈 화면이 나오고
 * 콘솔에도 아무것도 남지 않아 원인을 찾기 어렵다.
 */
public class ErrorFilter extends Filter {

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        try {
            chain.doFilter(exchange);
        } catch (RuntimeException | IOException e) {
            System.err.println("요청 처리 중 오류: " + exchange.getRequestMethod() + " " + exchange.getRequestURI());
            e.printStackTrace();
            // 이미 응답 헤더를 보낸 뒤라면(getResponseCode가 -1이 아니면) 상태 코드를 바꿀 수 없다.
            if (exchange.getResponseCode() == -1) {
                exchange.sendResponseHeaders(500, -1);
            }
            exchange.close();
        }
    }

    @Override
    public String description() {
        return "예외가 나면 500으로 응답";
    }
}
