package com.yettiesoft.shop.web;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.sun.net.httpserver.HttpExchange;
import com.yettiesoft.shop.cart.CartService;
import com.yettiesoft.shop.member.Member;

/**
 * Thymeleaf 템플릿(src/main/resources/templates/*.html)에 값을 채워 HTML 응답을 보낸다.
 * <p>
 * 모든 화면 위쪽 메뉴에 로그인 상태를 보여줘야 하므로, 로그인한 회원(loginMember)과
 * 장바구니 개수(cartCount)는 여기서 자동으로 넣는다.
 * 템플릿의 th:text는 &lt; &gt; 같은 특수문자를 자동으로 바꿔 주므로 직접 escape할 필요가 없다.
 */
public class View {

    private final TemplateEngine engine = new TemplateEngine();
    private final SessionManager sessionManager;
    private final CartService cartService;

    public View(SessionManager sessionManager, CartService cartService) {
        this.sessionManager = sessionManager;
        this.cartService = cartService;

        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/"); // render("home") → templates/home.html
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        engine.setTemplateResolver(resolver);
    }

    public void render(HttpExchange exchange, int status, String template) throws IOException {
        render(exchange, status, template, Map.of());
    }

    public void render(HttpExchange exchange, int status, String template, Map<String, ?> variables)
            throws IOException {
        Context context = new Context();
        variables.forEach(context::setVariable);
        Optional<Member> loginMember = sessionManager.currentMember(exchange);
        context.setVariable("loginMember", loginMember.orElse(null));
        // 메뉴의 "장바구니 (3)"에 쓸 개수
        context.setVariable("cartCount",
                loginMember.map(member -> cartService.getCart(member.id()).totalQuantity()).orElse(0));
        Html.send(exchange, status, engine.process(template, context));
    }

    public void notFound(HttpExchange exchange) throws IOException {
        render(exchange, 404, "error/404");
    }
}
