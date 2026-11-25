package com.example.shortener.config;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private final Properties properties = new Properties();

    public AppConfig() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            System.err.println("Ошибка загрузки конфигурации: " + e.getMessage());
        }
    }

    public String getBaseUrl() { return properties.getProperty("app.base.url", "http://clck.ru/"); }
    public int getDefaultTtlMinutes() { return Integer.parseInt(properties.getProperty("link.default.ttl.minutes", "1440")); }
    public int getDefaultMaxClicks() { return Integer.parseInt(properties.getProperty("link.default.max.clicks", "100")); }
    public int getCleanupIntervalSeconds() { return Integer.parseInt(properties.getProperty("cleanup.interval.seconds", "60")); }
}
