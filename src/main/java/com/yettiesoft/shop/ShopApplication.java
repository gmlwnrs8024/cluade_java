package com.yettiesoft.shop;

import java.io.IOException;

public class ShopApplication {

    public static void main(String[] args) throws IOException {
        AppConfig config = AppConfig.load();
        ShopServer server = new ShopServer(config.getInt("server.port"));
        server.start();
        System.out.println("쇼핑몰 서버 시작: http://localhost:" + server.getPort());
    }
}
