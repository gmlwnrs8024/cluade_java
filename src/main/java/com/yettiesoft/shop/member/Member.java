package com.yettiesoft.shop.member;

/**
 * 회원 한 명.
 *
 * @param loginId 로그인할 때 쓰는 아이디
 * @param passwordHash 비밀번호 원문이 아니라 {@link PasswordHasher}로 변환한 값
 */
public record Member(long id, String loginId, String passwordHash, String name) {

    /** 로그 등에 찍혀도 비밀번호 해시가 드러나지 않도록 기본 toString을 바꾼다. */
    @Override
    public String toString() {
        return "Member[id=" + id + ", loginId=" + loginId + ", name=" + name + "]";
    }
}
