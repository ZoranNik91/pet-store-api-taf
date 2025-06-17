@pet
Feature: Pet API
    As an API user
    I want to manage pets in the store
    So that I can perform CRUD operations on pet data

  Background:
    Given the Pet Store API is available

  # ==================== PET CREATION ====================
  @smoke @addPet
  Scenario: Add a new basic pet to the store
    Given I have a pet with the following details:
      | name   | status    |
      | Doggie | available |
#    When I add the pet to the store
#    Then the pet should be added successfully
#    And the response should contain the pet details

#  @regression @addPet
#  Scenario: Add a new pet with all details
#    Given I have a pet with the following details:
#      | name   | status    | category | tags         |
#      | Buddy  | available | Dog      | friendly,big |
#    When I add the pet to the store
#    Then the pet should be added successfully
#    And the response should contain the pet details
#    And the pet should have the category "Dog"
#    And the pet should have the tag "friendly"
#
#  # ==================== PET RETRIEVAL ====================
#  @smoke @getPet
#  Scenario: Retrieve pet details by ID
#    Given I have added a pet to the store
#    When I retrieve the pet by its ID
#    Then the pet details should be returned correctly
#
#  @regression @getPet
#  Scenario: Attempt to retrieve non-existent pet
#    When I attempt to retrieve a pet with ID "999999"
#    Then the response status code should be 404
#    And the response should contain error message "Pet not found"
#
#  # ==================== PET UPDATES ====================
#  @smoke @updatePet
#  Scenario: Update an existing pet's status
#    Given I have added a pet to the store
#    When I update the pet's status to "sold"
#    Then the pet should be updated successfully
#    And the pet status should be "sold"
#
#  @regression @updatePet
#  Scenario: Update pet with form data
#    Given I have added a pet to the store
#    When I update the pet's details via form:
#      | name   | status   |
#      | Doggie | pending  |
#    Then the pet should be updated successfully
#    And the pet name should be "Max"
#    And the pet status should be "pending"
#
#  # ==================== IMAGE UPLOAD ====================
#  @regression @uploadImage
#  Scenario: Upload an image for a pet
#    Given I have added a pet to the store
#    When I upload an image "test-data/images/pet1.jpg" for the pet
#    Then the image should be uploaded successfully
#    And the response should contain the success message
#
#  @regression @uploadImage
#  Scenario: Upload multiple images for a pet
#    Given I have added a pet to the store
#    When I upload the following images for the pet:
#      | test-data/images/pet1.jpg |
#      | test-data/images/pet2.jpg  |
#    Then the images should be uploaded successfully
#
#  # ==================== FIND PETS ====================
#  @smoke @findByStatus
#  Scenario: Find available pets
#    Given I have added a pet with status "available"
#    When I find pets by status "available"
#    Then I should receive a non-empty list of available pets
#
#  @regression @findByStatus
#  Scenario: Find pets by multiple statuses
#    Given I have added pets with different statuses
#    When I find pets by status "available,pending"
#    Then I should receive a list containing pets with the specified statuses
#
#  @regression @findByStatus
#  Scenario: Find pets with invalid status
#    When I attempt to find pets with status "invalid_status"
#    Then the response status code should be 400
#    And the response should contain error message "Invalid status value"
#
#  # ==================== PET DELETION ====================
#  @smoke @deletePet
#  Scenario: Delete an existing pet
#    Given I have added a pet to the store
#    When I delete the pet
#    Then the pet should be deleted successfully
#    And the pet should no longer exist
#
#  @regression @deletePet
#  Scenario: Delete non-existent pet
#    When I attempt to delete a pet with ID "999999"
#    Then the response status code should be 404
#    And the response should contain error message "Pet not found"
#    When I delete the pet
#    Then the pet should not be found
#    And I should not be able to retrieve the deleted pet
#
#  @petManagement @regression @negative
#  Scenario: Attempt to retrieve a non-existent pet
#    Given the Pet Store API is available
#    Given a pet with ID 999999999 does not exist
#    When I attempt to retrieve the pet with ID 999999999
#
#  @petManagement @regression @negative
#  Scenario: Attempt to update a non-existent pet
#    Given the Pet Store API is available
#    Given a pet with ID 999999999 does not exist
#    When I attempt to update the pet with ID 999999999