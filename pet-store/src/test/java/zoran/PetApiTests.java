package zoran;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import zoran.base.BaseApiTest;
import zoran.models.Pet;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

class PetApiTests extends BaseApiTest {
    private static Long createdPetId;

    @Test
    @Order(1)
    void createPet_shouldSucceed() {
        Pet testPet = Pet.builder()
                .id(0L)
                .name("Fluffy")
                .status("available")
                .category(Pet.Category.builder()
                        .id(1L)
                        .name("Dogs")
                        .build())
                .build();
    }

    @Test
    @Order(2)
    void getPetById_shouldReturnCreatedPet() {
        given()
            .header("api_key", config.getApiKey())
        .when()
            .get("/pet/{id}", createdPetId)
        .then()
            .statusCode(200)
            .body("id", equalTo(createdPetId.intValue()))
            .body("name", equalTo("Fluffy"));
    }

    @AfterAll
    static void cleanUp() {
        if (createdPetId != null) {
            given()
                .header("api_key", config.getApiKey())
            .when()
                .delete("/pet/{id}", createdPetId)
            .then()
                .statusCode(200);
        }
    }
}