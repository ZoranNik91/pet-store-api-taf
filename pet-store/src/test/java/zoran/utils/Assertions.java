package zoran.utils;

import io.restassured.response.Response;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Common assertion utilities for test steps
 */
public class Assertions {
    
    /**
     * Verifies that an exception indicates a "Not Found" error
     * @param statusCode The expected status code (e.g., 404)
     * @param exception The exception to verify, or null if no exception was thrown
     * @throws AssertionError if the exception doesn't indicate a not found error
     */
    public static void verifyNotFoundError(int statusCode, Exception exception) {
        String expectedStatusCode = String.valueOf(statusCode);
        
        // If no exception was thrown, but we expected one, fail the test
        if (exception == null) {
            throw new AssertionError("Expected an exception with status code " + statusCode + " but no exception was thrown");
        }
        
        // Check if the exception message contains the expected status code or a relevant error message
        String errorMessage = exception.getMessage().toLowerCase();
        
        // Check for different patterns that might indicate a not found error
        boolean isNotFoundError = errorMessage.contains(expectedStatusCode) || 
                               errorMessage.contains("not found") || 
                               errorMessage.contains("404") ||
                               errorMessage.contains("user not found") ||
                               errorMessage.contains("pet not found");
        
        if (!isNotFoundError) {
            throw new AssertionError("Expected a not found error (status " + statusCode + ") but got: " + exception.getMessage());
        }
    }
    
    /**
     * Asserts that the response has the expected status code
     * @param response The response to check
     * @param expectedStatusCode The expected status code
     * @throws AssertionError if the status code doesn't match
     */
    public static void assertStatusCode(Response response, int expectedStatusCode) {
        assertEquals(expectedStatusCode, response.getStatusCode(), 
            "Expected status code " + expectedStatusCode + " but was " + response.getStatusCode() + 
            "\nResponse body: " + response.getBody().asString());
    }
}
