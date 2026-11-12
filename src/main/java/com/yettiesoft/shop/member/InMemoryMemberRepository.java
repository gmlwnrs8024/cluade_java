package com.yettiesoft.shop.member;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 회원을 메모리에 보관한다. 프로그램을 끄면 사라진다.
 * 여러 요청이 동시에 들어올 수 있으므로 동시 접근에 안전한 ConcurrentHashMap, AtomicLong을 쓴다.
 */
public class InMemoryMemberRepository implements MemberRepository {

    private final Map<Long, Member> members = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public Member create(String loginId, String passwordHash, String name) {
        Member member = new Member(nextId.getAndIncrement(), loginId, passwordHash, name);
        members.put(member.id(), member);
        return member;
    }

    @Override
    public Optional<Member> findById(long id) {
        return Optional.ofNullable(members.get(id));
    }

    @Override
    public Optional<Member> findByLoginId(String loginId) {
        return members.values().stream()
                .filter(m -> m.loginId().equals(loginId))
                .findFirst();
    }
}
