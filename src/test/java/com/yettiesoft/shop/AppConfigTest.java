package com.yettiesoft.shop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppConfigTest {

    @TempDir
    Path tempDir;

    @AfterEach
    void clearSystemProperty() {
        System.clearProperty("server.port");
    }

    @Test
    void usesDefaultsInsideJarWhenNoExternalFile() {
        AppConfig config = AppConfig.load(tempDir.resolve("application.properties"));

        assertEquals(8080, config.getInt("server.port"));
    }

    @Test
    void externalFileOverridesDefaults() throws Exception {
        Path file = Files.writeString(tempDir.resolve("application.properties"), "server.port=9090\n");

        AppConfig config = AppConfig.load(file);

        assertEquals(9090, config.getInt("server.port"));
    }

    @Test
    void systemPropertyOverridesExternalFile() throws Exception {
        Path file = Files.writeString(tempDir.resolve("application.properties"), "server.port=9090\n");
        System.setProperty("server.port", "7070");

        AppConfig config = AppConfig.load(file);

        assertEquals(7070, config.getInt("server.port"));
    }

    @Test
    void readsKoreanValuesAsUtf8() throws Exception {
        Path file = Files.writeString(tempDir.resolve("application.properties"), "shop.name=예티 쇼핑몰\n");

        AppConfig config = AppConfig.load(file);

        assertEquals("예티 쇼핑몰", config.get("shop.name"));
    }

    @Test
    void missingKeyFailsWithClearMessage() {
        AppConfig config = AppConfig.load(tempDir.resolve("application.properties"));

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> config.get("no.such.key"));
        assertEquals("설정값이 없습니다: no.such.key", e.getMessage());
    }
}
