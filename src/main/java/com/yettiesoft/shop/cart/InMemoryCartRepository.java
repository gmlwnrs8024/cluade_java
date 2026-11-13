package com.yettiesoft.shop.cart;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 장바구니를 메모리에 보관한다. 프로그램을 끄면 사라진다.
 * 모든 메서드에 synchronized를 붙여, 여러 요청이 동시에 와도 한 번에 하나씩만 내용을 바꾸게 한다.
 */
public class InMemoryCartRepository implements CartRepository {

    // 회원 번호 → (상품 번호 → 수량). 안쪽은 담은 순서를 기억하는 LinkedHashMap.
    private final Map<Long, Map<Long, Integer>> carts = new HashMap<>();

    @Override
    public synchronized Map<Long, Integer> findQuantities(long memberId) {
        Map<Long, Integer> cart = carts.getOrDefault(memberId, Map.of());
        // 복사본을 돌려준다. 원본을 그대로 주면 밖에서 바꿀 수 있고, 동시에 읽고 쓰다 오류가 날 수 있다.
        return Collections.unmodifiableMap(new LinkedHashMap<>(cart));
    }

    @Override
    public synchronized void saveQuantity(long memberId, long productId, int quantity) {
        carts.computeIfAbsent(memberId, id -> new LinkedHashMap<>()).put(productId, quantity);
    }

    @Override
    public synchronized void delete(long memberId, long productId) {
        Map<Long, Integer> cart = carts.get(memberId);
        if (cart != null) {
            cart.remove(productId);
        }
    }

    @Override
    public synchronized void deleteAll(long memberId) {
        carts.remove(memberId);
    }
}
