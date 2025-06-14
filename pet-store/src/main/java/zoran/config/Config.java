package zoran.config;

import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Getter
public class Config {
    private String baseUrl;
    private String apiKey;
    
    public Config() {
        Properties prop = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            prop.load(input);
            this.baseUrl = prop.getProperty("api.base.url");
            this.apiKey = prop.getProperty("api.key");
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Failed to load configuration");
        }
    }
}