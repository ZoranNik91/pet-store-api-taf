@store
Feature: Store API
    As an API user
    I want to manage store orders
    So that I can place and track orders for pets
#
#  Background:
#    Given the Pet Store API is available
#
#  @smoke @placeOrder
#  Scenario: Place a new order for a pet
#    Given I have an order with the following details:
#      | petId | quantity | status   | complete |
#      | 1     | 1        | placed   | true     |
#      | 2     | 2        | approved | false    |
#    When I place the order
#    Then the order should be placed successfully
#
#  @smoke @getOrder
#  Scenario: Retrieve an order by ID
#    Given I have placed an order
#    When I get the order by ID
#    Then the order details should be returned correctly
#
#  @regression @inventory
#  Scenario: Get store inventory
#    When I get the store inventory
#    Then the inventory should be returned
#    And the inventory should contain available pets
#
#  @smoke @deleteOrder
#  Scenario: Delete an order
#    Given I have placed an order
#    When I delete the order
#    Then the order should not be found
#
#  @regression @negative
#  Scenario: Attempt to retrieve a non-existent order
#    Given an order with ID 999999999 does not exist
#    When I attempt to retrieve the order with ID 999999999
#    Then I should receive a 404 Not Found error
#
#  @regression @negative
#  Scenario: Attempt to delete a non-existent order
#    Given an order with ID 999999999 does not exist
#    When I attempt to delete the order with ID 999999999
#    Then I should receive a 404 Not Found error
