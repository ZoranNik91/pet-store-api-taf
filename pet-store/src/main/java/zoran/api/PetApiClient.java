package zoran.api;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import zoran.config.ApiConfig;
import zoran.models.Pet;
import java.util.Map;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

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
        try {
            System.out.println("Adding new pet to store: " + pet);
            
            // Log the request details
            System.out.println("Sending POST request to /pet with body: " + 
                "ID=" + pet.getId() + 
                ", Name=" + pet.getName() + 
                ", Status=" + pet.getStatus());
            
            Response response = post(pet, "/pet");
            
            // Log the response details
            int statusCode = response.getStatusCode();
            String responseBody = response.getBody().asString();
            System.out.println("Add pet response - Status: " + statusCode + ", Body: " + responseBody);
            
            // Check for error status codes
            if (statusCode != 200) {
                String errorMsg = String.format("Failed to add pet. Status: %d, Response: %s", 
                    statusCode, responseBody);
                System.err.println(errorMsg);
                throw new RuntimeException(errorMsg);
            }
            
            // Extract and return the created pet
            Pet createdPet = response.then()
                .extract()
                .as(Pet.class);
                
            System.out.println("Successfully added pet with ID: " + createdPet.getId());
            return createdPet;
            
        } catch (Exception e) {
            String errorMsg = "Error adding pet: " + e.getMessage();
            System.err.println(errorMsg);
            throw new RuntimeException(errorMsg, e);
        }
    }

    /**
     * Update an existing pet
     * @param pet The pet data to update
     * @return The updated pet
     */
    public Pet updatePet(Pet pet) {
        Response response = put(pet, "/pet");
        return response.then()
                .statusCode(200)
                .extract()
                .as(Pet.class);
    }

    /**
     * Upload an image for a pet
     * @param petId ID of pet to update
     * @param file File to upload
     * @param additionalMetadata Additional data to pass to server
     * @return Response from the server
     */
    public Response uploadPetImage(Long petId, File file, String additionalMetadata) {
        RequestSpecification spec = given()
                .spec(requestSpec)
                .contentType("multipart/form-data")
                .multiPart("file", file);
                
        if (additionalMetadata != null) {
            spec = spec.multiPart("additionalMetadata", additionalMetadata);
        }
        
        return spec
                .when()
                .post("/pet/{petId}/uploadImage", petId)
                .then()
                .statusCode(200)
                .extract()
                .response();
    }

    /**
     * Find pets by status
     * @param status Status values that need to be considered for filter
     * @return List of pets matching the status
     */
    public List<Pet> findPetsByStatus(String... status) {
        return given()
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
            Response response = given()
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
    /**
     * Updates a pet in the store with form data
     * @param petId ID of pet that needs to be updated
     * @param name Updated name of the pet (optional)
     * @param status Updated status of the pet (optional)
     * @return Response object from the API
     */
    /**
     * Updates a pet in the store with form data
     * @param petId ID of pet that needs to be updated
     * @param name Updated name of the pet (optional)
     * @param status Updated status of the pet (optional)
     * @return Response object from the API
     */
    public Response updatePetWithForm(Long petId, String name, String status) {
        if (petId == null) {
            throw new IllegalArgumentException("Pet ID cannot be null");
        }
        
        try {
            // Log the request
            System.out.println("Updating pet " + petId + " - Name: " + name + ", Status: " + status);
            
            // Build the form data
            RequestSpecBuilder requestBuilder = new RequestSpecBuilder()
                    .setBaseUri(BASE_URL)
                    .setContentType("application/x-www-form-urlencoded; charset=utf-8")
                    .addHeader("api_key", "special-key");
            
            // Build form parameters
            Map<String, String> formParams = new HashMap<>();
            if (name != null) {
                formParams.put("name", name);
            }
            if (status != null) {
                formParams.put("status", status);
            }
            
            // Log the form parameters
            System.out.println("Form parameters: " + formParams);
            
            // Add form parameters to the request
            if (!formParams.isEmpty()) {
                requestBuilder.addFormParams(formParams);
            }
            
            // Make the request
            return given()
                    .spec(requestBuilder.build())
                    .when()
                    .post("/pet/{petId}", petId)
                    .then()
                    .extract()
                    .response();
                    
        } catch (Exception e) {
            String errorMsg = "Failed to update pet with form: " + e.getMessage();
            System.err.println(errorMsg);
            throw new RuntimeException(errorMsg, e);
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
        return given()
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