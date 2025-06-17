package zoran.base;

import zoran.config.Config;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;

public class BaseApiTest {
    protected static final Config config = new Config();

    @BeforeAll
    public static void setUp() {
        RestAssured.baseURI = config.getBaseUrl();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
}