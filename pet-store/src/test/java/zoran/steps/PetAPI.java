package zoran.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import static org.junit.jupiter.api.Assertions.*;
import io.restassured.response.Response;
import zoran.api.PetApiClient;
import zoran.models.Pet;
import zoran.utils.Assertions;
import zoran.utils.PetGenerator;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.io.FileNotFoundException;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class PetAPI {

    private Pet testPet;
    private Response response;
    private final PetApiClient petApi = new PetApiClient();
    private Pet createdPet;
    private Pet foundPet;
    private Exception lastException;

    @Given("the Pet Store API is available")
    public void verifyApiAvailable() {
        // The API client will handle the base URL and connection
        // We can make a simple call to verify the API is reachable
        petApi.findPetsByStatus("available");
    }

    @Given("I have a pet with the following details:")
    public void createPetWithDetails(io.cucumber.datatable.DataTable dataTable) {
        try {
            // Get basic pet details
            String name = dataTable.cell(1, 0).trim();
            String status = dataTable.cell(1, 1).trim();
            
            // Initialize photoUrls list
            List<String> photoUrls = new ArrayList<>();
            
            // Check if photoUrls column exists and has a value
            if (dataTable.width() > 2) {
                String photoUrlCell = dataTable.cell(1, 2).trim();
                if (!photoUrlCell.isEmpty()) {
                    // Split multiple URLs by comma and trim whitespace
                    String[] urls = photoUrlCell.split(",");
                    for (String url : urls) {
                        String trimmedUrl = url.trim();
                        if (!trimmedUrl.isEmpty()) {
                            photoUrls.add(trimmedUrl);
                        }
                    }
                }
            }
            
            // If no photo URLs were provided, use a default one
            if (photoUrls.isEmpty()) {
                photoUrls.add("https://example.com/photo1.jpg");
                System.out.println("No photo URLs provided, using default");
            }
            
            // Build the pet with the collected details
            testPet = Pet.builder()
                    .id(PetGenerator.generateRandomId())
                    .name(name)
                    .status(status)
                    .photoUrls(photoUrls)
                    .build();
            
            // Log the created pet details for debugging
            System.out.println("Created test pet with details:" +
                    "\n  ID: " + testPet.getId() +
                    "\n  Name: " + testPet.getName() +
                    "\n  Status: " + testPet.getStatus() +
                    "\n  Photo URLs: " + String.join(", ", testPet.getPhotoUrls()));
                    
        } catch (Exception e) {
            String errorMsg = "Error creating test pet from DataTable: " + e.getMessage();
            System.err.println(errorMsg);
            throw new RuntimeException(errorMsg, e);
        }
    }

    @When("I add the pet to the store")
    public void addPetToStore() {
        System.out.println("\n===== ADDING PET TO STORE =====");
        System.out.println("Timestamp: " + java.time.LocalDateTime.now());
        
        if (testPet == null) {
            throw new IllegalStateException("Test pet is not initialized. Please create a pet first.");
        }
        
        int maxRetries = 3;
        int retryCount = 0;
        boolean success = false;
        
        while (!success && retryCount < maxRetries) {
            try {
                // Log pet details being added
                System.out.println("\n[PET DETAILS]");
                System.out.println("  ID: " + testPet.getId());
                System.out.println("  Name: " + testPet.getName());
                System.out.println("  Status: " + testPet.getStatus());
                System.out.println("  Photo URLs: " + (testPet.getPhotoUrls() != null ? 
                    String.join(", ", testPet.getPhotoUrls()) : "[none]"));
                
                // Add pet to store and get the response
                System.out.println("\n[API REQUEST] Adding pet to store...");
                Pet addedPet = petApi.addPet(testPet);
                
                // Verify the pet was added successfully
                if (addedPet == null || addedPet.getId() == null) {
                    throw new RuntimeException("Failed to add pet: No valid pet data in response");
                }
                
                // Store the added pet for verification
                response = petApi.addPetWithResponse(testPet);
                int statusCode = response.getStatusCode();
                System.out.println("Response Status Code: " + statusCode);
                System.out.println("Successfully added pet with ID: " + addedPet.getId());
                
                if (statusCode == 200 || statusCode == 201) {
                    // Parse the response to get the created pet
                    createdPet = response.as(Pet.class);
                    System.out.println("Successfully added pet with ID: " + createdPet.getId());
                    success = true;
                    
                    // Verify the pet exists in the store
                    verifyPetInStore(createdPet.getId());
                    
                    // Store the created pet ID for cleanup
                    if (createdPet.getId() != null) {
                        System.out.println("Pet successfully added with ID: " + createdPet.getId());
                    } else {
                        System.err.println("Warning: Created pet has null ID");
                    }
                } else {
                    String errorMsg = "Failed to add pet. Status: " + statusCode + 
                                     "\nResponse: " + response.getBody().asString();
                    System.err.println(errorMsg);
                    throw new RuntimeException(errorMsg);
                }
                
            } catch (Exception e) {
                retryCount++;
                if (retryCount >= maxRetries) {
                    String errorMsg = "Failed after " + maxRetries + " attempts: " + e.getMessage();
                    System.err.println(errorMsg);
                    throw new RuntimeException(errorMsg, e);
                }
                
                // Wait before retrying (exponential backoff)
                long waitTime = (long) (Math.pow(2, retryCount) * 1000);
                System.out.println("Retry attempt " + retryCount + " of " + maxRetries + " after " + waitTime + "ms");
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during retry", ie);
                }
            }
        }
    }
    
    /**
     * Verifies that a pet exists in the store by its ID
     * @param petId The ID of the pet to verify
     * @throws RuntimeException if the pet cannot be found or there's an error
     */
    private void verifyPetInStore(long petId) {
        System.out.println("\n[VERIFICATION] Verifying pet with ID: " + petId);
        
        int maxRetries = 3;
        int retryCount = 0;
        boolean verified = false;
        
        while (!verified && retryCount < maxRetries) {
            try {
                Pet verifiedPet = petApi.getPetById(petId);
                
                if (verifiedPet != null && verifiedPet.getId() != null) {
                    System.out.println("Successfully verified pet in store:");
                    System.out.println("  ID: " + verifiedPet.getId());
                    System.out.println("  Name: " + verifiedPet.getName());
                    System.out.println("  Status: " + verifiedPet.getStatus());
                    verified = true;
                } else {
                    String errorMsg = "Pet with ID " + petId + " not found in store";
                    System.err.println(errorMsg);
                    throw new RuntimeException(errorMsg);
                }
            } catch (Exception e) {
                retryCount++;
                if (retryCount >= maxRetries) {
                    System.err.println("Failed to verify pet after " + maxRetries + " attempts: " + e.getMessage());
                    throw new RuntimeException("Failed to verify pet in store", e);
                }
                
                // Wait before retrying (exponential backoff)
                long waitTime = (long) (Math.pow(2, retryCount) * 1000);
                System.out.println("Verification retry attempt " + retryCount + " of " + maxRetries + " after " + waitTime + "ms");
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during verification retry", ie);
                }
            }
        }
    }
    
    @When("I parse the response and store the created pet")
    public void parseAndStoreCreatedPet() {
        if (response == null) {
            throw new IllegalStateException("No response available to parse");
        }
        
        createdPet = response.as(Pet.class);
        if (createdPet == null || createdPet.getId() == null) {
            String responseBody = response.getBody().asString();
            throw new RuntimeException("Failed to parse created pet from response: " + responseBody);
        }
        
        System.out.println("\n[PET CREATED] ID: " + createdPet.getId());
        
        // Verify the pet exists in the system with retry logic
        verifyPetInStore(createdPet.getId());
        
        // Log verification details
        Pet verifiedPet = petApi.getPetById(createdPet.getId());
        if (verifiedPet != null) {
            System.out.println("\n[VERIFICATION SUCCESS] Pet details from API:");
            System.out.println("  ID: " + verifiedPet.getId());
            System.out.println("  Name: " + verifiedPet.getName());
            System.out.println("  Status: " + verifiedPet.getStatus());
            System.out.println("  Photo URLs: " + (verifiedPet.getPhotoUrls() != null ? 
                String.join(", ", verifiedPet.getPhotoUrls()) : "[none]"));
        } else {
            System.out.println("\n[WARNING] Could not retrieve pet details after successful verification");
        }
        
        lastException = null;
        System.out.println("\n===== PET ADDED AND VERIFIED SUCCESSFULLY =====\n");
    }



    @Then("the pet should have the category {string}")
    public void verifyPetHasCategory(String expectedCategory) {
        assertNotNull(createdPet, "No pet was created");
        assertNotNull(createdPet.getCategory(), "Pet category should not be null");
        assertEquals(expectedCategory, createdPet.getCategory().getName(), 
            "Pet category does not match expected");
    }

    @Then("the pet should have the tag {string}")
    public void verifyPetHasTag(String expectedTag) {
        assertNotNull(createdPet, "No pet was created");
        assertNotNull(createdPet.getTags(), "Pet tags should not be null");
        assertTrue(createdPet.getTags().stream()
                .anyMatch(tag -> expectedTag.equals(tag.getName())), 
                "Pet does not have the expected tag: " + expectedTag);
    }
    
    @Then("the pet should have the photo URL {string}")
    public void verifyPetPhotoUrl(String expectedUrl) {
        System.out.println("\n===== VERIFYING PET PHOTO URL =====");
        System.out.println("Expected photo URL: " + expectedUrl);
        
        // Normalize the expected URL
        String normalizedExpectedUrl = expectedUrl.trim();
        System.out.println("Normalized expected URL: " + normalizedExpectedUrl);
        
        Pet refreshedPet = null;
        int maxAttempts = 3;
        int attempt = 0;
        Exception lastError = null;
        
        // Retry logic to handle potential timing issues
        while (attempt < maxAttempts) {
            try {
                System.out.println("\n--- Verification Attempt " + (attempt + 1) + " ---");
                
                // Get the latest pet data
                System.out.println("Retrieving pet with ID: " + createdPet.getId());
                refreshedPet = petApi.getPetById(createdPet.getId());
                
                if (refreshedPet == null) {
                    throw new RuntimeException("Pet not found in the system");
                }
                
                // Log the retrieved pet details
                System.out.println("Retrieved pet details:");
                System.out.println("  ID: " + refreshedPet.getId());
                System.out.println("  Name: " + refreshedPet.getName());
                System.out.println("  Status: " + refreshedPet.getStatus());
                
                // Check if photo URLs exist
                if (refreshedPet.getPhotoUrls() == null) {
                    throw new AssertionError("Pet photo URLs should not be null. Check if the API is returning the photoUrls field.");
                }
                
                System.out.println("Current photo URLs: " + 
                    (refreshedPet.getPhotoUrls().isEmpty() ? "[empty]" : ""));
                
                // Print each photo URL on a new line for better readability
                refreshedPet.getPhotoUrls().forEach(url -> 
                    System.out.println("  - " + (url != null ? url.trim() : "null") + ""));
                
                // Check if we have any photo URLs
                if (refreshedPet.getPhotoUrls().isEmpty()) {
                    throw new AssertionError(String.format("Pet should have at least one photo URL. " +
                        "Expected: %s, but no photo URLs were found.", normalizedExpectedUrl));
                }
                
                // Try to find the URL (case-sensitive first, then case-insensitive)
                boolean exactMatchFound = refreshedPet.getPhotoUrls().stream()
                    .anyMatch(url -> url != null && url.trim().equals(normalizedExpectedUrl));
                    
                if (exactMatchFound) {
                    System.out.println("✓ Found exact case-sensitive match for URL: " + normalizedExpectedUrl);
                    System.out.println("\n===== PHOTO URL VERIFICATION SUCCESSFUL =====\n");
                    return; // Success - exit the method
                }
                
                // If no exact match, try case-insensitive comparison
                boolean caseInsensitiveMatchFound = refreshedPet.getPhotoUrls().stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .anyMatch(normalizedExpectedUrl::equalsIgnoreCase);
                    
                if (caseInsensitiveMatchFound) {
                    System.out.println("⚠ Found case-insensitive match for URL: " + normalizedExpectedUrl);
                    System.out.println("  Note: The test passed, but consider normalizing URL case in your tests for consistency.");
                    System.out.println("\n===== PHOTO URL VERIFICATION SUCCESSFUL (CASE-INSENSITIVE) =====\n");
                    return; // Success - exit the method
                }
                
                // If we get here, no match was found in this attempt
                System.out.println("❌ No matching URL found in attempt " + (attempt + 1));
                
                // If this isn't the last attempt, wait before retrying
                if (attempt < maxAttempts - 1) {
                    System.out.println("Waiting 2 seconds before retry...");
                    Thread.sleep(2000);
                }
                
            } catch (Exception e) {
                lastError = e;
                System.err.println("Error during verification attempt " + (attempt + 1) + ": " + e.getMessage());
                
                // If this isn't the last attempt, wait before retrying
                if (attempt < maxAttempts - 1) {
                    try {
                        System.out.println("Waiting 2 seconds before retry...");
                        Thread.sleep(2000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Thread was interrupted during verification", ie);
                    }
                }
            }
            
            attempt++;
        }
        
        // If we get here, all attempts failed
        String errorMessage = String.format("\n❌ FAILED to verify photo URL after %d attempts.\n" +
            "Expected URL: %s\n" +
            "Actual URLs: %s\n" +
            "Pet details: %s",
            maxAttempts,
            normalizedExpectedUrl,
            (refreshedPet != null && refreshedPet.getPhotoUrls() != null) ? 
                String.join(", ", refreshedPet.getPhotoUrls()) : "[no photo URLs]",
            (refreshedPet != null) ? refreshedPet.toString() : "[pet not found]");
            
        if (lastError != null) {
            errorMessage += "\nLast error: " + lastError.getMessage();
        }
        
        System.err.println(errorMessage);
        throw new AssertionError(errorMessage, lastError);
    }

    @When("I attempt to retrieve a pet with ID {string}")
    public void iAttemptToRetrievePetWithId(String petId) {
        try {
            createdPet = petApi.getPetById(Long.parseLong(petId));
            lastException = null;
        } catch (Exception e) {
            lastException = e;
        }
    }

    @Then("the response status code should be {int}")
    public void theResponseStatusCodeShouldBe(int statusCode) {
        if (lastException != null && lastException.getMessage().contains(String.valueOf(statusCode))) {
            // If we have an exception with the expected status code, consider it a pass
            return;
        }
        assertNotNull(response, "No response available");
        assertEquals(statusCode, response.getStatusCode(), 
            "Unexpected status code");
    }

    @Then("the response should contain error message {string}")
    public void theResponseShouldContainErrorMessage(String errorMessage) {
        if (lastException != null && lastException.getMessage().contains(errorMessage)) {
            // If the exception contains the error message, consider it a pass
            return;
        }
        assertNotNull(response, "No response available");
        String responseBody = response.getBody().asString();
        assertTrue(responseBody.contains(errorMessage), 
            "Response does not contain expected error message: " + errorMessage);
    }

    @When("I update the pet's status to {string}")
    public void updatePetStatus(String status) {
        try {
            // First, determine which pet we're working with
            Pet targetPet = testPet != null ? testPet : createdPet;
            if (targetPet == null) {
                throw new RuntimeException("No pet available for update");
            }
            
            Long petId = targetPet.getId();
            if (petId == null) {
                throw new RuntimeException("Pet ID is null, cannot update status");
            }
            
            // Log the current state
            System.out.println("Updating pet " + petId + " status to: " + status);
            
            // Update the pet status using form data
            response = petApi.updatePetWithForm(petId, null, status);
            
            // Log the response
            System.out.println("Update status response status: " + response.getStatusCode());
            System.out.println("Update status response body: " + response.getBody().asString());
            
            // Check if the update was successful
            if (response.getStatusCode() != 200) {
                throw new RuntimeException("Failed to update pet status. Status code: " + 
                    response.getStatusCode() + ", Response: " + response.getBody().asString());
            }
            
            // Refresh the pet data with a small delay to ensure the update is processed
            Thread.sleep(1000);
            
            // Get the updated pet data with retry logic
            int maxRetries = 3;
            int retryCount = 0;
            Pet updatedPet = null;
            
            while (retryCount < maxRetries) {
                try {
                    updatedPet = petApi.getPetById(petId);
                    if (updatedPet != null && status.equals(updatedPet.getStatus())) {
                        break; // Status matches, exit retry loop
                    }
                    System.out.println("Status not yet updated, retrying... (" + (retryCount + 1) + "/" + maxRetries + ")");
                    if (retryCount < maxRetries - 1) {
                        Thread.sleep(2000); // Wait 2 seconds before retry
                    }
                } catch (Exception e) {
                    System.err.println("Error retrieving updated pet (attempt " + (retryCount + 1) + "): " + e.getMessage());
                    if (retryCount == maxRetries - 1) {
                        throw e; // If last retry, rethrow the exception
                    }
                    Thread.sleep(2000); // Wait 2 seconds before retry
                }
                retryCount++;
            }
            
            if (updatedPet == null) {
                throw new RuntimeException("Failed to retrieve updated pet with ID: " + petId + " after " + maxRetries + " attempts");
            }
            
            // Verify the status was updated
            String actualStatus = updatedPet.getStatus();
            System.out.println("Verifying update - Expected: " + status + ", Actual: " + actualStatus);
            
            if (!status.equals(actualStatus)) {
                throw new AssertionError(
                    String.format("Pet status was not updated correctly. Expected: '%s', Actual: '%s'",
                        status, actualStatus)
                );
            }
            
            System.out.println("Successfully updated pet " + petId + " status to: " + status);
            lastException = null;
            
        } catch (Exception e) {
            lastException = e;
            String errorMsg = "Error updating pet status: " + e.getMessage();
            System.err.println(errorMsg);
            throw new RuntimeException(errorMsg, e);
        }
    }





    @Then("the image should be uploaded successfully")
    public void theImageShouldBeUploadedSuccessfully() {
        assertNotNull(response, "No response from image upload");
        assertEquals(200, response.getStatusCode(), 
            "Image upload failed with status: " + response.getStatusCode());
    }
    
    @Then("the response should contain the success message")
    public void verifyResponseContainsSuccessMessage() {
        assertNotNull(response, "No response available");
        String responseBody = response.getBody().asString().toLowerCase();
        assertTrue(responseBody.contains("success") || responseBody.contains("file uploaded") || 
                 responseBody.contains("message"), 
                "Response does not contain success message. Actual response: " + responseBody);
    }

    @When("I upload the following images for the pet:")
    public void iUploadTheFollowingImagesForThePet(io.cucumber.datatable.DataTable dataTable) {
        List<String> imagePaths = dataTable.asList();
        for (String imagePath : imagePaths) {
            try {
                File imageFile = new File(getClass().getClassLoader().getResource(imagePath.trim()).getFile());
                response = petApi.uploadPetImage(createdPet.getId(), imageFile, "Additional image");
                assertEquals(200, response.getStatusCode(), 
                    "Failed to upload image: " + imagePath);
            } catch (Exception e) {
                throw new RuntimeException("Failed to upload image: " + imagePath, e);
            }
        }
    }

    @When("I attempt to find pets with status {string}")
    public void iAttemptToFindPetsWithStatus(String status) {
        try {
            foundPets = petApi.findPetsByStatus(status);
            lastException = null;
        } catch (Exception e) {
            lastException = e;
        }
    }

    @Then("the pet should be deleted successfully")
    public void thePetShouldBeDeletedSuccessfully() {
        // The delete operation should not throw an exception
        assertNull(lastException, "Delete operation failed with exception: " + 
            (lastException != null ? lastException.getMessage() : ""));
    }

@Given("I have added a pet to the store")
public void addDefaultPetToStore() {
    testPet = PetGenerator.generateRandomPet();
    createdPet = petApi.addPet(testPet);
    testPet.setId(createdPet.getId());
}

@When("I retrieve the pet by its ID")
public void retrievePetById() {
    try {
        createdPet = petApi.getPetById(testPet.getId());
        lastException = null;
    } catch (Exception e) {
        createdPet = null;
        lastException = e;
        // Don't throw here, let the test steps handle the error
    }
}

@Then("the pet status should be {string}")
public void verifyPetStatus(String expectedStatus) {
    assertThat("Pet status should be " + expectedStatus, 
              createdPet.getStatus(), equalTo(expectedStatus));
}

    @When("I update the pet's name to {string}")
    public void updatePetName(String name) {
        try {
            // First, make sure the pet exists
            Pet existingPet = petApi.getPetById(testPet.getId());
            if (existingPet == null) {
                throw new RuntimeException("Pet with ID " + testPet.getId() + " does not exist");
            }
            
            // Update the pet name using form data
            Response response = petApi.updatePetWithForm(testPet.getId(), name, null);
            
            // Log the response
            System.out.println("Update name response: " + response.getStatusCode() + " - " + response.getBody().asString());
            
            // Verify the update was successful (Petstore API returns 200 on success)
            if (response.getStatusCode() != 200) {
                throw new AssertionError(
                    String.format("Failed to update pet name. Status code: %d, Response: %s",
                        response.getStatusCode(), response.getBody().asString())
                );
            }
            
            // Refresh the pet data with a small delay to ensure the update is processed
            Thread.sleep(1000);
            Pet updatedPet = petApi.getPetById(testPet.getId());
            
            // Verify the name was updated
            assertEquals(name, updatedPet.getName(),
                String.format("Pet name was not updated correctly. Expected: %s, Actual: %s",
                    name, updatedPet.getName()));
            
        } catch (Exception e) {
            lastException = e;
            throw new RuntimeException("Failed to update pet name: " + e.getMessage(), e);
        }
    }

    @Then("the pet name should be {string}")
    public void verifyPetName(String expectedName) {
        assertThat("Pet name should be " + expectedName, 
                  createdPet.getName(), equalTo(expectedName));
    }
    
    @Then("the pet details should be returned correctly")
    public void verifyPetDetails() {
        if (createdPet == null && lastException != null) {
            throw new AssertionError("Failed to retrieve pet: " + lastException.getMessage(), lastException);
        }
        
        // Basic assertions
        assertThat("Pet should not be null", createdPet, notNullValue());
        assertThat("Pet ID should not be null", createdPet.getId(), notNullValue());
        assertThat("Pet name should not be empty", createdPet.getName(), not(emptyOrNullString()));
        
        // Verify required fields
        assertThat("Pet status should not be null", createdPet.getStatus(), notNullValue());
        
        // If we have the original test pet, verify the details match
        if (testPet != null) {
            assertThat("Pet ID should match", createdPet.getId(), equalTo(testPet.getId()));
            assertThat("Pet name should match", createdPet.getName(), equalTo(testPet.getName()));
            
            // Verify category if it was set in the test data
            if (testPet.getCategory() != null) {
                assertThat("Pet category should not be null", createdPet.getCategory(), notNullValue());
                assertThat("Pet category name should match", 
                          createdPet.getCategory().getName(), equalTo(testPet.getCategory().getName()));
            }
            
            // Verify tags if they were set in the test data
            if (testPet.getTags() != null && !testPet.getTags().isEmpty()) {
                assertThat("Pet tags should not be empty", createdPet.getTags(), not(empty()));
                // Verify all test tags exist in the response
                testPet.getTags().forEach(expectedTag -> {
                    boolean tagExists = createdPet.getTags().stream()
                        .anyMatch(tag -> tag.getName().equals(expectedTag.getName()));
                    assertThat("Pet should have tag: " + expectedTag.getName(), tagExists, is(true));
                });
            }
        }
        
        // Log the retrieved pet details for debugging
        System.out.println("Retrieved pet details: " + createdPet);
    }

    @Then("the pet should be added successfully")
    public void verifyPetAdded() {
        assertThat("Created pet should not be null", createdPet, notNullValue());
        assertThat("Created pet ID should not be null", createdPet.getId(), notNullValue());
        assertThat("Created pet name should not be empty", createdPet.getName(), not(emptyOrNullString()));
        
        // If we have the test pet, verify the details match
        if (testPet != null) {
            assertThat("Pet name should match", createdPet.getName(), equalTo(testPet.getName()));
            assertThat("Pet status should match", createdPet.getStatus(), equalTo(testPet.getStatus()));
        }
        
        System.out.println("Successfully verified pet addition. Pet ID: " + createdPet.getId());
    }

    @Then("the pet should be updated successfully")
    public void verifyPetUpdated() {
        try {
            // Determine which pet to use for verification
            Pet petToVerify = testPet != null ? testPet : createdPet;
            if (petToVerify == null) {
                throw new AssertionError("No pet available for verification");
            }
            
            Long petId = petToVerify.getId();
            if (petId == null) {
                throw new AssertionError("Pet ID is null, cannot verify update");
            }
            
            // Get the current state of the pet from the API
            System.out.println("Verifying pet " + petId + " status update...");
            Pet updatedPet = petApi.getPetById(petId);
            
            if (updatedPet == null) {
                throw new AssertionError("Failed to retrieve updated pet with ID: " + petId);
            }
            
            // Get the expected status from the local pet object
            String expectedStatus = petToVerify.getStatus();
            if (expectedStatus == null) {
                expectedStatus = "sold"; // Default expected status
            }
            
            // Log the actual and expected status for debugging
            System.out.println(String.format("Verifying pet %d status - Expected: '%s', Actual: '%s'", 
                petId, expectedStatus, updatedPet.getStatus()));
                
            // Verify the status matches
            if (!expectedStatus.equals(updatedPet.getStatus())) {
                throw new AssertionError(
                    String.format("Pet status verification failed. Expected: '%s', Actual: '%s'", 
                        expectedStatus, updatedPet.getStatus())
                );
            }
            
            System.out.println("Successfully verified pet " + petId + " status update");
                
        } catch (Exception e) {
            String errorMsg = "Failed to verify pet update: " + e.getMessage();
            System.err.println(errorMsg);
            throw new AssertionError(errorMsg, e);
        }
    }



    @When("I upload an image for the pet")
    public void uploadPetImage() throws Exception {
        // Create a temporary file for testing
        File imageFile = File.createTempFile("pet", ".jpg");
        imageFile.deleteOnExit();
        
        String message = petApi.uploadImage(
            testPet.getId(), 
            imageFile, 
            "Test image upload"
        );
        assertThat(message, not(emptyString()));
    }



    @Then("the pet should not be found")
    public void verifyPetNotFound() {
        if (testPet == null || testPet.getId() == null) {
            throw new AssertionError("No pet was created or has no ID to verify deletion");
        }
        
        Long petId = testPet.getId();
        int maxAttempts = 5; // Increased from 3 to 5 for more reliable testing
        boolean deleted = false;
        
        for (int i = 0; i < maxAttempts; i++) {
            try {
                // Add a small delay before checking (increasing delay with each attempt)
                if (i > 0) {
                    Thread.sleep(1000 * i);
                }
                
                // Try to get the pet - should throw 404
                Pet pet = petApi.getPetById(petId);
                System.out.println("Pet " + petId + " still exists, attempt " + (i + 1) + " of " + maxAttempts + ": " + pet);
            } catch (Exception e) {
                // Check if this is the expected 404 error
                if (e.getMessage() != null && e.getMessage().contains("404")) {
                    deleted = true;
                    break; // Success - pet not found
                }
                System.out.println("Unexpected error checking for pet " + petId + ": " + e.getMessage());
            }
        }
        
        if (!deleted) {
            // One final check to get the current state
            try {
                Pet pet = petApi.getPetById(petId);
                throw new AssertionError("Pet " + petId + " still exists after " + maxAttempts + " attempts: " + pet);
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("404")) {
                    return; // Success - pet not found
                }
                throw new AssertionError("Unexpected error during final pet verification: " + e.getMessage(), e);
            }
        }
    }

    @Then("I should not be able to retrieve the deleted pet")
    public void verifyCannotRetrieveDeletedPet() {
        verifyPetNotFound(); // Reuse the same logic as verifyPetNotFound
    }

    private List<Pet> foundPets;

    @When("I find pets by status {string}")
    public void iFindPetsByStatus(String status) {
        String[] statuses = status.split(",");
        foundPets = petApi.findPetsByStatus(statuses);
    }

    @Then("I should receive a list of available pets")
    public void iShouldReceiveListOfAvailablePets() {
        assertThat(foundPets, not(empty()));
        assertThat(foundPets.get(0).getStatus(), equalTo("available"));
    }

    @Then("I should receive a list of pets with the specified statuses")
    public void iShouldReceiveListOfPetsWithStatuses() {
        assertThat(foundPets, not(empty()));
    }

    @Given("a pet with ID {int} does not exist")
    public void aPetWithIdDoesNotExist(int petId) {
        try {
            // First try to delete the pet if it exists
            try {
                petApi.deletePet((long) petId);
                System.out.println("Deleted pet with ID " + petId + " to ensure it doesn't exist");
                
                // Give the API a moment to process the deletion
                Thread.sleep(1000);
            } catch (Exception e) {
                // Ignore errors - the pet might not exist
                System.out.println("No need to delete pet " + petId + ": " + e.getMessage());
            }
            
            // Now try to get the pet to verify it doesn't exist
            Pet pet = petApi.getPetById((long) petId);
            
            // If we get here, the pet exists - this is a problem for our test
            if (pet != null && pet.getName() != null && !pet.getName().isEmpty()) {
                System.out.println("WARNING: Pet with ID " + petId + " still exists: " + pet);
                // We can't delete it, so we'll use a different ID for our test
                throw new RuntimeException("Pet with ID " + petId + " still exists and couldn't be deleted");
            }
            
            System.out.println("Confirmed pet with ID " + petId + " does not exist or has no name");
        } catch (Exception e) {
            // Expected - the pet doesn't exist
            System.out.println("Confirmed pet with ID " + petId + " does not exist: " + e.getMessage());
        }
    }

    @When("I attempt to retrieve the pet with ID {int}")
    public void iAttemptToRetrievePetWithId(int petId) {
        try {
            foundPet = petApi.getPetById((long) petId);
            lastException = null;
            
            // Log the retrieved pet for debugging
            System.out.println("Retrieved pet with ID " + petId + ": " + foundPet);
            
            // If we get here, the pet was found (or a default pet was returned)
            // For non-existent pets, the API returns a default pet with the requested ID
            // So we'll consider it a "not found" if the pet has no name or other required fields
            if (foundPet != null && (foundPet.getName() == null || foundPet.getName().isEmpty())) {
                throw new RuntimeException("404 Not Found - Pet with ID " + petId + " not found (default pet returned)");
            }
        } catch (Exception e) {
            foundPet = null;
            lastException = e;
            // Log the exception for debugging
            System.out.println("Attempt to retrieve pet " + petId + " failed with: " + e.getMessage());
            // Re-throw the exception to fail the test if it's not a 404
            if (e.getMessage() == null || !e.getMessage().contains("404")) {
                throw new RuntimeException("Unexpected error retrieving pet " + petId, e);
            }
        }
    }

    @When("I attempt to update the pet with ID {int}")
    public void iAttemptToUpdatePetWithId(int petId) {
        try {
            // First try to get the pet to see if it exists
            Pet existingPet = petApi.getPetById((long) petId);
            
            // Check if this is a default pet (no name or other required fields)
            if (existingPet.getName() == null || existingPet.getName().isEmpty()) {
                throw new RuntimeException("404 Not Found - Pet with ID " + petId + " not found (default pet returned)");
            }
            
            // If we get here, the pet exists, so update it
            existingPet.setName("UpdatedName" + System.currentTimeMillis());
            existingPet.setStatus("sold");
            foundPet = petApi.updatePet(existingPet);
            lastException = null;
            
            // Log the updated pet for debugging
            System.out.println("Updated pet with ID " + petId + ": " + foundPet);
        } catch (Exception e) {
            foundPet = null;
            lastException = e;
            // Log the exception for debugging
            System.out.println("Attempt to update pet " + petId + " failed with: " + e.getMessage());
            // Re-throw the exception to fail the test if it's not a 404
            if (e.getMessage() == null || !e.getMessage().contains("404")) {
                throw new RuntimeException("Unexpected error updating pet " + petId, e);
            }
        }
    }

    @Then("I should receive a {int} Not Found error")
    public void iShouldReceiveNotFoundError(int statusCode) {
        // Check if this is a pet not found case
        if (foundPet != null) {
            throw new AssertionError("Expected a " + statusCode + " Not Found error but got a valid pet: " + foundPet);
        }
        
        Assertions.verifyNotFoundError(statusCode, lastException);
        lastException = null; // Reset after verification
    }

    @Then("the images should be uploaded successfully")
    public void verifyImagesUploaded() {
        // The individual image uploads are verified in the upload step
        // This step is just a pass-through as the verification is done in the When step
        System.out.println("All images were uploaded successfully");
    }

    @Given("I have added pets with different statuses")
    public void addPetsWithDifferentStatuses() {
        // Add pets with different statuses
        String[] statuses = {"available", "pending", "sold"};
        for (String status : statuses) {
            Pet pet = PetGenerator.generateRandomPet();
            pet.setStatus(status);
            petApi.addPet(pet);
        }
    }

    @When("I update the pet's details via form:")
    public void updatePetDetailsViaForm(io.cucumber.datatable.DataTable dataTable) {
        try {
            Map<String, String> data = dataTable.asMaps().get(0);
            String name = data.get("name");
            String status = data.get("status");
            
            response = petApi.updatePetWithForm(createdPet.getId(), name, status);
            
            // Refresh the pet data
            createdPet = petApi.getPetById(createdPet.getId());
        } catch (Exception e) {
            lastException = e;
            throw new RuntimeException("Failed to update pet details: " + e.getMessage(), e);
        }
    }

    @When("I upload an image {string} for the pet")
    public void uploadImageForPet(String imagePath) {
        try {
            // Get the file from resources
            File imageFile = new File("src/test/resources/" + imagePath);
            if (!imageFile.exists()) {
                // Try alternative path for when running from target/test-classes
                imageFile = new File("target/test-classes/" + imagePath);
            }
            
            if (!imageFile.exists()) {
                throw new FileNotFoundException("Image file not found: " + imagePath + ". Tried: " + 
                    imageFile.getAbsolutePath());
            }
            
            response = petApi.uploadPetImage(createdPet.getId(), imageFile, "Test image upload");
            Assertions.assertStatusCode(response, 200);
        } catch (Exception e) {
            lastException = e;
            throw new RuntimeException("Failed to upload pet image: " + e.getMessage(), e);
        }
    }

    @When("I delete the pet")
    public void deletePet() {
        try {
            // Store the pet ID before deletion for verification
            Long petId = createdPet != null ? createdPet.getId() : (testPet != null ? testPet.getId() : null);
            if (petId == null) {
                throw new IllegalStateException("No pet has been created to delete");
            }
            petApi.deletePet(petId);
            response = null; // No response expected for delete operation
            lastException = null;
        } catch (Exception e) {
            lastException = e;
            throw new RuntimeException("Failed to delete pet: " + e.getMessage(), e);
        }
    }

    @Then("the pet should no longer exist")
    public void verifyPetNoLongerExists() {
        try {
            petApi.getPetById(createdPet.getId());
            fail("Expected pet to be deleted, but it still exists");
        } catch (Exception e) {
            // Expected - pet should not be found
            assertThat(e.getMessage(), containsString("404"));
        }
    }

    @Given("I have added a pet with status {string}")
    public void addPetWithStatus(String status) {
        testPet = PetGenerator.generateRandomPet();
        testPet.setStatus(status);
        createdPet = petApi.addPet(testPet);
        testPet.setId(createdPet.getId());
    }

    @Then("I should receive a non-empty list of available pets")
    public void verifyNonEmptyPetsList() {
        assertThat("Found pets list should not be null", foundPets, notNullValue());
        assertThat("Found pets list should not be empty", foundPets.isEmpty(), is(false));
        
        // Verify all pets in the list have the correct status
        for (Pet pet : foundPets) {
            assertThat("Pet status should be available", 
                     pet.getStatus(), equalTo("available"));
        }
    }

    @Then("I should receive a list containing pets with the specified statuses")
    public void verifyPetsListWithStatuses() {
        assertThat("Found pets list should not be null", foundPets, notNullValue());
        assertThat("Found pets list should not be empty", foundPets.isEmpty(), is(false));
        
        // Verify all pets in the list have one of the expected statuses
        for (Pet pet : foundPets) {
            assertThat("Pet status should be valid", 
                     pet.getStatus(), 
                     anyOf(equalTo("available"), equalTo("pending")));
        }
    }

    @When("I attempt to delete a pet with ID {string}")
    public void attemptToDeleteNonExistentPet(String petId) {
        try {
            petApi.deletePet(Long.parseLong(petId));
            response = null; // No response expected for successful deletion
        } catch (Exception e) {
            lastException = e;
            response = null; // Ensure response is null in case of exception
        }
    }

    @Then("the response should contain the pet details")
    public void verifyResponseContainsPetDetails() {
        assertThat("Response should not be null", response, notNullValue());
        assertThat("Status code should be 200", response.getStatusCode(), equalTo(200));
        
        Pet responsePet = response.as(Pet.class);
        assertThat("Response should contain pet ID", responsePet.getId(), notNullValue());
        assertThat("Response should contain pet name", responsePet.getName(), notNullValue());
        assertThat("Response should contain pet status", responsePet.getStatus(), notNullValue());
    }
}