@user
Feature: User API
    As an API user
    I want to manage user accounts
    So that I can create, update, and delete user information

  Background:
    Given the Pet Store API is available

  @smoke @createUser
  Scenario: Create a new user
    Given I have a user with the following details:
      | username | firstName | lastName | email               | password  | phone       | userStatus |
      | johndoe  | John      | Doe      | john@example.com    | password1 | 1234567890  | 1          |
      | janedoe  | Jane      | Doe      | jane@example.com    | password2 | 0987654321  | 0          |
    When I create the user
    Then the user should be created successfully

  @smoke @loginUser
  Scenario: Login with valid credentials
    Given I have created a user
    When I login with username "johndoe" and password "password1"
    Then I should receive an authentication token

  @smoke @getUser
  Scenario: Get user by username
    Given I have created a user
    When I get the user by username
    Then the user details should be returned correctly

  @smoke @updateUser
  Scenario: Update an existing user
    Given I have created a user
    When I update the user with new details:
      | firstName | lastName | email           |
      | Johnathan | Doer     | john.d@example.com |
    Then the user details should be updated

  @smoke @logoutUser
  Scenario: Logout user
    Given I am logged in
    When I logout
    Then I should be logged out successfully

  @regression @createWithList
  Scenario: Create users with list
    When I create multiple users from a list
    Then all users should be created successfully

  @smoke @deleteUser
  Scenario: Delete a user
    Given I have created a user
    When I delete the user
    Then the user should not be found

  @regression @negative
  Scenario: Attempt to get a non-existent user
    Given a user with username "nonexistent" does not exist
    When I attempt to get the user with username "nonexistent"
    Then I should receive a 404 Not Found error for user

  @regression @negative
  Scenario: Attempt to login with invalid credentials
    When I attempt to login with username "invalid" and password "wrongpassword"
    Then I should receive a login response with a session ID
