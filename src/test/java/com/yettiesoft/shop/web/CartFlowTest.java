package com.yettiesoft.shop.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.yettiesoft.shop.ShopServer;
import com.yettiesoft.shop.cart.InMemoryCartRepository;
import com.yettiesoft.shop.member.InMemoryMemberRepository;
import com.yettiesoft.shop.product.InMemoryProductRepository;
import com.yettiesoft.shop.product.Product;

/** 브라우저처럼 HTTP 요청을 보내서 장바구니 흐름을 확인한다. */
class CartFlowTest {

    private ShopServer server;
    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    void startServer() throws Exception {
        server = new ShopServer(0,
                new InMemoryProductRepository(List.of(
                        new Product(1, "무선 키보드", 39000, ""),
                        new Product(2, "무선 마우스", 25000, ""))),
                new InMemoryMemberRepository(), new InMemoryCartRepository());
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop();
    }

    @Test
    void cartRequiresLoginAndReturnsToCartAfterLogin() throws Exception {
        HttpResponse<String> cart = get("/cart", null);
        assertEquals(303, cart.statusCode());
        assertEquals("/login?next=%2Fcart", location(cart));

        HttpResponse<String> loginPage = get("/login?next=%2Fcart", null);
        assertTrue(loginPage.body().contains("name=\"next\" value=\"/cart\""));

        signUp();
        HttpResponse<String> login = post("/login", Map.of("loginId", "hong_1", "password", "password1", "next", "/cart"), null);
        assertEquals("/cart", location(login));
    }

    @Test
    void addingFromProductPageWithoutLoginReturnsToThatProduct() throws Exception {
        HttpResponse<String> add = post("/cart/add", Map.of("productId", "2", "quantity", "1"), null);

        assertEquals(303, add.statusCode());
        assertEquals("/login?next=%2Fproducts%2F2", location(add));
    }

    @Test
    void addUpdateAndRemoveItems() throws Exception {
        String cookie = signUpAndLogin();

        assertEquals("/cart", location(post("/cart/add", Map.of("productId", "1", "quantity", "2"), cookie)));
        post("/cart/add", Map.of("productId", "2", "quantity", "1"), cookie);

        String body = get("/cart", cookie).body();
        assertTrue(body.contains("무선 키보드"));
        assertTrue(body.contains("78,000원"));   // 39,000 × 2
        assertTrue(body.contains("103,000원"));  // 총 금액
        assertTrue(body.contains("장바구니 (3)"));

        post("/cart/update", Map.of("productId", "1", "quantity", "5"), cookie);
        assertTrue(get("/cart", cookie).body().contains("195,000원")); // 39,000 × 5

        post("/cart/remove", Map.of("productId", "1"), cookie);
        body = get("/cart", cookie).body();
        assertTrue(!body.contains("무선 키보드"));
        assertTrue(body.contains("장바구니 (1)"));
    }

    @Test
    void invalidInputShowsErrorOnCartPage() throws Exception {
        String cookie = signUpAndLogin();

        HttpResponse<String> badQuantity = post("/cart/add", Map.of("productId", "1", "quantity", "abc"), cookie);
        assertEquals(400, badQuantity.statusCode());
        assertTrue(badQuantity.body().contains("수량 값이 올바르지 않습니다."));

        // int 범위를 넘는 수가 엉뚱한 작은 수로 바뀌어 통과하지 않는지
        HttpResponse<String> huge = post("/cart/add", Map.of("productId", "1", "quantity", "4294967297"), cookie);
        assertEquals(400, huge.statusCode());

        HttpResponse<String> unknown = post("/cart/add", Map.of("productId", "999", "quantity", "1"), cookie);
        assertEquals(400, unknown.statusCode());
        assertTrue(unknown.body().contains("존재하지 않는 상품입니다."));
        assertTrue(unknown.body().contains("장바구니가 비어 있습니다."));
    }

    @Test
    void wrongMethodsAndPaths() throws Exception {
        String cookie = signUpAndLogin();

        assertEquals(405, get("/cart/add", cookie).statusCode());
        assertEquals(405, post("/cart", Map.of(), cookie).statusCode());
        assertEquals(404, get("/cart/nothing", cookie).statusCode());
    }

    private void signUp() throws Exception {
        post("/signup", Map.of("loginId", "hong_1", "password", "password1", "name", "홍길동"), null);
    }

    private String signUpAndLogin() throws Exception {
        signUp();
        String setCookie = post("/login", Map.of("loginId", "hong_1", "password", "password1"), null)
                .headers().firstValue("Set-Cookie").orElseThrow();
        return setCookie.substring(0, setCookie.indexOf(';'));
    }

    private static String location(HttpResponse<String> response) {
        return response.headers().firstValue("Location").orElse("");
    }

    private HttpResponse<String> get(String path, String cookie) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(uri(path));
        if (cookie != null) {
            request.header("Cookie", cookie);
        }
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, Map<String, String> form, String cookie) throws Exception {
        String body = form.entrySet().stream()
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "="
                        + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
        HttpRequest.Builder request = HttpRequest.newBuilder(uri(path))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body));
        if (cookie != null) {
            request.header("Cookie", cookie);
        }
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + server.getPort() + path);
    }
}
