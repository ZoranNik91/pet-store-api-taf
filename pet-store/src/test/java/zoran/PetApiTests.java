package zoran;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import zoran.base.BaseApiTest;
import zoran.models.Pet;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.config.EncoderConfig.encoderConfig;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class PetApiTests extends BaseApiTest {
    private static final String LOG_FILE = "target/pet-api-test.log";
    
    private void logToFile(String message) {
        try (PrintWriter out = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            out.println("[" + timestamp + "] " + message);
            System.out.println("[" + timestamp + "] " + message); // Also log to console
        } catch (Exception e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }
    private static Long createdPetId;

    @Test
    @Order(1)
    void createPet_shouldSucceed() {
        Pet testPet = Pet.builder()
                .id(0L)
                .name("doggie")
                .status("available")
                .category(Pet.Category.builder()
                        .id(1L)
                        .name("Dogs")
                        .build())
                .build();
                
        createdPetId = given()
            .contentType("application/json")
            .body(testPet)
        .when()
            .post("/pet")
        .then()
            .statusCode(200)
            .extract().path("id");
            
        assertNotNull(createdPetId, "Pet ID should not be null after creation");
    }

    private Response getPetWithRetry(long petId, int maxRetries, long delayMs) throws InterruptedException {
        logToFile("Attempting to retrieve pet with ID: " + petId);
        Response response = null;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                logToFile(String.format("Attempt %d/%d to get pet with ID: %d", attempt, maxRetries, petId));
                response = given()
                    .header("api_key", "testApiKey")
                    .when()
                    .get("/pet/" + petId);
                
                if (response.getStatusCode() == 200) {
                    logToFile("Successfully retrieved pet on attempt " + attempt);
                    return response;
                }
                
                logToFile(String.format("Attempt %d failed with status %d. Response: %s", 
                    attempt, response.getStatusCode(), response.getBody().asString()));
                
                if (attempt < maxRetries) {
                    logToFile("Waiting " + delayMs + "ms before next retry...");
                    Thread.sleep(delayMs);
                }
            } catch (Exception e) {
                logToFile("Error during attempt " + attempt + ": " + e.getMessage());
                if (attempt == maxRetries) {
                    throw new AssertionError("Failed to retrieve pet after " + maxRetries + " attempts", e);
                }
                Thread.sleep(delayMs);
            }
        }
        return response;
    }
    
    private Response addPetWithRetry(Pet pet, int maxRetries, long delayMs) throws InterruptedException {
        logToFile("Attempting to add pet with ID: " + pet.getId());
        Response response = null;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                logToFile(String.format("Attempt %d/%d to add pet", attempt, maxRetries));
                response = given()
                    .header("api_key", "testApiKey")
                    .contentType(ContentType.JSON)
                    .body(pet)
                    .when()
                    .post("/pet");
                
                if (response.getStatusCode() == 200) {
                    logToFile("Successfully added pet on attempt " + attempt);
                    return response;
                }
                
                logToFile(String.format("Attempt %d failed with status %d. Response: %s", 
                    attempt, response.getStatusCode(), response.getBody().asString()));
                
                if (attempt < maxRetries) {
                    logToFile("Waiting " + delayMs + "ms before next retry...");
                    Thread.sleep(delayMs);
                }
            } catch (Exception e) {
                logToFile("Error during attempt " + attempt + ": " + e.getMessage());
                if (attempt == maxRetries) {
                    throw new AssertionError("Failed to add pet after " + maxRetries + " attempts", e);
                }
                Thread.sleep(delayMs);
            }
        }
        return response;
    }
    
    @Test
    @Order(2)
    @DisplayName("Should retrieve a pet by ID after creating it")
    void getPetById_shouldReturnCreatedPet() throws InterruptedException {
        // Print system properties for debugging
        System.out.println("\n=== System Properties ===");
        System.getProperties().forEach((key, value) -> 
            System.out.println(key + " = " + value)
        );
        
        // Print environment variables for debugging
        System.out.println("\n=== Environment Variables ===");
        System.getenv().forEach((key, value) -> 
            System.out.println(key + " = " + value)
        );
        
        // Print current working directory
        System.out.println("\nCurrent working directory: " + System.getProperty("user.dir"));
        
        // Clear the log file at the start of the test
        try (PrintWriter writer = new PrintWriter(LOG_FILE)) {
            writer.print("");
        } catch (Exception e) {
            System.err.println("Failed to clear log file: " + e.getMessage());
        }
        
        logToFile("=== Starting test: getPetById_shouldReturnCreatedPet ===");
        
        // Enable detailed logging for all requests and responses
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        
        // Create a new test pet with a unique ID and name
        long uniqueId = System.currentTimeMillis() % 1000000; // Last 6 digits of current time
        String petName = "testPet_" + System.currentTimeMillis();
        Pet testPet = new Pet();
        testPet.setId(uniqueId);
        testPet.setName(petName);
        testPet.setStatus("available");

        logToFile("Test pet details - ID: " + uniqueId + ", Name: " + petName);
        
        // 1. Add a new pet to the store with retry logic
        logToFile("1. Sending POST request to /pet");
        Response addResponse = addPetWithRetry(testPet, 3, 2000);
        
        // 2. Verify the pet was added successfully
        addResponse.then().statusCode(200);
        
        // 3. Get the created pet ID from the response
        Pet createdPet = addResponse.as(Pet.class);
        long createdPetId = createdPet.getId();
        logToFile("Created pet ID from response: " + createdPetId);
        
        // 4. Try to get the pet with retry logic
        logToFile("2. Attempting to retrieve the pet with retry logic");
        Response getResponse = getPetWithRetry(createdPetId, 5, 2000);
        
        // 5. Verify the response
        logToFile("3. Verifying the pet details");
        getResponse.then().statusCode(200);
        
        Pet retrievedPet = getResponse.as(Pet.class);
        assertNotNull(retrievedPet, "Retrieved pet should not be null");
        assertEquals(createdPetId, retrievedPet.getId(), "Pet ID should match");
        assertEquals(petName, retrievedPet.getName(), "Pet name should match");
        
        logToFile("=== Test completed successfully ===");
        
        // Clean up - delete the test pet
        try {
            logToFile("Cleaning up - Deleting pet with ID: " + createdPetId);
            given()
                .header("api_key", "testApiKey")
            .when()
                .delete("/pet/" + createdPetId)
            .then()
                .statusCode(200);
                
            // Verify the pet was deleted
            logToFile("Verifying pet was deleted...");
            given()
                .header("api_key", "testApiKey")
            .when()
                .get("/pet/" + createdPetId)
            .then()
                .statusCode(404);
                
            logToFile("Pet successfully deleted");
        } catch (Exception e) {
            logToFile("Cleanup failed (this is not critical): " + e.getMessage());
        }
    }

    @AfterAll
    static void cleanUp() {
        if (createdPetId != null) {
            try {
                given()
                    .header(config.getApiKey() != null && !config.getApiKey().isEmpty() ? "api_key" : "", 
                            config.getApiKey() != null ? config.getApiKey() : "")
                .when()
                    .delete("/pet/" + createdPetId)
                .then()
                    .statusCode(200);
            } catch (Exception e) {
                System.err.println("Failed to clean up pet with ID " + createdPetId + ": " + e.getMessage());
            }
        }
    }
}