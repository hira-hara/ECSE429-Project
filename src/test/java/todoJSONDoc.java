import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.ProcessBuilder;

// Ivan

import static io.restassured.RestAssured.*;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

public class todoJSONDoc {
    private static final String BASE_URL = "http://localhost:4567";

    @BeforeAll
    static void init() {
        ProcessBuilder proc = new ProcessBuilder("java", "-jar", "runTodoManagerRestAPI-1.5.5.jar");
        try {
            proc.start();
        } catch (IOException e) {
            System.err.println("An I/O error occurred: " + e.getMessage());
        }

        RestAssured.baseURI = BASE_URL;
        try {
            given().get("/projects").then().statusCode(200);
        } catch (Exception e) {
            Assumptions.abort("Service is not running at " + BASE_URL + ". Skipping tests." + e.getMessage());
        }
    }

    @AfterAll
    static void shutdown() {
        try {
            given().get("/shutdown");
        } catch (Exception e) {
            System.err.println("An I/O error occurred: " + e.getMessage());

        }
    }

@Test
    @DisplayName("GET /todo JSON")
    void testGetProject() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/todos");
    }
}
