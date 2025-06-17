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

    @Test
    @Order(2)
    @DisplayName("Should retrieve a pet by ID after creating it")
    void getPetById_shouldReturnCreatedPet() {
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
        
        // Create a new test pet with a unique name
        String uniquePetName = "testPet_" + System.currentTimeMillis();
        
        // Create a pet object with a unique ID
        Long petId = System.currentTimeMillis() % 1000000; // Keep the number smaller to avoid potential issues
        
        Pet pet = new Pet();
        pet.setId(petId);
        pet.setName(uniquePetName);
        pet.setStatus("available");
        
        // Log test details
        logToFile("Test pet details - ID: " + petId + ", Name: " + uniquePetName);
        
        // 1. Add a new pet to the store
        logToFile("1. Sending POST request to /pet");
        Response addResponse = given()
            .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
            .contentType(ContentType.JSON)
            .header("api_key", config.getApiKey())
            .log().all()
            .body(pet)
        .when()
            .post("/pet");
            
        // Log add response
        logToFile("Add pet response status: " + addResponse.getStatusCode());
        String addResponseBody = addResponse.getBody().asString();
        logToFile("Add pet response body: " + addResponseBody);
        
        // Verify the pet was added successfully
        addResponse.then().statusCode(200);
        
        // Parse the response to get the actual ID if the server generated one
        try {
            Pet createdPet = addResponse.as(Pet.class);
            if (createdPet != null && createdPet.getId() != null) {
                petId = createdPet.getId(); // Use the ID from the response
                logToFile("Created pet ID from response: " + petId);
                logToFile("Created pet name from response: " + createdPet.getName());
            }
        } catch (Exception e) {
            logToFile("Failed to parse add pet response: " + e.getMessage());
        }
        
        // Small delay to ensure the pet is available
        try {
            logToFile("Waiting 2 seconds before retrieving the pet...");
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 2. Get the pet by ID
        logToFile("2. Sending GET request to /pet/" + petId);
        Response getResponse = given()
            .header("api_key", config.getApiKey())
            .accept(ContentType.JSON)
            .log().all()
        .when()
            .get("/pet/" + petId);
            
        // Log get response
        int statusCode = getResponse.getStatusCode();
        logToFile("Get pet response status: " + statusCode);
        String responseBody = getResponse.getBody().asString();
        logToFile("Get pet response body: " + responseBody);
        
        // If we got a 404, investigate further
        if (statusCode == 404) {
            logToFile("Pet not found with ID: " + petId);
            
            // Try to find the pet by status
            logToFile("3. Searching for pet by status...");
            
            // Check available pets
            logToFile("Checking available pets...");
            Response availablePets = given()
                .header("api_key", config.getApiKey())
                .queryParam("status", "available")
            .when()
                .get("/pet/findByStatus");
            logToFile("Available pets: " + availablePets.getBody().asString());
            
            // Check pending pets
            logToFile("Checking pending pets...");
            Response pendingPets = given()
                .header("api_key", config.getApiKey())
                .queryParam("status", "pending")
            .when()
                .get("/pet/findByStatus");
            logToFile("Pending pets: " + pendingPets.getBody().asString());
            
            // Check sold pets
            logToFile("Checking sold pets...");
            Response soldPets = given()
                .header("api_key", config.getApiKey())
                .queryParam("status", "sold")
            .when()
                .get("/pet/findByStatus");
            logToFile("Sold pets: " + soldPets.getBody().asString());
        }
        
        // 4. Verify the response
        logToFile("4. Verifying response status code");
        try {
            getResponse.then().statusCode(200);
            logToFile("Status code verification passed (200)");
        } catch (AssertionError e) {
            logToFile("Status code verification failed: " + e.getMessage());
            logToFile("Response status: " + getResponse.getStatusCode());
            logToFile("Response body: " + getResponse.getBody().asString());
            logToFile("Response headers: " + getResponse.getHeaders());
            throw e; // Re-throw to fail the test
        }
        
        // 5. Parse and verify the pet details
        logToFile("5. Parsing and verifying pet details");
        Pet responsePet = getResponse.as(Pet.class);
        try {
            assertNotNull(responsePet, "Response pet should not be null");
            assertNotNull(responsePet.getId(), "Pet ID should not be null");
            assertEquals(petId, responsePet.getId(), "Pet ID should match the created pet's ID");
            assertEquals(uniquePetName, responsePet.getName(), "Pet name should match");
            logToFile("Successfully verified pet details");
            
            // 6. Clean up - delete the test pet
            logToFile("6. Cleaning up - Deleting pet with ID: " + petId);
            given()
                .header("api_key", config.getApiKey())
            .when()
                .delete("/pet/" + petId)
            .then()
                .statusCode(200);
                
            // 7. Verify the pet was deleted
            logToFile("7. Verifying pet was deleted...");
            given()
                .header("api_key", config.getApiKey())
            .when()
                .get("/pet/" + petId)
            .then()
                .statusCode(404);
                
            logToFile("=== Test completed successfully ===\n");
        } finally {
            // Ensure cleanup happens even if assertions fail
            try {
                logToFile("Ensuring cleanup of pet with ID: " + petId);
                given()
                    .header("api_key", config.getApiKey())
                .when()
                    .delete("/pet/" + petId);
            } catch (Exception e) {
                logToFile("Cleanup failed (this is not critical): " + e.getMessage());
            }
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