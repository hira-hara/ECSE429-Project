

import static io.restassured.RestAssured.*;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import static org.hamcrest.Matchers.*;
import org.junit.jupiter.api.*;

import io.restassured.response.Response;

@TestMethodOrder(MethodOrderer.Random.class) // In order to run in any order
public class TestProjectJSONUndoc {
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

    /* UNDOCUMENTED JSON TESTS */
    @Test
    @DisplayName("PUT /projects JSON")
    void testPutProject() {
        given()
                .accept(ContentType.JSON)
                .when()
                .put("/projects")
                .then()
                .statusCode(405);
    }


    @Test
    @DisplayName("DELETE /projects JSON")
    void testDeleteProject() {
        given()
                .accept(ContentType.JSON)
                .when()
                .delete("/projects")
                .then()
                .statusCode(405);
    }

    @Test
    @DisplayName("OPTIONS /projects JSON")
    void testOptionsProject() {
        given()
                .accept(ContentType.JSON)
                .when()
                .options("/projects")
                .then()
                .statusCode(200);
    }
}
