package com.yettiesoft.shop.member;

import java.util.Optional;

public interface MemberRepository {

    /** 새 회원을 저장하고, 번호(id)가 붙은 회원을 돌려준다. */
    Member create(String loginId, String passwordHash, String name);

    Optional<Member> findById(long id);

    Optional<Member> findByLoginId(String loginId);
}
