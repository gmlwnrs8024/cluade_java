package com.yettiesoft.shop.web;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yettiesoft.shop.product.Product;
import com.yettiesoft.shop.product.ProductRepository;

/**
 * 상품 화면을 처리한다.
 * <ul>
 *   <li>/products      → 상품 목록</li>
 *   <li>/products/{id} → 상품 상세 (예: /products/3)</li>
 * </ul>
 */
public class ProductHandler implements HttpHandler {

    private static final String PREFIX = "/products/";

    private final ProductRepository repository;

    public ProductHandler(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // HttpServer는 "/products"로 시작하는 주소를 모두 여기로 보낸다("/productsabc"도 포함).
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/products") || path.equals(PREFIX)) {
            showList(exchange);
            return;
        }
        Optional<Product> product = path.startsWith(PREFIX)
                ? parseId(path.substring(PREFIX.length())).flatMap(repository::findById)
                : Optional.empty();
        if (product.isPresent()) {
            showDetail(exchange, product.get());
        } else {
            Html.sendNotFound(exchange);
        }
    }

    private void showList(HttpExchange exchange) throws IOException {
        List<Product> products = repository.findAll();
        StringBuilder items = new StringBuilder();
        for (Product p : products) {
            items.append("""
                        <li><a href="/products/%d">%s</a> <span class="price">%s</span></li>
                    """.formatted(p.id(), Html.escape(p.name()), formatPrice(p.price())));
        }
        String body = """
                  <h1>상품 목록</h1>
                  <ul>
                %s  </ul>
                """.formatted(items);
        Html.send(exchange, 200, Html.page("상품 목록", body));
    }

    private void showDetail(HttpExchange exchange, Product p) throws IOException {
        String body = """
                  <h1>%s</h1>
                  <p class="price">%s</p>
                  <p>%s</p>
                  <p><a href="/products">목록으로</a></p>
                """.formatted(Html.escape(p.name()), formatPrice(p.price()), Html.escape(p.description()));
        Html.send(exchange, 200, Html.page(p.name(), body));
    }

    /** "3" → 3. 숫자가 아니면(예: "abc") 빈 값을 돌려줘서 404가 되게 한다. */
    private static Optional<Long> parseId(String text) {
        try {
            return Optional.of(Long.parseLong(text));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /** 39000 → "39,000원" */
    static String formatPrice(int price) {
        // 천 단위 구분 기호는 나라마다 다르므로 한국 형식으로 고정한다.
        return String.format(Locale.KOREA, "%,d원", price);
    }
}
