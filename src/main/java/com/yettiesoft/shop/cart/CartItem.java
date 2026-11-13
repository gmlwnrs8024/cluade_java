package com.yettiesoft.shop.cart;

import com.yettiesoft.shop.product.Product;

/** 장바구니에 담긴 상품 하나와 수량. */
public record CartItem(Product product, int quantity) {

    /** 가격 × 수량. 합계가 커질 수 있으므로 long으로 계산한다. */
    public long subtotal() {
        return (long) product.price() * quantity;
    }
}
