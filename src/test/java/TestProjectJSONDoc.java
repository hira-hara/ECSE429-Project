
import static io.restassured.RestAssured.*;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import static org.hamcrest.Matchers.*;

import java.util.ArrayList;

import org.junit.jupiter.api.*;

import io.restassured.response.Response;

@TestMethodOrder(MethodOrderer.Random.class) // In order to run in any order
public class TestProjectJSONDoc {
    private static final String BASE_URL = "http://localhost:4567";
    private String testProjectId;

    @BeforeAll
    static void ServiceRunningCheck() {
        RestAssured.baseURI = BASE_URL;
        try {
            given().get("/projects").then().statusCode(200);
        } catch (Exception e) {
            Assumptions.abort("Service is not running at " + BASE_URL + ". Skipping tests.");
        }
    }

    @BeforeEach
    void setUp() { // Set up initial conditions and save system state
        Response response = given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"Initial Project\", \"description\":\"Setup state\"}")
                .post("/projects");

        testProjectId = response.jsonPath().getString("id");
    }

    @AfterEach
    void tearDown() { // Restore system to initial state
        if (testProjectId != null) {
            given().delete("/projects/" + testProjectId);
        }
    }

    /* JSON TESTS */

    // endpoint: /projects
    @Test
    @DisplayName("GET /projects JSON")
    void testGetProject() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/projects")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", greaterThan(0))
                .body("projects", hasSize(greaterThan(0)));
    }

    @Test
    @DisplayName("POST /projects JSON")
    void testCreateProject() {
        Response response = given()
            .contentType(ContentType.JSON)
            .body("{\"title\":\"New Project\", \"completed\":false, \"description\":\"new project to do\"}")
        .when()
            .post("/projects");

        String newId = response.jsonPath().getString("id"); // Capture id for immediate cleanup

        try {
            response.then()
                .statusCode(201)
                .body("title", equalTo("New Project"));
        } finally { // Clean up to return to state
            if (newId != null) {
                given().delete("/projects/" + newId);
            }
        }
    }

    // endpoint: /projects/:id
    @Test
    @DisplayName("GET /projects/:id JSON")
    void testGetProjectID() {
        System.out.println("TEST PROJECT: " + testProjectId);
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/projects/" + testProjectId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("projects[0].id", equalTo(testProjectId));
    }


    @Test
    @DisplayName("POST /projects/:id JSON")
    void testCreateUpdateDeleteFlow() {
        Response createResponse = given() // New project acting as old one
                .contentType(ContentType.JSON)
                .body("{\"title\":\"Old Project\", \"completed\":false}")
            .when()
                .post("/projects");

        createResponse.then().statusCode(201);
        String localId = createResponse.jsonPath().getString("id");

        try { // Update parameters
            given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"Done Project\", \"completed\":true}")
            .when()
                .post("/projects/" + localId) 
            .then()
                .log().all()
                .statusCode(200) 
                .body("title", equalTo("Done Project"))
                .body("completed", equalTo("true"));

        } finally {
            // Delete project
            if (localId != null) {
                given()
                    .delete("/projects/" + localId)
                .then()
                    .statusCode(200); 
            }
        }
    }

    // TESTING UNEXPECTED INPUTS
    @Test
    @DisplayName("DELETE /projects/:id JSON")
    void testDeleteProjectIDErr() {
        given()
            .accept(ContentType.JSON)
            .when()
            .delete("/projects/" + 40000)
            .then()
            .statusCode(404)
            .contentType(ContentType.JSON)
            .body("errorMessages[0]", equalTo("Could not find any instances with projects/40000"));
    }



    // test a post /projects/undefined or null

}

