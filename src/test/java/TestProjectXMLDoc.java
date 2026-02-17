import static io.restassured.RestAssured.*;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import static org.hamcrest.Matchers.*;
import org.junit.jupiter.api.*;
import io.restassured.response.Response;

@TestMethodOrder(MethodOrderer.Random.class)
public class TestProjectXMLDoc {
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
    void setUp() {
        Response response = given()
                .accept(ContentType.XML)
                .when()
                .get("/projects");
        
        testProjectId = response.xmlPath().getString("projects.project[-1].id");

        if (testProjectId == null) {
            Response createResponse = given()
                    .contentType(ContentType.XML)
                    .accept(ContentType.XML)
                    .body("<project><title>Initial Project</title></project>")
                    .post("/projects");
            testProjectId = createResponse.xmlPath().getString("id");
        }
    }

    /* XML TESTS */

    @Test
    @DisplayName("GET /projects XML")
    void testGetProject() {
        given()
                .accept(ContentType.XML)
                .when()
                .get("/projects")
                .then()
                .statusCode(200)
                .contentType(ContentType.XML)
                .body("projects.project.size()", greaterThan(0));
    }

    @Test
    @DisplayName("POST /projects XML")
    void testCreateProject() {
        String xmlBody = "<project>" +
                        "<active>true</active>" +
                        "<description>new project to do</description>" +
                         "<completed>false</completed>" +
                         "<title>New Project</title>" +
                         "</project>";

        Response response = given()
                .contentType(ContentType.XML)
                .accept(ContentType.XML)
                .body(xmlBody)
                .when()
                .post("/projects");

        String newId = response.xmlPath().getString("project.id");

        try {
            response.then()
                    .statusCode(201)
                    .body("project.title", equalTo("New Project"));
        } finally {
            if (newId != null) {
                given().delete("/projects/" + newId);
            }
        }
    }

    @Test
    @DisplayName("GET /projects/:id XML")
    void testGetProjectID() {
        given()
                .accept(ContentType.XML)
                .when()
                .get("/projects/" + testProjectId)
                .then()
                .statusCode(200)
                .contentType(ContentType.XML)
                .body("projects.project[0].id", equalTo(testProjectId));
    }

    @Test
    @DisplayName("POST /projects/:id XML")
    void testCreateUpdateDeleteFlow() {
        // Create
        Response createResponse = given()
                .contentType(ContentType.XML)
                .accept(ContentType.XML)
                .body("<project><title>Old Project</title><completed>false</completed></project>")
                .when()
                .post("/projects");

        createResponse.then().statusCode(201);
        String localId = createResponse.xmlPath().getString("project.id");

        try { 
            // Update
            given()
                    .contentType(ContentType.XML)
                    .accept(ContentType.XML)
                    .body("<project><completed>true</completed></project>")
                    .when()
                    .post("/projects/" + localId)
                    .then()
                    .statusCode(200)
                    .body("project.title", equalTo("Old Project"))
                    .body("project.completed", equalTo("true"));

        } finally {
            if (localId != null) {
                given().delete("/projects/" + localId).then().statusCode(200);
            }
        }
    }

    @Test
    @DisplayName("GET /projects/:id/tasks XML")
    void testGetProjectIDTasks() {
        given()
                .accept(ContentType.XML)
                .when()
                .get("/projects/" + testProjectId + "/tasks")
                .then()
                .statusCode(200)
                .contentType(ContentType.XML)
                .body("todos.todo.size()", greaterThan(0));
    }


    @Test
    @DisplayName("HEAD /projects/:id/tasks XML")
    void testHeadProjectIDTasks() {
        given()
                .accept(ContentType.XML)
                .when()
                .head("/projects/" + testProjectId + "/tasks")
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("DELETE /projects/:id NON EXISTENT ID XML")
    void testDeleteProjectIDErr() {
        given()
                .accept(ContentType.XML)
                .when()
                .delete("/projects/40000")
                .then()
                .statusCode(404)
                .body("errorMessages.errorMessage[0]", equalTo("Could not find any instances with projects/40000"));
    }

    @Test
    @DisplayName("POST /projects/:id UNDEFINED INPUT XML")
    void testPostProjectErr() {
        given()
                .contentType(ContentType.XML)
                .accept(ContentType.XML)
                .when()
                .post("/projects/hello")
                .then()
                .statusCode(404)
                .body("errorMessages.errorMessage[0]", containsString("No such project entity instance with GUID or ID hello found"));
    }
}
