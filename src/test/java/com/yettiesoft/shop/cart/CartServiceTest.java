package com.yettiesoft.shop.cart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.yettiesoft.shop.product.InMemoryProductRepository;
import com.yettiesoft.shop.product.Product;

class CartServiceTest {

    private static final long MEMBER = 1;
    private static final long OTHER_MEMBER = 2;

    private final CartService service = new CartService(new InMemoryCartRepository(),
            new InMemoryProductRepository(List.of(
                    new Product(1, "키보드", 39000, ""),
                    new Product(2, "마우스", 25000, ""))));

    @Test
    void addingSameProductTwiceAddsQuantities() {
        service.add(MEMBER, 1, 2);
        service.add(MEMBER, 1, 3);

        Cart cart = service.getCart(MEMBER);
        assertEquals(1, cart.items().size());
        assertEquals(5, cart.items().get(0).quantity());
    }

    @Test
    void totalsAddUpAllItems() {
        service.add(MEMBER, 1, 2); // 78,000
        service.add(MEMBER, 2, 1); // 25,000

        Cart cart = service.getCart(MEMBER);
        assertEquals(103000, cart.totalPrice());
        assertEquals(3, cart.totalQuantity());
        assertEquals(List.of("키보드", "마우스"), cart.items().stream().map(i -> i.product().name()).toList());
    }

    @Test
    void cartsAreSeparatedPerMember() {
        service.add(MEMBER, 1, 1);

        assertTrue(service.getCart(OTHER_MEMBER).isEmpty());
    }

    @Test
    void rejectsUnknownProductAndBadQuantities() {
        assertThrows(CartException.class, () -> service.add(MEMBER, 999, 1));
        assertThrows(CartException.class, () -> service.add(MEMBER, 1, 0));
        assertThrows(CartException.class, () -> service.add(MEMBER, 1, -1));
        assertThrows(CartException.class, () -> service.add(MEMBER, 1, 100));
        assertTrue(service.getCart(MEMBER).isEmpty());
    }

    @Test
    void totalQuantityOfOneProductCannotExceedMax() {
        service.add(MEMBER, 1, 90);

        CartException e = assertThrows(CartException.class, () -> service.add(MEMBER, 1, 10));
        assertEquals("한 상품은 최대 99개까지 담을 수 있습니다.", e.getMessage());
        assertEquals(90, service.getCart(MEMBER).totalQuantity());
    }

    @Test
    void changeQuantityToZeroRemovesItem() {
        service.add(MEMBER, 1, 2);
        service.changeQuantity(MEMBER, 1, 7);
        assertEquals(7, service.getCart(MEMBER).totalQuantity());

        service.changeQuantity(MEMBER, 1, 0);
        assertTrue(service.getCart(MEMBER).isEmpty());
    }

    @Test
    void changeQuantityOfItemNotInCartFails() {
        assertThrows(CartException.class, () -> service.changeQuantity(MEMBER, 1, 3));
    }

    @Test
    void removeDeletesOnlyThatItem() {
        service.add(MEMBER, 1, 1);
        service.add(MEMBER, 2, 1);

        service.remove(MEMBER, 1);

        assertEquals("마우스", service.getCart(MEMBER).items().get(0).product().name());
        assertEquals(1, service.getCart(MEMBER).items().size());
    }
}
