import static io.restassured.RestAssured.*;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.*;
import io.restassured.response.Response;

import java.io.IOException;

@TestMethodOrder(MethodOrderer.Random.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestProjectJSONDoc {

    private static final String BASE_URL = "http://localhost:4567";
    private Process serverProcess;
    private String testProjectId;

    // -------------------------------
    // START/STOP SERVER
    // -------------------------------

    @BeforeAll
    void startServer() {
        try {
            ProcessBuilder proc = new ProcessBuilder(
                    "java", "-jar", "runTodoManagerRestAPI-1.5.5.jar"
            );
            serverProcess = proc.start();
        } catch (IOException e) {
            System.err.println("Could not start server: " + e.getMessage());
        }

        RestAssured.baseURI = BASE_URL;

        // Wait/check if service is running
        try {
            given().get("/projects").then().statusCode(200);
        } catch (Exception e) {
            Assumptions.abort("Service is not running at " + BASE_URL + ". Skipping tests.");
        }
    }

    @AfterAll
    void stopServer() {
        try {
            given().get("/shutdown"); // if your API supports it
        } catch (Exception ignored) {
        }
        if (serverProcess != null) {
            serverProcess.destroy();
        }
    }

    // -------------------------------
    // RESET TEST STATE BEFORE EACH
    // -------------------------------

    @BeforeEach
    void setUp() {
        // Clean all projects (optional: adjust range based on expected IDs)
        for (int i = 1; i <= 50; i++) {
            given()
                .accept(ContentType.JSON)
            .when()
                .delete("/projects/" + i);
        }

        // Ensure at least one test project exists
        Response response = given()
                .contentType(ContentType.JSON)
                .when()
                .get("/projects");

        testProjectId = response.jsonPath().getString("projects[-1].id");

        if (testProjectId == null) {
            Response createResponse = given()
                    .contentType(ContentType.JSON)
                    .body("{\"title\":\"Initial Project\"}")
                    .post("/projects");
            testProjectId = createResponse.jsonPath().getString("id");
        }
    }

    // -------------------------------
    // JSON TESTS
    // -------------------------------

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

        String newId = response.jsonPath().getString("id");

        try {
            response.then()
                    .statusCode(201)
                    .body("title", equalTo("New Project"));
        } finally {
            if (newId != null) {
                given().delete("/projects/" + newId);
            }
        }
    }

    @Test
    @DisplayName("GET /projects/:id JSON")
    void testGetProjectID() {
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
        Response createResponse = given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"Old Project\", \"completed\":false}")
                .when()
                .post("/projects");

        createResponse.then().statusCode(201);
        String localId = createResponse.jsonPath().getString("id");

        try {
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
            if (localId != null) {
                given()
                        .delete("/projects/" + localId)
                        .then()
                        .statusCode(200);
            }
        }
    }

    @Test
    @DisplayName("GET /projects/:id/tasks JSON")
    void testGetProjectIDTasks() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/projects/" + testProjectId + "/tasks")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", greaterThan(0))
                .body("todos", hasSize(greaterThan(0)));
    }

    @Test
    @DisplayName("POST /projects/:id/tasks JSON")
    void testPostProjectTasks() {
        Response createResponse = given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"Old Project\", \"completed\":false}")
                .when()
                .post("/projects");

        createResponse.then().statusCode(201);
        String localId = createResponse.jsonPath().getString("id");

        try {
            given()
                    .contentType(ContentType.JSON)
                    .body("{\"id\":\"1\"}")
                    .when()
                    .post("/projects/" + localId + "/tasks")
                    .then()
                    .log().all()
                    .statusCode(201);
        } finally {
            if (localId != null) {
                given()
                        .delete("/projects/" + localId)
                        .then()
                        .statusCode(200);
            }
        }
    }

    @Test
    @DisplayName("HEAD /projects/:id/tasks JSON")
    void testHeadProjectIDTasks() {
        given()
                .accept(ContentType.JSON)
                .when()
                .head("/projects/" + testProjectId + "/tasks")
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("DELETE /projects/:id NON EXISTENT ID")
    void testDeleteProjectIDErr() {
        given()
                .accept(ContentType.JSON)
                .when()
                .delete("/projects/40000")
                .then()
                .statusCode(404)
                .contentType(ContentType.JSON)
                .body("errorMessages[0]", equalTo("Could not find any instances with projects/40000"));
    }

    @Test
    @DisplayName("POST /projects/:id UNDEFINED INPUT")
    void testPostProjectErr() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/projects/hello")
                .then()
                .log().all()
                .statusCode(404)
                .contentType(ContentType.JSON)
                .body("errorMessages[0]", containsString("No such project entity instance with GUID or ID hello found"));
    }
}
