package com.yettiesoft.shop.member;

import java.util.List;

/** 회원가입 입력값이 잘못됐을 때. 화면에 한꺼번에 보여줄 수 있도록 오류 메시지를 모두 담는다. */
public class SignUpException extends RuntimeException {

    private final List<String> errors;

    public SignUpException(List<String> errors) {
        super(String.join(" ", errors));
        this.errors = List.copyOf(errors);
    }

    public List<String> errors() {
        return errors;
    }
}
