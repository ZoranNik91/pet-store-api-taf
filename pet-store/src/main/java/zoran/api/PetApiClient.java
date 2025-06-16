package zoran.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import zoran.config.ApiConfig;
import zoran.models.Pet;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Client for interacting with the Pet API endpoints
 */
public class PetApiClient extends BaseApiClient {
    
    public PetApiClient() {
        super();
    }

    /**
     * Add a new pet to the store
     * @param pet The pet to add
     * @return The added pet with generated ID
     */
    public Pet addPet(Pet pet) {
        return post(pet, "/pet")
                .then()
                .statusCode(200)
                .extract()
                .as(Pet.class);
    }

    /**
     * Update an existing pet
     * @param pet The pet data to update
     * @return The updated pet
     */
    public Pet updatePet(Pet pet) {
        Response response = put(pet, "/pet");
        
        if (response.getStatusCode() == 404) {
            throw new RuntimeException("404 Not Found - Pet with ID " + pet.getId() + " not found");
        }
        
        return response.then()
                .statusCode(200)
                .extract()
                .as(Pet.class);
    }

    /**
     * Find pets by status
     * @param status Status values that need to be considered for filter
     * @return List of pets matching the status
     */
    public List<Pet> findPetsByStatus(String... status) {
        return RestAssured.given()
                .spec(requestSpec)
                .queryParam("status", (Object[]) status)
                .when()
                .get("/pet/findByStatus")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("", Pet.class);
    }

    /**
     * Find pet by ID
     * @param petId ID of pet to return
     * @return The pet with the specified ID
     */
    public Pet getPetById(Long petId) {
        try {
            System.out.println("Attempting to get pet with ID: " + petId);
            
            // Make the request directly instead of using the base get() method
            // so we can check the status code before extracting the response
            Response response = RestAssured.given()
                .spec(requestSpec)
                .when()
                .get("/pet/{petId}", petId);
            
            int statusCode = response.getStatusCode();
            System.out.println("Response status code for pet ID " + petId + ": " + statusCode);
            System.out.println("Response body: " + response.getBody().asString());
                
            if (statusCode == 404) {
                String errorMsg = "404 Not Found - Pet with ID " + petId + " not found";
                System.out.println(errorMsg);
                throw new RuntimeException(errorMsg);
            }
            
            // If we get here, the status code is not 404, so proceed with extraction
            Pet pet = response.then()
                    .statusCode(200)  // This will throw an exception if status is not 200
                    .extract()
                    .as(Pet.class);
            
            System.out.println("Successfully retrieved pet: " + pet);
            return pet;
                    
        } catch (Exception e) {
            System.err.println("Exception in getPetById for pet ID " + petId + ": " + e.getMessage());
            // If this is already our custom exception, rethrow it
            if (e.getMessage() != null && e.getMessage().contains("404")) {
                throw e;
            }
            throw new RuntimeException("Failed to get pet by ID: " + e.getMessage(), e);
        }
    }

    /**
     * Updates a pet in the store with form data
     * @param petId ID of pet that needs to be updated
     * @param formParams Form parameters to update
     */
    /**
     * Updates a pet in the store with form data
     * @param petId ID of pet that needs to be updated
     * @param formParams Form parameters to update (name, status, etc.)
     * @return The updated pet
     */
    public Pet updatePetWithForm(Long petId, Map<String, String> formParams) {
        try {
            // First, get the current pet to preserve existing data
            Pet currentPet = getPetById(petId);
            
            // Update the pet with form parameters
            if (formParams.containsKey("name")) {
                currentPet.setName(formParams.get("name"));
            }
            if (formParams.containsKey("status")) {
                currentPet.setStatus(formParams.get("status"));
            }
            
            // Update the pet with the modified data
            return updatePet(currentPet);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to update pet with form: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a pet
     * @param petId Pet id to delete
     */
    public void deletePet(Long petId) {
        if (petId == null) {
            throw new IllegalArgumentException("Pet ID cannot be null");
        }
        
        // Delete the pet
        try {
            Response response = delete("/pet/{petId}", petId);
            
            // Check if deletion was successful
            if (response.getStatusCode() != 200) {
                // Special case: If pet doesn't exist, the API returns 404
                if (response.getStatusCode() == 404) {
                    System.out.println("Pet with ID " + petId + " not found during deletion (404)");
                    return; // Consider this a success for idempotency
                }
                throw new RuntimeException("Failed to delete pet with ID " + petId + 
                                  ". Status code: " + response.getStatusCode());
            }
            
            // Verify the pet is actually deleted with retries
            int maxRetries = 3;
            int retryCount = 0;
            boolean deleted = false;
            
            while (retryCount < maxRetries && !deleted) {
                try {
                    // Add a small delay before checking
                    Thread.sleep(1000 * (retryCount + 1));
                    
                    // Try to get the pet - should throw 404
                    getPetById(petId);
                    
                    // If we get here, the pet still exists
                    retryCount++;
                    System.out.println("Pet " + petId + " still exists, retry " + retryCount + "/" + maxRetries);
                } catch (Exception e) {
                    // Check if this is the expected 404 error
                    if (e.getMessage() != null && e.getMessage().contains("404")) {
                        deleted = true;
                    } else {
                        // Unexpected error
                        throw new RuntimeException("Unexpected error verifying pet deletion: " + e.getMessage(), e);
                    }
                }
            }
            
            if (!deleted) {
                throw new RuntimeException("Pet with ID " + petId + " might not have been deleted successfully after " + maxRetries + " retries");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error during pet deletion: " + e.getMessage(), e);
        }
    }

    /**
     * Uploads an image for a pet
     * @param petId ID of pet to update
     * @param file Image file to upload
     * @param additionalMetadata Additional data to pass to server
     * @return The API response message
     */
    /**
     * Uploads an image for a pet
     * @param petId ID of pet to update
     * @param file Image file to upload
     * @param additionalMetadata Additional data to pass to server
     * @return The API response message
     */
    public String uploadImage(Long petId, File file, String additionalMetadata) {
        return RestAssured.given()
                .spec(requestSpec)
                .contentType(ContentType.MULTIPART)
                .multiPart("file", file)
                .formParam("additionalMetadata", additionalMetadata)
                .when()
                .post("/pet/{petId}/uploadImage", petId)
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getString("message");
    }
}