package com.yettiesoft.shop.product;

import java.util.List;
import java.util.Optional;

/**
 * 상품을 저장하고 꺼내는 곳. 지금은 메모리에 두지만,
 * 나중에 DB로 바꿀 때 이 인터페이스의 구현만 바꾸면 화면 코드는 그대로 쓸 수 있다.
 */
public interface ProductRepository {

    List<Product> findAll();

    Optional<Product> findById(long id);
}
