package com.yettiesoft.shop.product;

/**
 * 상품 하나. record는 값을 담기만 하는 클래스를 짧게 쓰는 문법이다.
 * 생성자, getter(id(), name() ...), equals/hashCode/toString을 자동으로 만들어 준다.
 *
 * @param price 원 단위 가격
 */
public record Product(long id, String name, int price, String description) {
}
