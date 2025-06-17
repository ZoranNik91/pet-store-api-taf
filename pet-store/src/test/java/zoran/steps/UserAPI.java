package zoran.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import zoran.api.UserApiClient;
import zoran.models.User;
import zoran.utils.UserGenerator;
import zoran.utils.Assertions;

import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class UserAPI {
    private final UserApiClient userApi = new UserApiClient();
    private User testUser;
    private String authToken;
    private Exception lastException;
    private User lastRetrievedUser;
    private String lastLoginResponse;
    private List<User> createdUsers = new ArrayList<>();

    @Given("I have a user with the following details:")
    public void createUser(io.cucumber.datatable.DataTable dataTable) {
        testUser = User.builder()
                .id(UserGenerator.generateRandomId())
                .username(dataTable.cell(1, 0))
                .firstName(dataTable.cell(1, 1))
                .lastName(dataTable.cell(1, 2))
                .email(dataTable.cell(1, 3))
                .password(dataTable.cell(1, 4))
                .phone(dataTable.cell(1, 5))
                .userStatus(Integer.parseInt(dataTable.cell(1, 6)))
                .build();
    }

    @When("I create the user")
    public void createUser() {
        userApi.createUser(testUser);
    }

    @Then("the user should be created successfully")
    public void verifyUserCreated() {
        User fetchedUser = userApi.getUserByUsername(testUser.getUsername());
        assertThat(fetchedUser, notNullValue());
        assertThat(fetchedUser.getUsername(), equalTo(testUser.getUsername()));
    }

    @When("I login with username {string} and password {string}")
    public void loginUser(String username, String password) {
        authToken = userApi.loginUser(username, password);
    }

    @Then("I should receive an authentication token")
    public void verifyAuthToken() {
        assertThat(authToken, not(emptyString()));
    }

    @When("I logout")
    public void logoutUser() {
        userApi.logoutUser();
    }

    @When("I get the user by username")
    public void getUserByUsername() {
        User fetchedUser = userApi.getUserByUsername(testUser.getUsername());
        testUser = fetchedUser; // Update with server response
    }

    @When("I update the user with new details:")
    public void updateUser(io.cucumber.datatable.DataTable dataTable) {
        testUser.setFirstName(dataTable.cell(1, 0));
        testUser.setLastName(dataTable.cell(1, 1));
        testUser.setEmail(dataTable.cell(1, 2));
        
        userApi.updateUser(testUser.getUsername(), testUser);
    }

    @Then("the user details should be updated")
    public void verifyUserUpdated() {
        User fetchedUser = userApi.getUserByUsername(testUser.getUsername());
        assertThat(fetchedUser.getFirstName(), equalTo(testUser.getFirstName()));
        assertThat(fetchedUser.getLastName(), equalTo(testUser.getLastName()));
        assertThat(fetchedUser.getEmail(), equalTo(testUser.getEmail()));
    }

    @When("I delete the user")
    public void deleteUser() {
        userApi.deleteUser(testUser.getUsername());
    }

    @Then("the user should not be found")
    public void verifyUserNotFound() {
        try {
            userApi.getUserByUsername(testUser.getUsername());
            throw new AssertionError("Expected user to be deleted but it was found");
        } catch (Exception e) {
            assertThat(e.getMessage(), containsString("404"));
        }
    }

    @When("I create multiple users from a list")
    public void createUsersWithList() {
        User user1 = UserGenerator.generateRandomUser();
        User user2 = UserGenerator.generateRandomUser();
        
        userApi.createUsersWithList(Arrays.asList(user1, user2));
        testUser = user1; // Set the first user as the test user
    }

    @Given("I have created a user")
    public void iHaveCreatedAUser() {
        if (testUser == null) {
            testUser = UserGenerator.generateRandomUser();
            userApi.createUser(testUser);
        }
    }

    @Then("the user details should be returned correctly")
    public void theUserDetailsShouldBeReturnedCorrectly() {
        assertNotNull(testUser, "Test user should be created first");
        User retrievedUser = userApi.getUserByUsername(testUser.getUsername());
        assertEquals(testUser.getUsername(), retrievedUser.getUsername());
        assertEquals(testUser.getEmail(), retrievedUser.getEmail());
        assertEquals(testUser.getFirstName(), retrievedUser.getFirstName());
        assertEquals(testUser.getLastName(), retrievedUser.getLastName());
    }

    @Given("I am logged in")
    public void iAmLoggedIn() {
        if (testUser == null) {
            iHaveCreatedAUser();
        }
        String loginStatus = userApi.loginUser(testUser.getUsername(), testUser.getPassword());
        assertTrue(loginStatus.contains("logged in user session:"));
    }

    @Then("I should be logged out successfully")
    public void iShouldBeLoggedOutSuccessfully() {
        userApi.logoutUser();
        // The logout API returns void, so we'll just verify no exception was thrown
        // If we need to verify logout, we can try to access a protected endpoint
    }

    @Then("all users should be created successfully")
    public void allUsersShouldBeCreatedSuccessfully() {
        assertNotNull(createdUsers);
        assertFalse(createdUsers.isEmpty());
        
        for (User user : createdUsers) {
            User retrievedUser = userApi.getUserByUsername(user.getUsername());
            assertEquals(user.getUsername(), retrievedUser.getUsername());
        }
    }

    @Given("I have placed an order")
    public void iHavePlacedAnOrder() {
        // This step assumes the order creation is handled in the StoreManagementAPI
        // and is just a placeholder to indicate an order exists in the context
        // The actual order creation should be done in the StoreManagementAPI steps
    }

    @Given("a user with username {string} does not exist")
    public void aUserWithUsernameDoesNotExist(String username) {
        try {
            // First try to delete the user if it exists
            userApi.deleteUser(username);
            System.out.println("Deleted user " + username + " to ensure it doesn't exist");
            
            // Give the API a moment to process the deletion
            Thread.sleep(1000);
        } catch (Exception e) {
            // User doesn't exist, which is what we want
            System.out.println("No need to delete user " + username + ": " + e.getMessage());
        }
        
        // Don't verify here - let the test steps handle the verification
        // This allows the actual test to verify the 404 behavior
        System.out.println("Assuming user " + username + " does not exist for this test");
    }

    @When("I attempt to get the user with username {string}")
    public void iAttemptToGetTheUserWithUsername(String username) {
        try {
            System.out.println("Attempting to get user with username: " + username);
            lastRetrievedUser = null;
            lastException = null;
            
            // This will throw an exception if the user is not found
            User user = userApi.getUserByUsername(username);
            
            // If we get here, the user exists
            System.out.println("Successfully retrieved user: " + user.getUsername());
            lastRetrievedUser = user;
        } catch (RuntimeException e) {
            System.out.println("Error getting user: " + e.getMessage());
            lastRetrievedUser = null;
            lastException = e;
            // Don't rethrow - we want to verify the error in the test
        }
    }

    @When("I attempt to login with username {string} and password {string}")
    public void iAttemptToLoginWithUsernameAndPassword(String username, String password) {
        try {
            lastLoginResponse = userApi.loginUser(username, password);
            System.out.println("Login response: " + lastLoginResponse);
            
            // The Petstore API returns a session ID even for invalid logins
            // So we need to verify if the login was actually successful
            if (lastLoginResponse.startsWith("logged in user session:")) {
                // This is a successful login
                lastException = null;
            } else {
                // This is an error response
                lastException = new RuntimeException("Login failed: " + lastLoginResponse);
            }
        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
            lastLoginResponse = null;
            lastException = e;
        }
    }

    @Then("I should receive an error response")
    public void iShouldReceiveAnErrorResponse() {
        // If we have an exception, that's our error response
        if (lastException != null) {
            System.out.println("Received error response as exception: " + lastException.getMessage());
            lastException = null; // Reset after handling
            return;
        }
        
        // If no exception, check if we have a response with error status or message
        if (lastLoginResponse != null) {
            System.out.println("Checking response for error: " + lastLoginResponse);
            
            // For login specifically, the Petstore API returns a 200 with a session ID even for invalid logins
            // So we need to check if this is a login attempt with invalid credentials
            if (lastLoginResponse.startsWith("logged in user session:")) {
                // This is actually a successful login response, not an error
                fail("Expected an error response but got a successful login response: " + lastLoginResponse);
            }
            
            // Check for common error indicators in the response
            if (lastLoginResponse.contains("error") || 
                lastLoginResponse.contains("code") || 
                lastLoginResponse.contains("message") ||
                lastLoginResponse.contains("status")) {
                System.out.println("Error response detected in the message body");
                return;
            }
            
            // If we have a response but it doesn't look like an error, fail the test
            fail("Expected an error response but got: " + lastLoginResponse);
        } else {
            // No response and no exception - this is an error
            fail("Expected an error response but got no response");
        }
    }
    
    @Then("I should receive a login response with a session ID")
    public void iShouldReceiveALoginResponseWithASessionID() {
        assertNotNull("Expected a login response but got null", lastLoginResponse);
        assertTrue(lastLoginResponse.startsWith("logged in user session:"),
                "Expected login response to contain 'logged in user session:' but got: " + lastLoginResponse);
    }
    
    @Then("I should receive a {int} Not Found error for user")
    public void iShouldReceiveANotFoundErrorForUser(int statusCode) {
        // Check if we got a user when we expected none
        if (lastRetrievedUser != null) {
            throw new AssertionError("Expected a " + statusCode + " Not Found error but got a valid user: " + lastRetrievedUser);
        }
        
        Assertions.verifyNotFoundError(statusCode, lastException);
        lastException = null; // Reset after verification
    }
}
