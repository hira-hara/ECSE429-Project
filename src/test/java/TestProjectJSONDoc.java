
import static io.restassured.RestAssured.*;
import io.restassured.http.ContentType;
import static org.hamcrest.Matchers.*;
import org.junit.jupiter.api.*;

import io.restassured.response.Response;

@TestMethodOrder(MethodOrderer.Random.class) // In order to run in any order
public class TestProjectJSONDoc {
    private static final String BASE_URL = "http://localhost:4567";
    private String testProjectId;

    @BeforeAll
    static void ServiceRunningCheck() {
        try {
            given()
                .baseUri(BASE_URL)
            .when()
                .get("/projects")
            .then()
                .statusCode(200); 
        } catch (Exception e) {
            Assumptions.abort("Service not running.");
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

    
}
