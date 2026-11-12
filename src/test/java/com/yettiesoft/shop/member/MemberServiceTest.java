package com.yettiesoft.shop.member;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class MemberServiceTest {

    private final MemberService service = new MemberService(new InMemoryMemberRepository());

    @Test
    void signUpStoresHashedPasswordAndTrimmedName() {
        Member member = service.signUp("hong_1", "password1", "  홍길동  ");

        assertEquals("hong_1", member.loginId());
        assertEquals("홍길동", member.name());
        assertNotEquals("password1", member.passwordHash());
    }

    @Test
    void duplicateLoginIdIsRejected() {
        service.signUp("hong_1", "password1", "홍길동");

        SignUpException e = assertThrows(SignUpException.class,
                () -> service.signUp("hong_1", "password2", "김철수"));
        assertEquals(List.of("이미 사용 중인 아이디입니다."), e.errors());
    }

    @Test
    void allInvalidFieldsAreReportedTogether() {
        SignUpException e = assertThrows(SignUpException.class, () -> service.signUp("a!", "short", " "));

        assertEquals(3, e.errors().size());
    }

    @Test
    void loginSucceedsOnlyWithCorrectPassword() {
        Member member = service.signUp("hong_1", "password1", "홍길동");

        assertEquals(member, service.login("hong_1", "password1").orElseThrow());
        assertTrue(service.login("hong_1", "wrong-password").isEmpty());
        assertTrue(service.login("nobody", "password1").isEmpty());
        assertTrue(service.login("hong_1", "").isEmpty());
    }
}
