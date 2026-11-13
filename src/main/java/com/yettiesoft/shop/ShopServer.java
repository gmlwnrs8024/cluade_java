package com.yettiesoft.shop;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.yettiesoft.shop.cart.CartRepository;
import com.yettiesoft.shop.cart.CartService;
import com.yettiesoft.shop.member.MemberRepository;
import com.yettiesoft.shop.member.MemberService;
import com.yettiesoft.shop.product.ProductRepository;
import com.yettiesoft.shop.web.CartHandler;
import com.yettiesoft.shop.web.ErrorFilter;
import com.yettiesoft.shop.web.HomeHandler;
import com.yettiesoft.shop.web.LoginHandler;
import com.yettiesoft.shop.web.LogoutHandler;
import com.yettiesoft.shop.web.ProductHandler;
import com.yettiesoft.shop.web.SessionManager;
import com.yettiesoft.shop.web.SignUpHandler;
import com.yettiesoft.shop.web.View;

/**
 * JDK에 내장된 HttpServer로 쇼핑몰 웹 서버를 띄운다.
 * 주소(경로)마다 어떤 핸들러가 요청을 처리할지 여기서 연결한다.
 * 여러 주소가 겹치면 가장 길게 일치하는 쪽이 처리한다("/products/1"은 "/"가 아니라 "/products"로).
 */
public class ShopServer {

    private static final int THREADS = 10;

    private final HttpServer server;
    private final ExecutorService executor;

    /** port에 0을 주면 비어 있는 포트를 자동으로 고른다(테스트용). */
    public ShopServer(int port, ProductRepository productRepository, MemberRepository memberRepository,
            CartRepository cartRepository) throws IOException {
        MemberService memberService = new MemberService(memberRepository);
        CartService cartService = new CartService(cartRepository, productRepository);
        SessionManager sessionManager = new SessionManager(memberRepository);
        View view = new View(sessionManager, cartService);

        server = HttpServer.create(new InetSocketAddress(port), 0);
        addRoute("/", new HomeHandler(view));
        addRoute("/products", new ProductHandler(productRepository, view));
        addRoute("/signup", new SignUpHandler(memberService, view));
        addRoute("/login", new LoginHandler(memberService, sessionManager, view));
        addRoute("/logout", new LogoutHandler(sessionManager, view));
        addRoute("/cart", new CartHandler(cartService, sessionManager, view));

        // 기본값은 요청을 하나씩 차례로 처리한다. 로그인(비밀번호 확인)은 일부러 느리게 만든 계산이라
        // 그동안 다른 사람의 요청이 모두 기다리지 않도록 여러 스레드에서 동시에 처리한다.
        executor = Executors.newFixedThreadPool(THREADS);
        server.setExecutor(executor);
    }

    /** 주소와 핸들러를 연결하고, 오류 처리 필터를 앞에 붙인다. */
    private void addRoute(String path, HttpHandler handler) {
        server.createContext(path, handler).getFilters().add(new ErrorFilter());
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
        executor.shutdown();
    }

    public int getPort() {
        return server.getAddress().getPort();
    }
}
