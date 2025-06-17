package zoran.api;

import zoran.models.User;
import io.restassured.response.Response;
import java.util.List;

public class UserApiClient extends BaseApiClient {
    
    public User createUser(User user) {
        return post(user, "/user")
            .then()
            .statusCode(200)
            .extract()
            .as(User.class);
    }
    
    public void createUsersWithArray(List<User> users) {
        post(users, "/user/createWithArray")
            .then()
            .statusCode(200);
    }
    
    public void createUsersWithList(List<User> users) {
        post(users, "/user/createWithList")
            .then()
            .statusCode(200);
    }
    
    public User getUserByUsername(String username) {
        try {
            Response response = get("/user/{username}", username);
            
            if (response.getStatusCode() == 404) {
                throw new RuntimeException("404 User not found with username: " + username);
            }
            
            // Check for successful response
            if (response.getStatusCode() != 200) {
                throw new RuntimeException("Unexpected status code " + response.getStatusCode() + 
                                       " when getting user " + username + 
                                       ": " + response.getBody().asString());
            }
            
            return response.as(User.class);
            
        } catch (RuntimeException e) {
            // Re-throw as is if it's already our custom exception
            if (e.getMessage() != null && e.getMessage().startsWith("404")) {
                throw e;
            }
            // Wrap other runtime exceptions
            throw new RuntimeException("Failed to get user " + username + ": " + e.getMessage(), e);
        } catch (Exception e) {
            // Wrap checked exceptions
            throw new RuntimeException("Failed to get user " + username + ": " + e.getMessage(), e);
        }
    }
    
    public User updateUser(String username, User user) {
        if (username == null || user == null) {
            throw new IllegalArgumentException("Username and user object cannot be null");
        }
        
        return put(user, "/user/{username}", username)
            .then()
            .statusCode(200)
            .extract()
            .as(User.class);
    }
    
    public boolean deleteUser(String username) {
        if (username == null) {
            throw new IllegalArgumentException("Username cannot be null");
        }
        
        Response response = delete("/user/{username}", username);
        
        // The API returns 200 for successful deletion
        // and 404 if user doesn't exist (which we'll consider as success for idempotency)
        if (response.getStatusCode() == 200 || response.getStatusCode() == 404) {
            return true;
        }
        
        // For other status codes, log the error and return false
        System.err.println("Failed to delete user " + username + 
                          ". Status code: " + response.getStatusCode() + 
                          ", Response: " + response.getBody().asString());
        return false;
    }
    
    public String loginUser(String username, String password) {
        try {
            // The login endpoint returns a JSON with code, type, and message fields
            Response response = get("/user/login?username={username}&password={password}", username, password);
            
            // Extract the response body as string for logging
            String responseBody = response.getBody().asString();
            System.out.println("Login response: " + response.getStatusCode() + " - " + responseBody);
            
            // If successful (200), return the message
            if (response.getStatusCode() == 200) {
                return response.jsonPath().getString("message");
            }
            
            // For error responses, throw an exception with the response details
            throw new RuntimeException("Login failed with status " + response.getStatusCode() + 
                                    ". Response: " + responseBody);
                                    
        } catch (Exception e) {
            System.err.println("Error during login: " + e.getMessage());
            throw new RuntimeException("Failed to login: " + e.getMessage(), e);
        }
    }
    
    public void logoutUser() {
        get("/user/logout")
            .then()
            .statusCode(200);
    }
}
