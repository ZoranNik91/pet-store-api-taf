package zoran.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private final String baseUrl;
    private final String apiKey;

    public Config() {
        Properties prop = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            prop.load(input);
            this.baseUrl = prop.getProperty("api.base.url");
            this.apiKey = prop.getProperty("api.key");
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load configuration", ex);
        }
    }

    public String getBaseUrl() { return baseUrl; }
    public String getApiKey() { return apiKey; }
}