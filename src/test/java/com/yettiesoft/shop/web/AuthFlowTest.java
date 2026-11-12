package com.yettiesoft.shop.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import com.yettiesoft.shop.member.InMemoryMemberRepository;
import com.yettiesoft.shop.product.InMemoryProductRepository;

/** 브라우저처럼 실제 HTTP 요청을 보내서 회원가입 → 로그인 → 로그아웃 흐름을 확인한다. */
class AuthFlowTest {

    private ShopServer server;
    // 리다이렉트를 자동으로 따라가지 않는다(기본값). 303 응답과 쿠키를 직접 확인하기 위해서다.
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
    void signUpLoginAndLogout() throws Exception {
        HttpResponse<String> signUp = post("/signup", Map.of("loginId", "hong_1", "password", "password1", "name", "홍길동"), null);
        assertEquals(303, signUp.statusCode());
        assertEquals("/login?joined", signUp.headers().firstValue("Location").orElse(""));

        HttpResponse<String> joinedPage = get("/login?joined", null);
        assertTrue(joinedPage.body().contains("회원가입이 완료되었습니다"));

        HttpResponse<String> login = post("/login", Map.of("loginId", "hong_1", "password", "password1"), null);
        assertEquals(303, login.statusCode());
        String setCookie = login.headers().firstValue("Set-Cookie").orElse("");
        assertTrue(setCookie.startsWith("SESSION="));
        assertTrue(setCookie.contains("HttpOnly"));
        String cookie = setCookie.substring(0, setCookie.indexOf(';'));

        HttpResponse<String> home = get("/", cookie);
        assertTrue(home.body().contains("홍길동님, 환영합니다."));
        assertTrue(home.body().contains("로그아웃"));

        HttpResponse<String> logout = post("/logout", Map.of(), cookie);
        assertEquals(303, logout.statusCode());
        assertTrue(logout.headers().firstValue("Set-Cookie").orElse("").contains("Max-Age=0"));

        // 로그아웃한 뒤에는 같은 쿠키를 보내도 로그인 상태가 아니다.
        HttpResponse<String> afterLogout = get("/", cookie);
        assertFalse(afterLogout.body().contains("홍길동님"));
        assertTrue(afterLogout.body().contains("href=\"/login\""));
    }

    @Test
    void wrongPasswordShowsErrorWithoutCookie() throws Exception {
        post("/signup", Map.of("loginId", "hong_1", "password", "password1", "name", "홍길동"), null);

        HttpResponse<String> login = post("/login", Map.of("loginId", "hong_1", "password", "nope-nope"), null);

        assertEquals(401, login.statusCode());
        assertTrue(login.body().contains("아이디 또는 비밀번호가 올바르지 않습니다."));
        assertTrue(login.headers().firstValue("Set-Cookie").isEmpty());
    }

    @Test
    void invalidSignUpShowsErrorsAndKeepsInputEscaped() throws Exception {
        HttpResponse<String> response = post("/signup", Map.of("loginId", "<b>", "password", "short", "name", "<i>홍</i>"), null);

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("아이디는 영문, 숫자, _ 로 4~20자여야 합니다."));
        assertTrue(response.body().contains("value=\"&lt;i&gt;홍&lt;/i&gt;\""));
        assertFalse(response.body().contains("<i>홍</i>"));
    }

    @Test
    void unknownSessionCookieIsTreatedAsLoggedOut() throws Exception {
        HttpResponse<String> home = get("/", "SESSION=made-up-value");

        assertEquals(200, home.statusCode());
        assertTrue(home.body().contains("href=\"/login\""));
    }

    @Test
    void logoutRequiresPost() throws Exception {
        assertEquals(405, get("/logout", null).statusCode());
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
