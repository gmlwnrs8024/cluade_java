package com.yettiesoft.shop;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;
import com.yettiesoft.shop.product.ProductRepository;
import com.yettiesoft.shop.web.HomeHandler;
import com.yettiesoft.shop.web.ProductHandler;

/**
 * JDK에 내장된 HttpServer로 쇼핑몰 웹 서버를 띄운다.
 * 주소(경로)마다 어떤 핸들러가 요청을 처리할지 여기서 연결한다.
 * 여러 주소가 겹치면 가장 길게 일치하는 쪽이 처리한다("/products/1"은 "/"가 아니라 "/products"로).
 */
public class ShopServer {

    private final HttpServer server;

    /** port에 0을 주면 비어 있는 포트를 자동으로 고른다(테스트용). */
    public ShopServer(int port, ProductRepository productRepository) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new HomeHandler());
        server.createContext("/products", new ProductHandler(productRepository));
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    public int getPort() {
        return server.getAddress().getPort();
    }
}
