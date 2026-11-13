package com.yettiesoft.shop.cart;

/** 장바구니 요청이 규칙에 맞지 않을 때(없는 상품, 잘못된 수량 등). 메시지는 화면에 그대로 보여준다. */
public class CartException extends RuntimeException {

    public CartException(String message) {
        super(message);
    }
}
