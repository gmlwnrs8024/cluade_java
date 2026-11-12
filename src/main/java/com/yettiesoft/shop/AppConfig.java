package com.yettiesoft.shop;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * 설정값을 읽는다. 아래로 갈수록 우선한다.
 * <ol>
 *   <li>jar 안의 application.properties (기본값)</li>
 *   <li>외부 설정 파일: -Dshop.config=경로, 없으면 실행 폴더의 application.properties</li>
 *   <li>-D 옵션 (예: java -Dserver.port=9090 -jar shop.jar)</li>
 * </ol>
 */
public class AppConfig {

    private static final String FILE_NAME = "application.properties";

    private final Properties props;

    private AppConfig(Properties props) {
        this.props = props;
    }

    public static AppConfig load() {
        return load(Path.of(System.getProperty("shop.config", FILE_NAME)));
    }

    static AppConfig load(Path externalFile) {
        Properties props = new Properties();
        try (InputStream in = AppConfig.class.getResourceAsStream("/" + FILE_NAME)) {
            if (in != null) {
                load(props, in);
            }
            if (Files.exists(externalFile)) {
                try (InputStream fileIn = Files.newInputStream(externalFile)) {
                    load(props, fileIn);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("설정 파일을 읽을 수 없습니다", e);
        }
        return new AppConfig(props);
    }

    /** 한글 값도 읽을 수 있도록 UTF-8로 읽는다. 같은 키는 나중에 읽은 값으로 덮어쓴다. */
    private static void load(Properties props, InputStream in) throws IOException {
        try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            props.load(reader);
        }
    }

    public String get(String key) {
        String value = System.getProperty(key, props.getProperty(key));
        if (value == null) {
            throw new IllegalStateException("설정값이 없습니다: " + key);
        }
        return value.trim();
    }

    public int getInt(String key) {
        String value = get(key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("설정값이 숫자가 아닙니다: " + key + "=" + value, e);
        }
    }
}
