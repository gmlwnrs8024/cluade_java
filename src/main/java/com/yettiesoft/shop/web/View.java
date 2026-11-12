package com.yettiesoft.shop.web;

import java.io.IOException;
import java.util.Map;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.sun.net.httpserver.HttpExchange;

/**
 * Thymeleaf 템플릿(src/main/resources/templates/*.html)에 값을 채워 HTML 응답을 보낸다.
 * <p>
 * 모든 화면 위쪽 메뉴에 로그인 상태를 보여줘야 하므로, 로그인한 회원(loginMember)은 여기서 자동으로 넣는다.
 * 템플릿의 th:text는 &lt; &gt; 같은 특수문자를 자동으로 바꿔 주므로 직접 escape할 필요가 없다.
 */
public class View {

    private final TemplateEngine engine = new TemplateEngine();
    private final SessionManager sessionManager;

    public View(SessionManager sessionManager) {
        this.sessionManager = sessionManager;

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
        context.setVariable("loginMember", sessionManager.currentMember(exchange).orElse(null));
        Html.send(exchange, status, engine.process(template, context));
    }

    public void notFound(HttpExchange exchange) throws IOException {
        render(exchange, 404, "error/404");
    }
}
