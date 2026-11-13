package com.yettiesoft.shop.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.yettiesoft.shop.ShopServer;
import com.yettiesoft.shop.cart.InMemoryCartRepository;
import com.yettiesoft.shop.member.InMemoryMemberRepository;
import com.yettiesoft.shop.product.InMemoryProductRepository;
import com.yettiesoft.shop.product.Product;

class ProductHandlerTest {

    private ShopServer server;
    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    void startServer() throws Exception {
        server = new ShopServer(0, new InMemoryProductRepository(List.of(
                new Product(1, "무선 키보드", 39000, "조용한 키보드"),
                new Product(2, "<script>alert(1)</script>", 1000, "a & b"))),
                new InMemoryMemberRepository(), new InMemoryCartRepository());
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop();
    }

    @Test
    void listShowsAllProductsWithLinksAndPrices() throws Exception {
        HttpResponse<String> response = get("/products");

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("<a href=\"/products/1\">무선 키보드</a>"));
        assertTrue(response.body().contains("39,000원"));
        assertTrue(response.body().contains("<a href=\"/products/2\">"));
    }

    @Test
    void detailShowsOneProduct() throws Exception {
        HttpResponse<String> response = get("/products/1");

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("<h1>무선 키보드</h1>"));
        assertTrue(response.body().contains("39,000원"));
        assertTrue(response.body().contains("조용한 키보드"));
    }

    @Test
    void productDataIsEscaped() throws Exception {
        HttpResponse<String> response = get("/products/2");

        assertFalse(response.body().contains("<script>"));
        assertTrue(response.body().contains("&lt;script&gt;alert(1)&lt;/script&gt;"));
        assertTrue(response.body().contains("a &amp; b"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/products/999", "/products/abc", "/products/1/extra", "/productsabc"})
    void unknownProductPathsReturn404(String path) throws Exception {
        assertEquals(404, get(path).statusCode());
    }

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + server.getPort() + path)).build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
