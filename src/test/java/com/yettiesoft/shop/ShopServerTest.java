package com.yettiesoft.shop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.yettiesoft.shop.member.InMemoryMemberRepository;
import com.yettiesoft.shop.product.InMemoryProductRepository;

class ShopServerTest {

    private ShopServer server;
    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    void startServer() throws Exception {
        server = new ShopServer(0, new InMemoryProductRepository(List.of()), new InMemoryMemberRepository());
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop();
    }

    @Test
    void homePageReturnsHtml() throws Exception {
        HttpResponse<String> response = get("/");

        assertEquals(200, response.statusCode());
        assertEquals("text/html; charset=UTF-8", response.headers().firstValue("Content-Type").orElse(""));
        assertTrue(response.body().contains("<h1>예티 쇼핑몰</h1>"));
    }

    @Test
    void unknownPathReturns404() throws Exception {
        HttpResponse<String> response = get("/no-such-page");

        assertEquals(404, response.statusCode());
    }

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + server.getPort() + path)).build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
