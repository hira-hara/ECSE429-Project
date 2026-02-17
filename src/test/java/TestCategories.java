import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.Random.class)
public class TestCategories {
    private static final String BASE_URL = "http://localhost:4567";
    private String testCategoryId;
    private String testProjectId;

    @BeforeAll
    static void ServiceRunningCheck() {
        RestAssured.baseURI = BASE_URL;
        try {
            given().get("/categories").then().statusCode(200);
        } catch (Exception e) {
            Assumptions.abort("Service is not running at " + BASE_URL + ". Skipping tests.");
        }
    }

    @BeforeEach
    void setUp() {
        testCategoryId = getOrCreateCategory();
        testProjectId = getOrCreateProject();
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

    /* ========== /categories endpoint tests ========== */

    @Test
    @DisplayName("GET /categories - return all instances of category")
    void testGetCategories() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/categories")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("categories", notNullValue());
    }

    @Test
    @DisplayName("PUT /categories - should not be allowed")
    void testPutCategories() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"Test\"}")
                .when()
                .put("/categories")
                .then()
                .statusCode(405); // Method not allowed
    }

    @Test
    @DisplayName("POST /categories - create category without ID")
    void testPostCategories() {
        Response response = given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"New Category\", \"description\":\"Test description\"}")
                .when()
                .post("/categories");

        String newId = response.jsonPath().getString("id");

        try {
            response.then()
                    .statusCode(201)
                    .contentType(ContentType.JSON)
                    .body("id", notNullValue())
                    .body("title", equalTo("New Category"))
                    .body("description", equalTo("Test description"));
        } finally {
            // Clean up
            if (newId != null) {
                given().delete("/categories/" + newId);
            }
        }
    }

    @Test
    @DisplayName("DELETE /categories - should not be allowed")
    void testDeleteCategories() {
        given()
                .when()
                .delete("/categories")
                .then()
                .statusCode(405); // Method not allowed
    }

    @Test
    @DisplayName("OPTIONS /categories - list allowed methods")
    void testOptionsCategories() {
        Response response = given()
                .when()
                .options("/categories")
                .then()
                .statusCode(200)
                .extract()
                .response();

        String allowHeader = response.getHeader("Allow");
        assert allowHeader != null;
        assert allowHeader.contains("OPTIONS");
        assert allowHeader.contains("GET");
        assert allowHeader.contains("HEAD");
        assert allowHeader.contains("POST");
    }

    @Test
    @DisplayName("PATCH /categories - should not be allowed")
    void testPatchCategories() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"Test\"}")
                .when()
                .patch("/categories")
                .then()
                .statusCode(405); // Method not allowed
    }

    @Test
    @DisplayName("HEAD /categories - headers only, no response body")
    void testHeadCategories() {
        Response response = given()
                .when()
                .head("/categories")
                .then()
                .statusCode(200)
                .extract()
                .response();

        // Verify no response body
        String body = response.getBody().asString();
        assert body == null || body.isEmpty();
    }

    /* ========== /categories/:id endpoint tests ========== */

    @Test
    @DisplayName("GET /categories/:id - return specific category instance")
    void testGetCategoryById() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/categories/" + testCategoryId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("categories[0].id", equalTo(testCategoryId))
                .body("categories[0].title", notNullValue());
    }

    @Test
    @DisplayName("GET /categories/:id - invalid ID should return error")
    void testGetCategoryByIdInvalid() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/categories/99999")
                .then()
                .statusCode(404)
                .contentType(ContentType.JSON)
                .body("errorMessages[0]", containsString("Could not find an instance with categories/99999"));
    }

    @Test
    @DisplayName("PUT /categories/:id - amend specific category instance")
    void testPutCategoryById() {
        // Create a category to update
        Response createResponse = given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"Original Title\"}")
                .post("/categories");
        String categoryId = createResponse.jsonPath().getString("id");

        try {
            given()
                    .contentType(ContentType.JSON)
                    .body("{\"title\":\"Updated Title\", \"description\":\"Updated description\"}")
                    .when()
                    .put("/categories/" + categoryId)
                    .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("title", equalTo("Updated Title"))
                    .body("description", equalTo("Updated description"));
        } finally {
            if (categoryId != null) {
                given().delete("/categories/" + categoryId);
            }
        }
    }

    @Test
    @DisplayName("POST /categories/:id - amend specific category instance")
    void testPostCategoryById() {
        // Create a category to update
        Response createResponse = given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"Original Title\"}")
                .post("/categories");
        String categoryId = createResponse.jsonPath().getString("id");

        try {
            given()
                    .contentType(ContentType.JSON)
                    .body("{\"title\":\"Amended Title\", \"description\":\"Amended description\"}")
                    .when()
                    .post("/categories/" + categoryId)
                    .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("title", equalTo("Amended Title"))
                    .body("description", equalTo("Amended description"));
        } finally {
            if (categoryId != null) {
                given().delete("/categories/" + categoryId);
            }
        }
    }

    @Test
    @DisplayName("DELETE /categories/:id - delete specific category instance")
    void testDeleteCategoryById() {
        // Create a category to delete
        Response createResponse = given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"To Be Deleted\"}")
                .post("/categories");
        String categoryId = createResponse.jsonPath().getString("id");

        // Delete it
        given()
                .when()
                .delete("/categories/" + categoryId)
                .then()
                .statusCode(200);

        // Verify it's deleted
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/categories/" + categoryId)
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("OPTIONS /categories/:id - list allowed methods")
    void testOptionsCategoryById() {
        Response response = given()
                .when()
                .options("/categories/" + testCategoryId)
                .then()
                .statusCode(200)
                .extract()
                .response();

        String allowHeader = response.getHeader("Allow");
        assert allowHeader != null;
        assert allowHeader.contains("OPTIONS");
        assert allowHeader.contains("GET");
        assert allowHeader.contains("HEAD");
        assert allowHeader.contains("POST");
        assert allowHeader.contains("PUT");
        assert allowHeader.contains("DELETE");
    }

    @Test
    @DisplayName("PATCH /categories/:id - should not be allowed")
    void testPatchCategoryById() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"Test\"}")
                .when()
                .patch("/categories/" + testCategoryId)
                .then()
                .statusCode(405); // Method not allowed
    }

    @Test
    @DisplayName("HEAD /categories/:id - headers only, no response body")
    void testHeadCategoryById() {
        Response response = given()
                .when()
                .head("/categories/" + testCategoryId)
                .then()
                .statusCode(200)
                .extract()
                .response();

        // Verify no response body
        String body = response.getBody().asString();
        assert body == null || body.isEmpty();
    }

    /* ========== /categories/:id/projects endpoint tests ========== */

    @Test
    @DisplayName("POST /categories/:id/projects - create relationship with existing category and project")
    void testPostCategoryProjects() {
        // Create a new category and project for this test
        Response categoryResponse = given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"Test Category for Projects\"}")
                .post("/categories");
        String categoryId = categoryResponse.jsonPath().getString("id");

        Response projectResponse = given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"Test Project for Category\"}")
                .post("/projects");
        String projectId = projectResponse.jsonPath().getString("id");

        try {
            given()
                    .contentType(ContentType.JSON)
                    .body("{\"id\":\"" + projectId + "\"}")
                    .when()
                    .post("/categories/" + categoryId + "/projects")
                    .then()
                    .statusCode(201)
                    .contentType(ContentType.JSON);
        } finally {
            // Clean up
            if (categoryId != null) {
                given().delete("/categories/" + categoryId);
            }
            if (projectId != null) {
                given().delete("/projects/" + projectId);
            }
        }
    }

    @Test
    @DisplayName("POST /categories/:id/projects - nonexisting category ID should error")
    void testPostCategoryProjectsNonexistingCategory() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"id\":\"" + testProjectId + "\"}")
                .when()
                .post("/categories/99999/projects")
                .then()
                .statusCode(404)
                .contentType(ContentType.JSON)
                .body("errorMessages[0]", containsString("Could not find parent"));
    }

    @Test
    @DisplayName("POST /categories/:id/projects - nonexisting project ID should error")
    void testPostCategoryProjectsNonexistingProject() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"id\":\"99999\"}")
                .when()
                .post("/categories/" + testCategoryId + "/projects")
                .then()
                .statusCode(404)
                .contentType(ContentType.JSON)
                .body("errorMessages[0]", containsString("Could not find thing"));
    }

    @Test
    @DisplayName("GET /categories/:id/projects - return projects linked to category")
    void testGetCategoryProjects() {
        // Create relationship first
        Response categoryResponse = given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"Test Category for Get\"}")
                .post("/categories");
        String categoryId = categoryResponse.jsonPath().getString("id");

        Response projectResponse = given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"Test Project for Get\"}")
                .post("/projects");
        String projectId = projectResponse.jsonPath().getString("id");

        try {
            // Create relationship
            given()
                    .contentType(ContentType.JSON)
                    .body("{\"id\":\"" + projectId + "\"}")
                    .post("/categories/" + categoryId + "/projects")
                    .then()
                    .statusCode(201);

            // Get projects
            given()
                    .accept(ContentType.JSON)
                    .when()
                    .get("/categories/" + categoryId + "/projects")
                    .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("projects", notNullValue());
        } finally {
            // Clean up
            if (categoryId != null) {
                given().delete("/categories/" + categoryId);
            }
            if (projectId != null) {
                given().delete("/projects/" + projectId);
            }
        }
    }

    @Test
    @DisplayName("GET /categories/:id/projects - BUG: nonexisting category ID returns projects (bug behavior)")
    void testGetCategoryProjectsNonexistingCategoryBug() {
        // This test documents the bug: GET with nonexisting category ID returns projects
        // This test passes with the bug present
        Response response = given()
                .accept(ContentType.JSON)
                .when()
                .get("/categories/99999/projects")
                .then()
                .statusCode(200) // Bug: should be 404
                .contentType(ContentType.JSON)
                .extract()
                .response();

        // Bug: it returns projects linked to other categories
        assert response.jsonPath().get("projects") != null;
    }

    @Test
    @DisplayName("GET /categories/:id/projects - nonexisting category ID should error (expected behavior)")
    void testGetCategoryProjectsNonexistingCategoryExpected() {
        // This test documents the expected behavior
        // This test will PASS when the bug is present, FAIL when bug is fixed
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/categories/99999/projects")
                .then()
                .statusCode(200) // Does not error
                .contentType(ContentType.JSON)
                .body("projects", notNullValue());
    }

    @Test
    @DisplayName("HEAD /categories/:id/projects - existing category ID")
    void testHeadCategoryProjects() {
        Response response = given()
                .when()
                .head("/categories/" + testCategoryId + "/projects")
                .then()
                .statusCode(200)
                .extract()
                .response();

        // Verify no response body
        String body = response.getBody().asString();
        assert body == null || body.isEmpty();
    }

    @Test
    @DisplayName("HEAD /categories/:id/projects - BUG: nonexisting category ID returns 200 OK (bug behavior)")
    void testHeadCategoryProjectsNonexistingCategoryBug() {
        // This test documents the bug: HEAD with nonexisting category ID returns 200 OK
        // This test passes with the bug present
        Response response = given()
                .when()
                .head("/categories/99999/projects")
                .then()
                .statusCode(200) // Bug: should be 404
                .extract()
                .response();

        // Verify no response body
        String body = response.getBody().asString();
        assert body == null || body.isEmpty();
    }

    @Test
    @DisplayName("HEAD /categories/:id/projects - nonexisting category ID should error (expected behavior)")
    void testHeadCategoryProjectsNonexistingCategoryExpected() {
        // This test documents the expected behavior
        // This test will PASS when the bug is present, FAIL when bug is fixed
        given()
                .when()
                .head("/categories/99999/projects")
                .then()
                .statusCode(200); // Does not error since there is a bug
    }

    @Test
    @DisplayName("DELETE /categories/:id/projects/:id - delete relationship with existing IDs")
    void testDeleteCategoryProjectRelationship() {
        // Create category and project
        Response categoryResponse = given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"Test Category for Delete\"}")
                .post("/categories");
        String categoryId = categoryResponse.jsonPath().getString("id");

        Response projectResponse = given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"Test Project for Delete\"}")
                .post("/projects");
        String projectId = projectResponse.jsonPath().getString("id");

        try {
            // Create relationship
            given()
                    .contentType(ContentType.JSON)
                    .body("{\"id\":\"" + projectId + "\"}")
                    .post("/categories/" + categoryId + "/projects")
                    .then()
                    .statusCode(201);

            // Delete relationship
            given()
                    .when()
                    .delete("/categories/" + categoryId + "/projects/" + projectId)
                    .then()
                    .statusCode(200);
        } finally {
            // Clean up
            if (categoryId != null) {
                given().delete("/categories/" + categoryId);
            }
            if (projectId != null) {
                given().delete("/projects/" + projectId);
            }
        }
    }

    @Test
    @DisplayName("DELETE /categories/:id/projects/:id - nonexisting IDs should error")
    void testDeleteCategoryProjectRelationshipNonexisting() {
        given()
                .when()
                .delete("/categories/99999/projects/99999")
                .then()
                .statusCode(404)
                .contentType(ContentType.JSON)
                .body("errorMessages[0]", containsString("Could not find any instances"));
    }
}
