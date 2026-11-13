package com.yettiesoft.shop.web;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yettiesoft.shop.cart.CartException;
import com.yettiesoft.shop.cart.CartService;
import com.yettiesoft.shop.member.Member;

/**
 * 장바구니. 로그인한 회원만 쓸 수 있다. 화면은 templates/cart.html.
 * <ul>
 *   <li>GET  /cart        → 장바구니 보기</li>
 *   <li>POST /cart/add    → 담기 (productId, quantity)</li>
 *   <li>POST /cart/update → 수량 바꾸기 (productId, quantity)</li>
 *   <li>POST /cart/remove → 빼기 (productId)</li>
 * </ul>
 * HTML 폼은 GET과 POST만 보낼 수 있어서, 하는 일을 주소로 구분한다.
 */
public class CartHandler implements HttpHandler {

    private final CartService cartService;
    private final SessionManager sessionManager;
    private final View view;

    public CartHandler(CartService cartService, SessionManager sessionManager, View view) {
        this.cartService = cartService;
        this.sessionManager = sessionManager;
        this.view = view;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        switch (path) {
            case "/cart" -> {
                if (!method.equals("GET")) {
                    Html.sendMethodNotAllowed(exchange, "GET");
                    return;
                }
                Optional<Member> member = sessionManager.currentMember(exchange);
                if (member.isEmpty()) {
                    redirectToLogin(exchange, "/cart");
                    return;
                }
                showCart(exchange, 200, member.get(), List.of());
            }
            case "/cart/add", "/cart/update", "/cart/remove" -> {
                if (!method.equals("POST")) {
                    Html.sendMethodNotAllowed(exchange, "POST");
                    return;
                }
                change(exchange, path);
            }
            default -> view.notFound(exchange);
        }
    }

    private void change(HttpExchange exchange, String path) throws IOException {
        Map<String, String> form = Requests.readForm(exchange);
        Optional<Member> member = sessionManager.currentMember(exchange);
        if (member.isEmpty()) {
            // 상품 화면에서 담기를 눌렀다면, 로그인한 뒤 그 상품 화면으로 돌아가게 한다.
            String productId = form.getOrDefault("productId", "");
            redirectToLogin(exchange, productId.matches("\\d+") ? "/products/" + productId : "/cart");
            return;
        }
        long memberId = member.get().id();
        try {
            long productId = parseNumber(form.get("productId"), "상품 번호");
            switch (path) {
                case "/cart/add" -> cartService.add(memberId, productId, parseQuantity(form.get("quantity")));
                case "/cart/update" -> cartService.changeQuantity(memberId, productId, parseQuantity(form.get("quantity")));
                default -> cartService.remove(memberId, productId);
            }
            Html.redirect(exchange, "/cart");
        } catch (CartException e) {
            showCart(exchange, 400, member.get(), List.of(e.getMessage()));
        }
    }

    private void showCart(HttpExchange exchange, int status, Member member, List<String> errors) throws IOException {
        view.render(exchange, status, "cart", Map.of("cart", cartService.getCart(member.id()), "errors", errors));
    }

    private static void redirectToLogin(HttpExchange exchange, String next) throws IOException {
        Html.redirect(exchange, "/login?next=" + URLEncoder.encode(next, StandardCharsets.UTF_8));
    }

    /** 폼에서 받은 글자를 숫자로 바꾼다. 비었거나 숫자가 아니면 화면에 보여줄 오류로 바꾼다. */
    private static long parseNumber(String text, String label) {
        try {
            return Long.parseLong(text == null ? "" : text.strip());
        } catch (NumberFormatException e) {
            throw new CartException(label + " 값이 올바르지 않습니다.");
        }
    }

    /**
     * 수량은 int 범위를 넘으면 안 된다. long으로 읽어서 (int)로 바꾸면 9999999999 같은 값이
     * 엉뚱한 작은 수로 바뀌어 통과할 수 있으므로, 처음부터 int로 읽는다.
     */
    private static int parseQuantity(String text) {
        try {
            return Integer.parseInt(text == null ? "" : text.strip());
        } catch (NumberFormatException e) {
            throw new CartException("수량 값이 올바르지 않습니다.");
        }
    }
}
