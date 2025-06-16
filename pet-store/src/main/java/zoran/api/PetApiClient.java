package zoran.api;

import zoran.config.Config;
import zoran.models.Pet;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class PetApiClient {
    private final Config config;

    public PetApiClient() {
        this.config = new Config();
        RestAssured.baseURI = config.getBaseUrl();
    }

    public Response addPet(Pet pet) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .header("api_key", config.getApiKey())
                .body(pet)
                .post("/pet");
    }

    public Response getPetById(Long petId) {
        return RestAssured.given()
                .header("api_key", config.getApiKey())
                .pathParam("petId", petId)
                .get("/pet/{petId}");
    }

    public Response updatePet(Pet pet) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .header("api_key", config.getApiKey())
                .body(pet)
                .put("/pet");
    }
}