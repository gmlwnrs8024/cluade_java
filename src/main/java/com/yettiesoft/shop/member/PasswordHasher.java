package com.yettiesoft.shop.member;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * 비밀번호를 저장용 값으로 바꾸고, 입력한 비밀번호가 맞는지 확인한다. JDK 기본 기능(PBKDF2)만 쓴다.
 * <p>
 * 비밀번호 원문은 절대 저장하지 않는다. 저장소가 유출되더라도 원래 비밀번호를 알아내기 어렵게 하기 위해:
 * <ul>
 *   <li>회원마다 다른 무작위 값(salt)을 섞어서, 같은 비밀번호라도 저장값이 달라지게 하고</li>
 *   <li>계산을 일부러 수십만 번 반복해서, 하나씩 대입해 보는 공격을 느리게 만든다.</li>
 * </ul>
 * 저장 형식: "반복횟수:salt:해시" (salt와 해시는 Base64 문자열)
 */
public final class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 600_000; // OWASP 권장값
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {
    }

    public static String hash(String password) {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] hash = pbkdf2(password, salt, ITERATIONS);
        Base64.Encoder encoder = Base64.getEncoder();
        return ITERATIONS + ":" + encoder.encodeToString(salt) + ":" + encoder.encodeToString(hash);
    }

    public static boolean matches(String password, String stored) {
        String[] parts = stored.split(":");
        int iterations = Integer.parseInt(parts[0]);
        byte[] salt = Base64.getDecoder().decode(parts[1]);
        byte[] expected = Base64.getDecoder().decode(parts[2]);
        byte[] actual = pbkdf2(password, salt, iterations);
        // 일반 equals는 앞에서부터 다른 글자를 만나면 바로 멈춰서, 걸린 시간으로 정답을 추측할 여지가 생긴다.
        // MessageDigest.isEqual은 항상 끝까지 비교한다.
        return MessageDigest.isEqual(expected, actual);
    }

    private static byte[] pbkdf2(String password, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, KEY_BITS);
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("비밀번호 해시를 계산할 수 없습니다", e);
        }
    }
}
