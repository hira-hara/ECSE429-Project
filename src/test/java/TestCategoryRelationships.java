import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@TestMethodOrder(MethodOrderer.Random.class)
public class TestCategoryRelationships {
    private static final String BASE_URL = "http://localhost:4567";
    private String testTodoId;
    private String testProjectId;
    private String testCategoryId;

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
        testTodoId = getOrCreateTodo();
        testProjectId = getOrCreateProject();
        testCategoryId = getOrCreateCategory();
    }

    private String getOrCreateTodo() {
        Response todoResponse = given()
                .contentType(ContentType.JSON)
                .when()
                .get("/todos");
        
        String id = todoResponse.jsonPath().getString("todos[-1].id");
        if (id == null) {
            Response createResponse = given()
                    .contentType(ContentType.JSON)
                    .body("{\"title\":\"Test Todo\"}")
                    .post("/todos")
                    .then()
                    .statusCode(201)
                    .extract()
                    .response();
            id = createResponse.jsonPath().getString("id");
        }
        return id;
    }

    private String getOrCreateProject() {
        Response projectResponse = given()
                .contentType(ContentType.JSON)
                .when()
                .get("/projects");
        
        String id = projectResponse.jsonPath().getString("projects[-1].id");
        if (id == null) {
            Response createResponse = given()
                    .contentType(ContentType.JSON)
                    .body("{\"title\":\"Test Project\"}")
                    .post("/projects")
                    .then()
                    .statusCode(201)
                    .extract()
                    .response();
            id = createResponse.jsonPath().getString("id");
        }
        return id;
    }

    private String getOrCreateCategory() {
        Response categoryResponse = given()
                .contentType(ContentType.JSON)
                .when()
                .get("/categories");
        
        String id = categoryResponse.jsonPath().getString("categories[-1].id");
        if (id == null) {
            Response createResponse = given()
                    .contentType(ContentType.JSON)
                    .body("{\"title\":\"Test Category\"}")
                    .post("/categories")
                    .then()
                    .statusCode(201)
                    .extract()
                    .response();
            id = createResponse.jsonPath().getString("id");
        }
        return id;
    }

    private void createTodoCategoryRelationship(String todoId, String categoryId) {
        given()
                .contentType(ContentType.JSON)
                .body("{\"id\":\"" + categoryId + "\"}")
                .when()
                .post("/todos/" + todoId + "/categories")
                .then()
                .statusCode(201);
    }

    private void createProjectCategoryRelationship(String projectId, String categoryId) {
        given()
                .contentType(ContentType.JSON)
                .body("{\"id\":\"" + categoryId + "\"}")
                .when()
                .post("/projects/" + projectId + "/categories")
                .then()
                .statusCode(201);
    }

    private void createProjectTodoRelationship(String projectId, String todoId) {
        given()
                .contentType(ContentType.JSON)
                .body("{\"id\":\"" + todoId + "\"}")
                .when()
                .post("/projects/" + projectId + "/tasks")
                .then()
                .statusCode(201);
    }

    /* TODO-CATEGORY RELATIONSHIP TESTS */

    /* For endpoint /todos/:id/categories */
    @Test
    @DisplayName("POST /todos/:id/categories - Create todo-category relationship")
    void testPostTodoCategoryRelationship() {
        // Link category to todo
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("{\"id\":\"" + testCategoryId + "\"}")
                .when()
                .post("/todos/" + testTodoId + "/categories")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON);
    }

    @Test
    @DisplayName("GET /todos/:id/categories - Return categories linked to todo")
    void testGetTodoCategoryRelationship() {
        // Ensure relationship exists
        createTodoCategoryRelationship(testTodoId, testCategoryId);
        // Fetch linked categories
        Response response = given()
                .accept(ContentType.JSON)
                .when()
                .get("/todos/" + testTodoId + "/categories")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("categories", notNullValue())
                .extract()
                .response();

        assert response.jsonPath().getList("categories") != null;
    }

    /* GET /categories/:id/todos */
    @Test
    @DisplayName("GET /categories/:id/todos - Return todos linked to category")
    void testGetTodosByCategoryRelationship() {
        // Ensure relationship exists
        createTodoCategoryRelationship(testTodoId, testCategoryId);

        // Fetch linked todos
        Response response = given()
                .accept(ContentType.JSON)
                .when()
                .get("/categories/" + testCategoryId + "/todos")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("todos", notNullValue())
                .extract()
                .response();

        assert response.jsonPath().getList("todos") != null;
    }

    /* PROJECT-CATEGORY RELATIONSHIP TESTS */

    /* /projects/:id/categories */
    @Test
    @DisplayName("POST /projects/:id/categories - Create project-category relationship")
    void testPostProjectCategoryRelationship() {
        // Link category to project
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("{\"id\":\"" + testCategoryId + "\"}")
                .when()
                .post("/projects/" + testProjectId + "/categories")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON);
    }

    @Test
    @DisplayName("GET /projects/:id/categories - Return categories linked to project")
    void testGetProjectCategoryRelationship() {
        // Ensure relationship exists
        createProjectCategoryRelationship(testProjectId, testCategoryId);
        // Fetch linked categories
        Response response = given()
                .accept(ContentType.JSON)
                .when()
                .get("/projects/" + testProjectId + "/categories")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("categories", notNullValue())
                .extract()
                .response();

        assert response.jsonPath().getList("categories") != null;
    }

    /* GET /categories/:id/projects */
    @Test
    @DisplayName("GET /categories/:id/projects - Return projects linked to category")
    void testGetProjectsByCategoryRelationship() {
        // Ensure relationship exists
        createProjectCategoryRelationship(testProjectId, testCategoryId);

        // Fetch linked projects
        Response response = given()
                .accept(ContentType.JSON)
                .when()
                .get("/categories/" + testCategoryId + "/projects")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("projects", notNullValue())
                .extract()
                .response();

        assert response.jsonPath().getList("projects") != null;
    }

    /* TODO-PROJECT RELATIONSHIP TESTS */

    /* GET /todos/:id/tasksof */
    @Test
    @DisplayName("GET /todos/:id/tasksof - Return projects linked to todo")
    void testGetProjectsByTodoRelationship() {
        // Ensure relationship exists
        createProjectTodoRelationship(testProjectId, testTodoId);

        // Fetch linked projects
        Response response = given()
                .accept(ContentType.JSON)
                .when()
                .get("/todos/" + testTodoId + "/tasksof")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("projects", notNullValue())
                .extract()
                .response();

        assert response.jsonPath().getList("projects") != null;
    }

    /* DELETION TESTS */

    /* DELETE /projects/:projectId/categories/:categoryId */
    @Test
    @DisplayName("DELETE /projects/:projectId/categories/:categoryId - Delete project-category relationship")
    void testDeleteProjectCategoryRelationship() {
        // Ensure relationship exists
        createProjectCategoryRelationship(testProjectId, testCategoryId);

        // Confirm relationship is visible before deletion
        Response beforeDelete = given()
                .accept(ContentType.JSON)
                .when()
                .get("/projects/" + testProjectId + "/categories")
                .then()
                .statusCode(200)
                .extract()
                .response();
        
        assert beforeDelete.jsonPath().getList("categories") != null;

        // Delete relationship
        given()
                .accept(ContentType.JSON)
                .when()
                .delete("/projects/" + testProjectId + "/categories/" + testCategoryId)
                .then()
                .statusCode(200);

        // Re-check linked categories
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/projects/" + testProjectId + "/categories")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    /* DELETE /projects/:projectId/tasks/:todoId */
    @Test
    @DisplayName("DELETE /projects/:projectId/tasks/:todoId - Delete project-todo relationship")
    void testDeleteProjectTodoRelationship() {
        // Ensure relationship exists
        createProjectTodoRelationship(testProjectId, testTodoId);

        // Confirm relationship is visible before deletion
        Response beforeDelete = given()
                .accept(ContentType.JSON)
                .when()
                .get("/projects/" + testProjectId + "/tasks")
                .then()
                .statusCode(200)
                .extract()
                .response();
        
        assert beforeDelete.jsonPath().getList("todos") != null;

        // Delete relationship
        given()
                .accept(ContentType.JSON)
                .when()
                .delete("/projects/" + testProjectId + "/tasks/" + testTodoId)
                .then()
                .statusCode(200);

        // Re-check linked todos
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/projects/" + testProjectId + "/tasks")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    /* DELETE /projects/:projectId/tasks/:todoId (second delete) */
    @Test
    @DisplayName("DELETE /projects/:projectId/tasks/:todoId (second delete) - Should be idempotent and return 404")
    void testDeleteProjectTodoRelationshipIdempotent() {
        // Ensure relationship exists
        createProjectTodoRelationship(testProjectId, testTodoId);

        // First delete succeeds
        given()
                .accept(ContentType.JSON)
                .when()
                .delete("/projects/" + testProjectId + "/tasks/" + testTodoId)
                .then()
                .statusCode(200);

        // Second delete confirms idempotency
        given()
                .accept(ContentType.JSON)
                .when()
                .delete("/projects/" + testProjectId + "/tasks/" + testTodoId)
                .then()
                .statusCode(404)
                .contentType(ContentType.JSON);
    }
}
