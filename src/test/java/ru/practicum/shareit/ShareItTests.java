package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Locale;

@SpringBootTest
class ShareItTests {

    public ShareItTests() {
        Locale locale = Locale.forLanguageTag("ru-RU");
        Locale.setDefault(locale);
    }

    @Test
    void contextLoads() {
    }

}
