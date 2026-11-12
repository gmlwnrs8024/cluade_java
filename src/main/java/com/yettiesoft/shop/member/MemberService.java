package com.yettiesoft.shop.member;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/** 회원가입과 로그인 규칙. 화면(HTTP)과 상관없는 업무 규칙만 여기에 둔다. */
public class MemberService {

    private static final Pattern LOGIN_ID = Pattern.compile("[a-zA-Z0-9_]{4,20}");
    private static final int PASSWORD_MIN = 8;
    private static final int PASSWORD_MAX = 100;
    private static final int NAME_MAX = 20;

    private final MemberRepository repository;

    public MemberService(MemberRepository repository) {
        this.repository = repository;
    }

    /**
     * 입력값을 검사한 뒤 회원을 만든다. 잘못된 값이 있으면 {@link SignUpException}을 던진다.
     * synchronized: 같은 아이디로 동시에 가입 요청이 와도 한 명만 가입되도록 한 번에 하나씩 처리한다.
     */
    public synchronized Member signUp(String loginId, String password, String name) {
        List<String> errors = new ArrayList<>();
        if (!LOGIN_ID.matcher(loginId).matches()) {
            errors.add("아이디는 영문, 숫자, _ 로 4~20자여야 합니다.");
        } else if (repository.findByLoginId(loginId).isPresent()) {
            errors.add("이미 사용 중인 아이디입니다.");
        }
        if (password.length() < PASSWORD_MIN || password.length() > PASSWORD_MAX) {
            errors.add("비밀번호는 " + PASSWORD_MIN + "~" + PASSWORD_MAX + "자여야 합니다.");
        }
        String trimmedName = name.strip();
        if (trimmedName.isEmpty() || trimmedName.length() > NAME_MAX) {
            errors.add("이름은 1~" + NAME_MAX + "자여야 합니다.");
        }
        if (!errors.isEmpty()) {
            throw new SignUpException(errors);
        }
        return repository.create(loginId, PasswordHasher.hash(password), trimmedName);
    }

    /** 아이디와 비밀번호가 맞으면 회원을, 아니면 빈 값을 돌려준다. 어느 쪽이 틀렸는지는 알려주지 않는다. */
    public Optional<Member> login(String loginId, String password) {
        if (password.isEmpty()) {
            return Optional.empty();
        }
        return repository.findByLoginId(loginId)
                .filter(member -> PasswordHasher.matches(password, member.passwordHash()));
    }
}
