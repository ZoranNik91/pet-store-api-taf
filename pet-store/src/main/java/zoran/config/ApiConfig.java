package zoran.config;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

/**
 * Configuration class for API settings
 */
public class ApiConfig {
    // Base URL for the Petstore API
    public static final String BASE_URI = "https://petstore.swagger.io/v2";
    
    // JSESSIONID for authentication
    public static final String JSESSIONID = "1fvtg340s8g1cjzf3ae3jx1tz";
    
    /**
     * @return Default request specification with common settings
     */
    public static RequestSpecification getDefaultRequestSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(BASE_URI)
                .setContentType(ContentType.JSON)
                .addHeader("Accept", "application/json")
                .addCookie("JSESSIONID", JSESSIONID)
                .build();
    }
}
