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
public class TestCategoryRelationshipsXMLDoc {
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
                .accept(ContentType.XML)
                .when()
                .get("/todos");

        String id = todoResponse.xmlPath().getString("todos.todo[-1].id");
        if (id == null) {
            Response createResponse = given()
                    .contentType(ContentType.XML)
                    .accept(ContentType.XML)
                    .body("<todo><title>Test Todo</title></todo>")
                    .post("/todos")
                    .then()
                    .statusCode(201)
                    .extract()
                    .response();
            id = createResponse.xmlPath().getString("todo.id");
        }
        return id;
    }

    private String getOrCreateProject() {
        Response projectResponse = given()
                .accept(ContentType.XML)
                .when()
                .get("/projects");

        String id = projectResponse.xmlPath().getString("projects.project[-1].id");
        if (id == null) {
            Response createResponse = given()
                    .contentType(ContentType.XML)
                    .accept(ContentType.XML)
                    .body("<project><title>Test Project</title></project>")
                    .post("/projects")
                    .then()
                    .statusCode(201)
                    .extract()
                    .response();
            id = createResponse.xmlPath().getString("project.id");
        }
        return id;
    }

    private String getOrCreateCategory() {
        Response categoryResponse = given()
                .accept(ContentType.XML)
                .when()
                .get("/categories");

        String id = categoryResponse.xmlPath().getString("categories.category[-1].id");
        if (id == null) {
            Response createResponse = given()
                    .contentType(ContentType.XML)
                    .accept(ContentType.XML)
                    .body("<category><title>Test Category</title></category>")
                    .post("/categories")
                    .then()
                    .statusCode(201)
                    .extract()
                    .response();
            id = createResponse.xmlPath().getString("category.id");
        }
        return id;
    }

    private void createTodoCategoryRelationship(String todoId, String categoryId) {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.XML)
                .body("{\"id\":\"" + categoryId + "\"}")
                .when()
                .post("/todos/" + todoId + "/categories")
                .then()
                .statusCode(201);
    }

    private void createProjectCategoryRelationship(String projectId, String categoryId) {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.XML)
                .body("{\"id\":\"" + categoryId + "\"}")
                .when()
                .post("/projects/" + projectId + "/categories")
                .then()
                .statusCode(201);
    }

    private void createProjectTodoRelationship(String projectId, String todoId) {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.XML)
                .body("{\"id\":\"" + todoId + "\"}")
                .when()
                .post("/projects/" + projectId + "/tasks")
                .then()
                .statusCode(201);
    }

    /* TODO-CATEGORY RELATIONSHIP TESTS */

    /* /todos/:id/categories */
    @Test
    @DisplayName("POST /todos/:id/categories XML")
    void testPostTodoCategoryRelationship() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.XML)
                .body("{\"id\":\"" + testCategoryId + "\"}")
                .when()
                .post("/todos/" + testTodoId + "/categories")
                .then()
                .statusCode(201)
                .contentType(ContentType.XML);
    }

    @Test
    @DisplayName("GET /todos/:id/categories XML")
    void testGetTodoCategoryRelationship() {
        createTodoCategoryRelationship(testTodoId, testCategoryId);
        Response response = given()
                .accept(ContentType.XML)
                .when()
                .get("/todos/" + testTodoId + "/categories")
                .then()
                .statusCode(200)
                .contentType(ContentType.XML)
                .body("categories.category", notNullValue())
                .extract()
                .response();

        assert response.xmlPath().getList("categories.category") != null;
    }

    /* /categories/:id/todos */
    @Test
    @DisplayName("GET /categories/:id/todos XML")
    void testGetTodosByCategoryRelationship() {
        createTodoCategoryRelationship(testTodoId, testCategoryId);

        Response response = given()
                .accept(ContentType.XML)
                .when()
                .get("/categories/" + testCategoryId + "/todos")
                .then()
                .statusCode(200)
                .contentType(ContentType.XML)
                .body("todos.todo", notNullValue())
                .extract()
                .response();

        assert response.xmlPath().getList("todos.todo") != null;
    }

    /* PROJECT-CATEGORY RELATIONSHIP TESTS */

    /* /projects/:id/categories */
    @Test
    @DisplayName("POST /projects/:id/categories XML")
    void testPostProjectCategoryRelationship() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.XML)
                .body("{\"id\":\"" + testCategoryId + "\"}")
                .when()
                .post("/projects/" + testProjectId + "/categories")
                .then()
                .statusCode(201)
                .contentType(ContentType.XML);
    }

    @Test
    @DisplayName("GET /projects/:id/categories XML")
    void testGetProjectCategoryRelationship() {
        createProjectCategoryRelationship(testProjectId, testCategoryId);
        Response response = given()
                .accept(ContentType.XML)
                .when()
                .get("/projects/" + testProjectId + "/categories")
                .then()
                .statusCode(200)
                .contentType(ContentType.XML)
                .body("categories.category", notNullValue())
                .extract()
                .response();

        assert response.xmlPath().getList("categories.category") != null;
    }

    /* /categories/:id/projects */
    @Test
    @DisplayName("GET /categories/:id/projects XML")
    void testGetProjectsByCategoryRelationship() {
        createProjectCategoryRelationship(testProjectId, testCategoryId);

        Response response = given()
                .accept(ContentType.XML)
                .when()
                .get("/categories/" + testCategoryId + "/projects")
                .then()
                .statusCode(200)
                .contentType(ContentType.XML)
                .body("projects.project", notNullValue())
                .extract()
                .response();

        assert response.xmlPath().getList("projects.project") != null;
    }

    /* TODO-PROJECT RELATIONSHIP TESTS */

    /* /todos/:id/tasksof */
    @Test
    @DisplayName("GET /todos/:id/tasksof XML")
    void testGetProjectsByTodoRelationship() {
        createProjectTodoRelationship(testProjectId, testTodoId);

        Response response = given()
                .accept(ContentType.XML)
                .when()
                .get("/todos/" + testTodoId + "/tasksof")
                .then()
                .statusCode(200)
                .contentType(ContentType.XML)
                .body("projects.project", notNullValue())
                .extract()
                .response();

        assert response.xmlPath().getList("projects.project") != null;
    }

    /* DELETION TESTS */

    /* /projects/:projectId/categories/:categoryId */
    @Test
    @DisplayName("DELETE /projects/:projectId/categories/:categoryId XML")
    void testDeleteProjectCategoryRelationship() {
        createProjectCategoryRelationship(testProjectId, testCategoryId);

        Response beforeDelete = given()
                .accept(ContentType.XML)
                .when()
                .get("/projects/" + testProjectId + "/categories")
                .then()
                .statusCode(200)
                .extract()
                .response();

        assert beforeDelete.xmlPath().getList("categories.category") != null;

        given()
                .accept(ContentType.XML)
                .when()
                .delete("/projects/" + testProjectId + "/categories/" + testCategoryId)
                .then()
                .statusCode(200);

        given()
                .accept(ContentType.XML)
                .when()
                .get("/projects/" + testProjectId + "/categories")
                .then()
                .statusCode(200)
                .contentType(ContentType.XML);
    }

    /* /projects/:projectId/tasks/:todoId */
    @Test
    @DisplayName("DELETE /projects/:projectId/tasks/:todoId XML")
    void testDeleteProjectTodoRelationship() {
        createProjectTodoRelationship(testProjectId, testTodoId);

        Response beforeDelete = given()
                .accept(ContentType.XML)
                .when()
                .get("/projects/" + testProjectId + "/tasks")
                .then()
                .statusCode(200)
                .extract()
                .response();

        assert beforeDelete.xmlPath().getList("todos.todo") != null;

        given()
                .accept(ContentType.XML)
                .when()
                .delete("/projects/" + testProjectId + "/tasks/" + testTodoId)
                .then()
                .statusCode(200);

        given()
                .accept(ContentType.XML)
                .when()
                .get("/projects/" + testProjectId + "/tasks")
                .then()
                .statusCode(200)
                .contentType(ContentType.XML);
    }

    /* /projects/:projectId/tasks/:todoId (second delete) */
    @Test
    @DisplayName("DELETE /projects/:projectId/tasks/:todoId (second delete) XML")
    void testDeleteProjectTodoRelationshipIdempotent() {
        createProjectTodoRelationship(testProjectId, testTodoId);

        given()
                .accept(ContentType.XML)
                .when()
                .delete("/projects/" + testProjectId + "/tasks/" + testTodoId)
                .then()
                .statusCode(200);

        given()
                .accept(ContentType.XML)
                .when()
                .delete("/projects/" + testProjectId + "/tasks/" + testTodoId)
                .then()
                .statusCode(404)
                .contentType(ContentType.XML);
    }
}
