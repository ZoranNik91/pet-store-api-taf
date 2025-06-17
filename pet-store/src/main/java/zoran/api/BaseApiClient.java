package zoran.api;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class BaseApiClient {
    protected static final String BASE_URL = "https://petstore.swagger.io/v2";
    protected RequestSpecification requestSpec;

    public BaseApiClient() {
        this.requestSpec = new RequestSpecBuilder()
                .setBaseUri(BASE_URL)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addHeader("api_key", "special-key") // Add API key for all requests
                .build();
    }

    protected Response get(String path, Object... pathParams) {
        try {
            return RestAssured.given()
                    .spec(requestSpec)
                    .when()
                    .get(path, pathParams)
                    .then()
                    .extract()
                    .response();
        } catch (Exception e) {
            throw new RuntimeException("GET request failed for path: " + path, e);
        }
    }

    protected <T> Response post(T body, String path, Object... pathParams) {
        try {
            return RestAssured.given()
                    .spec(requestSpec)
                    .body(body)
                    .when()
                    .post(path, pathParams)
                    .then()
                    .extract()
                    .response();
        } catch (Exception e) {
            throw new RuntimeException("POST request failed for path: " + path, e);
        }
    }

    protected <T> Response put(T body, String path, Object... pathParams) {
        try {
            return RestAssured.given()
                    .spec(requestSpec)
                    .body(body)
                    .when()
                    .put(path, pathParams)
                    .then()
                    .extract()
                    .response();
        } catch (Exception e) {
            throw new RuntimeException("PUT request failed for path: " + path, e);
        }
    }

    protected Response delete(String path, Object... pathParams) {
        try {
            return RestAssured.given()
                    .spec(requestSpec)
                    .when()
                    .delete(path, pathParams)
                    .then()
                    .extract()
                    .response();
        } catch (Exception e) {
            throw new RuntimeException("DELETE request failed for path: " + path, e);
        }
    }
}
