package com.yettiesoft.shop.cart;

import java.util.List;

/** 한 회원의 장바구니. 화면에 보여줄 때 쓰는 읽기 전용 값이다. */
public record Cart(List<CartItem> items) {

    public Cart {
        items = List.copyOf(items);
    }

    public long totalPrice() {
        return items.stream().mapToLong(CartItem::subtotal).sum();
    }

    /** 담긴 상품 개수의 합. 메뉴의 "장바구니 (3)"에 쓴다. */
    public int totalQuantity() {
        return items.stream().mapToInt(CartItem::quantity).sum();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
