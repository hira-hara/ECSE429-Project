import org.junit.jupiter.api.*;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import java.io.IOException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestTodoJSONDoc {

    private static final String BASE_URL = "http://localhost:4567";
    private Process serverProcess;

    @BeforeAll
    void startServer() {
        try {
            ProcessBuilder proc = new ProcessBuilder(
                    "java", "-jar", "runTodoManagerRestAPI-1.5.5.jar");
            serverProcess = proc.start();
        } catch (IOException e) {
            System.err.println("Could not start server: " + e.getMessage());
        }

        RestAssured.baseURI = BASE_URL;

        try {
            given().get("/projects").then().statusCode(200);
        } catch (Exception e) {
            Assumptions.abort("Service not running at " + BASE_URL);
        }
    }

    @AfterAll
    void stopServer() {
        try {
            given().get("/shutdown");
            if (serverProcess != null) {
                serverProcess.destroy();
            }
        } catch (Exception ignored) {
        }
 try {
            ProcessBuilder proc = new ProcessBuilder(
                    "java", "-jar", "runTodoManagerRestAPI-1.5.5.jar");
            serverProcess = proc.start();
        } catch (IOException e) {
            System.err.println("Could not start server: " + e.getMessage());
        }

    }

    @BeforeEach
    void resetState() {
        for (int i = 1; i <= 5; i++) {
            given()
                    .accept(ContentType.JSON)
                    .when()
                    .delete("/todos/" + i);
        }
    }

    @Test
    @DisplayName("GET /todos JSON")
    void testGetTodos() {

        given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"NewTodo\",\"description\":\"newDescription\"}")
                .when()
                .post("/todos")
                .then()
                .statusCode(201);

        given()
                .accept(ContentType.JSON)
                .when()
                .get("/todos")
                .then()
                .statusCode(200)
                .body("todos.size()", equalTo(1))
                .body("todos[0].title", equalTo("NewTodo"))
                .body("todos[0].description", equalTo("newDescription"));
    }

    @Test
    @DisplayName("POST /todos JSON")
    void testCreateTodo() {

        given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"NewTodo\",\"description\":\"newDescription\"}")
                .when()
                .post("/todos")
                .then()
                .statusCode(201)
                .body("title", equalTo("NewTodo"))
                .body("description", equalTo("newDescription"));
    }

    @Test
    @DisplayName("PUT /todos (method not allowed)")
    void testPutCollectionNotAllowed() {

        given()
                .accept(ContentType.JSON)
                .when()
                .put("/todos")
                .then()
                .statusCode(405);
    }

    @Test
    @DisplayName("DELETE /todos (method not allowed)")
    void testDeleteCollectionNotAllowed() {

        given()
                .accept(ContentType.JSON)
                .when()
                .delete("/todos")
                .then()
                .statusCode(405);
    }

    @Test
    @DisplayName("OPTIONS /todos")
    void testOptionsTodos() {

        given()
                .accept(ContentType.JSON)
                .when()
                .options("/todos")
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("HEAD /todos")
    void testHeadTodos() {

        given()
                .accept(ContentType.JSON)
                .when()
                .head("/todos")
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("PATCH /todos (method not allowed)")
    void testPatchTodos() {

        given()
                .accept(ContentType.JSON)
                .when()
                .patch("/todos")
                .then()
                .statusCode(405);
    }

    @Test
    @DisplayName("GET /todos/{id}")
    void testGetTodoById() {

        String id = given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"NewTodo\",\"description\":\"newDescription\"}")
                .when()
                .post("/todos")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        given()
                .accept(ContentType.JSON)
                .when()
                .get("/todos/" + id)
                .then()
                .statusCode(200)
                .body("todos[0].title", equalTo("NewTodo"))
                .body("todos[0].description", equalTo("newDescription"));
    }

    @Test
    @DisplayName("PUT /todos/{id}")
    void testEditTodoPut() {
        String id = given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"NewTodo\",\"description\":\"NewDescription\"}")
                .when()
                .post("/todos")
                .then()
                .extract()
                .path("id");
        
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/todos")
                .then()
                .statusCode(200)
                .body("todos.size()", equalTo(1))
                .body("todos[0].title", equalTo("NewTodo"))
                .body("todos[0].description", equalTo("NewDescription"));
            
        given()
            .contentType(ContentType.JSON)
            .body("{\"title\":\"UpNewTodo\",\"description\":\"UpNewDescription\"}")
            .when()
            .put("/todos/" + id)
            .then()
            .statusCode(200);
        
        given()
            .accept(ContentType.JSON)
            .when()
            .get("/todos")
            .then()
            .statusCode(200)
            .body("todos.size()", equalTo(1))
            .body("todos[0].title", equalTo("UpNewTodo"))
            .body("todos[0].description", equalTo("UpNewDescription"));
    }

    @Test
    @DisplayName("PUT /todos/{id}")
    void testEditTodoPost() {
         String id = given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"NewTodo\",\"description\":\"NewDescription\"}")
                .when()
                .post("/todos")
                .then()
                .extract()
                .path("id");
        
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/todos")
                .then()
                .statusCode(200)
                .body("todos.size()", equalTo(1))
                .body("todos[0].title", equalTo("NewTodo"))
                .body("todos[0].description", equalTo("NewDescription"));
            
        given()
            .contentType(ContentType.JSON)
            .body("{\"title\":\"UpNewTodo\",\"description\":\"UpNewDescription\"}")
            .when()
            .post("/todos/" + id)
            .then()
            .statusCode(200);
        
        given()
            .accept(ContentType.JSON)
            .when()
            .get("/todos")
            .then()
            .statusCode(200)
            .body("todos.size()", equalTo(1))
            .body("todos[0].title", equalTo("UpNewTodo"))
            .body("todos[0].description", equalTo("UpNewDescription"));
    }

    @Test
    @DisplayName("DELETE todo by id")
    void testDeleteTodo() {
         String id = given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"NewTodo\",\"description\":\"NewDescription\"}")
                .when()
                .post("/todos")
                .then()
                .extract()
                .path("id");

            given()
                .accept(ContentType.JSON)
                .when()
                .get("/todos/" + id)
                .then()
                .statusCode(200);
            
            given()
                .accept(ContentType.JSON)
                .when()
                .delete("/todos/" + id)
                .then()
                .statusCode(200);


            given()
                .accept(ContentType.JSON)
                .when()
                .get("/todos/" + id)
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("OPTIONS by todo id")
    void testTodoByIdOptions() {
        String id = given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"NewTodo\",\"description\":\"NewDescription\"}")
                .when()
                .post("/todos")
                .then()
                .extract()
                .path("id");
 
            given()
                .accept(ContentType.JSON)
                .when()
                .options("/todos/" + id)
                .then()
                .statusCode(200);

    }


    @Test
    @DisplayName("HEAD by todo id")
    void testTodoByIdHead() {
        String id = given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"NewTodo\",\"description\":\"NewDescription\"}")
                .when()
                .post("/todos")
                .then()
                .extract()
                .path("id");
 
            given()
                .accept(ContentType.JSON)
                .when()
                .head("/todos/" + id)
                .then()
                .statusCode(200);

    }

    @Test
    @DisplayName("Patch by todo id")
    void testTodoByIdPatch() {
        String id = given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"NewTodo\",\"description\":\"NewDescription\"}")
                .when()
                .post("/todos")
                .then()
                .extract()
                .path("id");
 
            given()
                .accept(ContentType.JSON)
                .when()
                .patch("/todos/" + id)
                .then()
                .statusCode(405);

    }
}
