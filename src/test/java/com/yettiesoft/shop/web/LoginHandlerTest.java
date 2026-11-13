package com.yettiesoft.shop.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class LoginHandlerTest {

    @ParameterizedTest
    @ValueSource(strings = {"/", "/cart", "/products/3", "/login?joined"})
    void keepsPathsInsideOurSite(String next) {
        assertEquals(next, LoginHandler.safeNext(next));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "//evil.com", "https://evil.com", "evil.com", "/\\evil.com", "/a\r\nSet-Cookie: x"})
    void replacesOtherAddressesWithHome(String next) {
        assertEquals("/", LoginHandler.safeNext(next));
    }

    @Test
    void missingNextGoesHome() {
        assertEquals("/", LoginHandler.safeNext(null));
    }
}
