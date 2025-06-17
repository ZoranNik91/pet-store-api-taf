package zoran.utils;

import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;

/**
 * Utility class for common test operations
 */
public class TestUtils {

    /**
     * Verify that the response status code matches the expected value
     *
     * @param response The response to check
     * @param expectedStatusCode The expected HTTP status code
     */
    public static void verifyStatusCode(Response response, int expectedStatusCode) {
        Assertions.assertEquals(
            expectedStatusCode, 
            response.getStatusCode(),
            "Expected status code " + expectedStatusCode + " but was " + response.getStatusCode()
        );
    }

    /**
     * Verify that a field in the response matches the expected value
     * 
     * @param response The response containing the field
     * @param fieldPath The JSON path to the field
     * @param expectedValue The expected value of the field
     * @param <T> The type of the expected value
     */
    public static <T> void verifyField(Response response, String fieldPath, T expectedValue) {
        T actualValue = response.jsonPath().get(fieldPath);
        Assertions.assertEquals(
            expectedValue, 
            actualValue,
            "Expected " + fieldPath + " to be " + expectedValue + " but was " + actualValue
        );
    }

    /**
     * Verify that a field in the response is not null
     * 
     * @param response The response containing the field
     * @param fieldPath The JSON path to the field
     */
    public static void verifyFieldIsNotNull(Response response, String fieldPath) {
        Object value = response.jsonPath().get(fieldPath);
        Assertions.assertNotNull(
            value,
            "Expected " + fieldPath + " to not be null but it was"
        );
    }

    /**
     * Verify that a field in the response is null
     * 
     * @param response The response containing the field
     * @param fieldPath The JSON path to the field
     */
    public static void verifyFieldIsNull(Response response, String fieldPath) {
        Object value = response.jsonPath().get(fieldPath);
        Assertions.assertNull(
            value,
            "Expected " + fieldPath + " to be null but was " + value
        );
    }
}
