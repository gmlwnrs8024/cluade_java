package com.yettiesoft.shop.cart;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.yettiesoft.shop.product.ProductRepository;

/** 장바구니 규칙: 담기, 수량 바꾸기, 빼기. */
public class CartService {

    public static final int MAX_QUANTITY = 99;

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    /** 상품을 담는다. 이미 담긴 상품이면 수량을 더한다. */
    public synchronized void add(long memberId, long productId, int quantity) {
        checkProductExists(productId);
        checkQuantity(quantity);
        int current = cartRepository.findQuantities(memberId).getOrDefault(productId, 0);
        if (current + quantity > MAX_QUANTITY) {
            throw new CartException("한 상품은 최대 " + MAX_QUANTITY + "개까지 담을 수 있습니다.");
        }
        cartRepository.saveQuantity(memberId, productId, current + quantity);
    }

    /** 담긴 상품의 수량을 바꾼다. 0으로 바꾸면 장바구니에서 뺀다. */
    public synchronized void changeQuantity(long memberId, long productId, int quantity) {
        if (!cartRepository.findQuantities(memberId).containsKey(productId)) {
            throw new CartException("장바구니에 없는 상품입니다.");
        }
        if (quantity == 0) {
            cartRepository.delete(memberId, productId);
            return;
        }
        checkQuantity(quantity);
        cartRepository.saveQuantity(memberId, productId, quantity);
    }

    public synchronized void remove(long memberId, long productId) {
        cartRepository.delete(memberId, productId);
    }

    public Cart getCart(long memberId) {
        List<CartItem> items = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : cartRepository.findQuantities(memberId).entrySet()) {
            // 담은 뒤에 판매가 중단된 상품은 목록에서 빠진다.
            productRepository.findById(entry.getKey())
                    .ifPresent(product -> items.add(new CartItem(product, entry.getValue())));
        }
        return new Cart(items);
    }

    private void checkProductExists(long productId) {
        if (productRepository.findById(productId).isEmpty()) {
            throw new CartException("존재하지 않는 상품입니다.");
        }
    }

    private static void checkQuantity(int quantity) {
        if (quantity < 1 || quantity > MAX_QUANTITY) {
            throw new CartException("수량은 1~" + MAX_QUANTITY + "개여야 합니다.");
        }
    }
}
