package com.yettiesoft.shop.web;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yettiesoft.shop.product.Product;
import com.yettiesoft.shop.product.ProductRepository;

/**
 * 상품 화면을 처리한다.
 * <ul>
 *   <li>/products      → 상품 목록 (templates/products/list.html)</li>
 *   <li>/products/{id} → 상품 상세 (templates/products/detail.html, 예: /products/3)</li>
 * </ul>
 */
public class ProductHandler implements HttpHandler {

    private static final String PREFIX = "/products/";

    private final ProductRepository repository;
    private final View view;

    public ProductHandler(ProductRepository repository, View view) {
        this.repository = repository;
        this.view = view;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // HttpServer는 "/products"로 시작하는 주소를 모두 여기로 보낸다("/productsabc"도 포함).
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/products") || path.equals(PREFIX)) {
            view.render(exchange, 200, "products/list", Map.of("products", repository.findAll()));
            return;
        }
        Optional<Product> product = path.startsWith(PREFIX)
                ? parseId(path.substring(PREFIX.length())).flatMap(repository::findById)
                : Optional.empty();
        if (product.isPresent()) {
            view.render(exchange, 200, "products/detail", Map.of("product", product.get()));
        } else {
            view.notFound(exchange);
        }
    }

    /** "3" → 3. 숫자가 아니면(예: "abc") 빈 값을 돌려줘서 404가 되게 한다. */
    private static Optional<Long> parseId(String text) {
        try {
            return Optional.of(Long.parseLong(text));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
