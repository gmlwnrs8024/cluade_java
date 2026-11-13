package com.yettiesoft.shop.cart;

import java.util.Map;

/** 회원별 장바구니 내용(상품 번호 → 수량)을 저장한다. */
public interface CartRepository {

    /** 상품 번호 → 수량. 담은 순서대로. 비어 있으면 빈 Map. */
    Map<Long, Integer> findQuantities(long memberId);

    void saveQuantity(long memberId, long productId, int quantity);

    void delete(long memberId, long productId);

    /** 장바구니를 비운다(주문을 마친 뒤 등). */
    void deleteAll(long memberId);
}
