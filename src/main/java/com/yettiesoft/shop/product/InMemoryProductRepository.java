package com.yettiesoft.shop.product;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** 상품을 메모리(Map)에 보관한다. 프로그램을 끄면 사라진다. */
public class InMemoryProductRepository implements ProductRepository {

    // LinkedHashMap은 넣은 순서를 기억하므로 목록이 항상 같은 순서로 나온다.
    private final Map<Long, Product> products = new LinkedHashMap<>();

    public InMemoryProductRepository(List<Product> initialProducts) {
        for (Product product : initialProducts) {
            products.put(product.id(), product);
        }
    }

    public static InMemoryProductRepository withSampleData() {
        return new InMemoryProductRepository(List.of(
                new Product(1, "무선 키보드", 39000, "조용한 키감의 블루투스 키보드입니다."),
                new Product(2, "무선 마우스", 25000, "손에 잘 맞는 가벼운 마우스입니다."),
                new Product(3, "27인치 모니터", 289000, "QHD 해상도의 IPS 모니터입니다."),
                new Product(4, "USB-C 허브", 45000, "HDMI, USB-A 3개, SD 카드 슬롯을 지원합니다.")));
    }

    @Override
    public List<Product> findAll() {
        return List.copyOf(products.values());
    }

    @Override
    public Optional<Product> findById(long id) {
        return Optional.ofNullable(products.get(id));
    }
}
