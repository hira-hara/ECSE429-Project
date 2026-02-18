import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import io.restassured.response.Response;

import static org.hamcrest.Matchers.*;
import static io.restassured.RestAssured.*;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

public class TestTodoJSONDoc {
    private static final String BASE_URL = "http://localhost:4567";

    @BeforeAll
    static void ServiceRunningCheck() {
        RestAssured.baseURI = BASE_URL;
        try {
            given().get("/categories").then().statusCode(200);
        } catch (Exception e) {
            Assumptions.abort("Service is not running at " + BASE_URL + ". Skipping tests.");
        }
    }

    @Test
    @DisplayName("GET /todo JSON")
    void testGetTodo() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/todos")
                .then()
                .statusCode(200)
                .body("todos.size()", greaterThan(1));
    }

    @Test
    @DisplayName("POST /todos JSON")
    void testPostTodo() {

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
                .contentType(ContentType.JSON)
                .when()
                .get("/todos/" + id)
                .then()
                .statusCode(200)
                .body("todos[0].title", equalTo("NewTodo"))
                .body("todos[0].description", equalTo("newDescription"));

        given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/todos/" + id)
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("Put /todos JSON")
    void testPutTodo() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"NewTodo\",\"description\":\"NewDescription\"}")
                .when()
                .put("/todos")
                .then()
                .statusCode(405);
    }

    @Test
    @DisplayName("Delete /todos JSON")
    void testDeleteTodo() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/todos")
                .then()
                .statusCode(405);
    }

    @Test
    @DisplayName("Options /todos JSON")
    void testOptionsTodo() {
        Response resp = given()
                .contentType(ContentType.JSON)
                .when()
                .options("/todos")
                .then()
                .statusCode(200)
                .extract()
                .response();

        String allowHeader = resp.getHeader("Allow");
        assert allowHeader != null;
        assert allowHeader.contains("OPTIONS");
        assert allowHeader.contains("GET");
        assert allowHeader.contains("HEAD");
        assert allowHeader.contains("POST");
    }

    @Test
    @DisplayName("Head /todos JSON")
    void testHeadTodo() {
        Response response = given()
                .when()
                .head("/todos")
                .then()
                .statusCode(200)
                .extract()
                .response();

        String body = response.getBody().asString();
        assert body == null || body.isEmpty();
    }

    @Test
    @DisplayName("Patch /todos JSON")
    void testPatchTodo() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .patch("/todos")
                .then()
                .statusCode(405);
    }

    @Test
    @DisplayName("Get /todos/:id JSON")
    void testGetTodoById() {
        String id = given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"Todo\",\"description\":\"Description\"}")
                .when()
                .post("/todos")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/todos/" + id)
                .then()
                .statusCode(200)
                .body("todos[0].title", equalTo("Todo"))
                .body("todos[0].description", equalTo("Description"));

        given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/todos/" + id)
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("Put /todos/:id JSON")
    void testPutTodoById() {
        String id = given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"Todo\",\"description\":\"Description\"}")
                .when()
                .post("/todos")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/todos/" + id)
                .then()
                .statusCode(200)
                .body("todos[0].title", equalTo("Todo"))
                .body("todos[0].description", equalTo("Description"));
        // edit
        given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"updatedTitle\",\"description\":\"updatedDesc\"}")
                .when()
                .put("/todos/" + id)
                .then()
                .statusCode(200);

        // check values updated
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/todos/" + id)
                .then()
                .statusCode(200)
                .body("todos[0].title", equalTo("updatedTitle"))
                .body("todos[0].description", equalTo("updatedDesc"));

        given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/todos/" + id)
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("Post /todos/:id JSON")
    void testPostTodoById() {
        String id = given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"Todo\",\"description\":\"Description\"}")
                .when()
                .post("/todos")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/todos/" + id)
                .then()
                .statusCode(200)
                .body("todos[0].title", equalTo("Todo"))
                .body("todos[0].description", equalTo("Description"));
        // edit
        given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"updatedTitle\",\"description\":\"updatedDesc\"}")
                .when()
                .post("/todos/" + id)
                .then()
                .statusCode(200);

        // check values updated
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/todos/" + id)
                .then()
                .statusCode(200)
                .body("todos[0].title", equalTo("updatedTitle"))
                .body("todos[0].description", equalTo("updatedDesc"));

        given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/todos/" + id)
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("Delete /todos/:id JSON")
    void testDeleteTodoById() {
        String id = given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"Todo\",\"description\":\"Description\"}")
                .when()
                .post("/todos")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/todos/" + id)
                .then()
                .statusCode(200)
                .body("todos[0].title", equalTo("Todo"))
                .body("todos[0].description", equalTo("Description"));

        given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/todos/" + id)
                .then()
                .statusCode(200);

        // check was deleted
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/todos/" + id)
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Options /todos/:id JSON")
    void testOptionsByTodo() {
        String id = given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"Todo\",\"description\":\"Description\"}")
                .when()
                .post("/todos")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        Response resp = given()
                .contentType(ContentType.JSON)
                .when()
                .options("/todos/" + id)
                .then()
                .statusCode(200)
                .extract()
                .response();

        String allowHeader = resp.getHeader("Allow");
        assert allowHeader != null;
        assert allowHeader.contains("OPTIONS");
        assert allowHeader.contains("GET");
        assert allowHeader.contains("HEAD");
        assert allowHeader.contains("POST");
        assert allowHeader.contains("DELETE");
        assert allowHeader.contains("PUT");

        given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/todos/" + id)
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("Head /todos/:id JSON")
    void testHeadTodoById() {
        String id = given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"Todo\",\"description\":\"Description\"}")
                .when()
                .post("/todos")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        Response response = given()
                .when()
                .head("/todos/" + id)
                .then()
                .statusCode(200)
                .extract()
                .response();

        String body = response.getBody().asString();
        assert body == null || body.isEmpty();

        given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/todos/" + id)
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("Patch /todos/:id JSON")
    void testPatchTodoById() {
        String id = given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"Todo\",\"description\":\"Description\"}")
                .when()
                .post("/todos")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        given()
                .contentType(ContentType.JSON)
                .when()
                .patch("/todos/" + id)
                .then()
                .statusCode(405);

        given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/todos/" + id)
                .then()
                .statusCode(200);
    }

    // results with invalid input (getting 404s)

    @Test
    @DisplayName("Post no title /todos")
    void testIncompleteTitleTodo() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"description\":\"Description\"}")
                .when()
                .post("/todos")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Get invalid id /todos/:id")
    void testGetInvalidTodoById() {
        String id = given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"Todo\",\"description\":\"Description\"}")
                .when()
                .post("/todos")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

         given()
                .contentType(ContentType.JSON)
                .when()
                .get("/todos/" + id)
                .then()
                .statusCode(200)
                .body("todos[0].title", equalTo("Todo"))
                .body("todos[0].description", equalTo("Description"));

        given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/todos/" + id)
                .then()
                .statusCode(200);
        
           given()
                .contentType(ContentType.JSON)
                .when()
                .get("/todos/" + id)
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Put invalid id /todos/:id")
    void testPutInvalidTodoById() {
        String id = given() // create
                .contentType(ContentType.JSON)
                .body("{\"title\":\"Todo\",\"description\":\"Description\"}")
                .when()
                .post("/todos")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

         given() // test successfully created
                .contentType(ContentType.JSON)
                .when()
                .get("/todos/" + id)
                .then()
                .statusCode(200)
                .body("todos[0].title", equalTo("Todo"))
                .body("todos[0].description", equalTo("Description"));

        given() // delete it 
                .contentType(ContentType.JSON)
                .when()
                .delete("/todos/" + id)
                .then()
                .statusCode(200);
        
           given() // put to invalid id (just deleted)

                .contentType(ContentType.JSON)
                .body("{\"title\":\"updatedTitle\",\"description\":\"updatedDesc\"}")
                .when()
                .put("/todos/" + id)
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Post invalid id /todos/:id")
    void testPostInvalidTodoById() {
        String id = given() // create
                .contentType(ContentType.JSON)
                .body("{\"title\":\"Todo\",\"description\":\"Description\"}")
                .when()
                .post("/todos")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

         given() // test successfully created
                .contentType(ContentType.JSON)
                .when()
                .get("/todos/" + id)
                .then()
                .statusCode(200)
                .body("todos[0].title", equalTo("Todo"))
                .body("todos[0].description", equalTo("Description"));

        given() // delete it 
                .contentType(ContentType.JSON)
                .when()
                .delete("/todos/" + id)
                .then()
                .statusCode(200);
        
           given() // post to invalid id (just deleted)

                .contentType(ContentType.JSON)
                .body("{\"title\":\"updatedTitle\",\"description\":\"updatedDesc\"}")
                .when()
                .post("/todos/" + id)
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Delete invalid id /todos/:id")
    void testDeleteInvalidTodoById() {
        String id = given() // create
                .contentType(ContentType.JSON)
                .body("{\"title\":\"Todo\",\"description\":\"Description\"}")
                .when()
                .post("/todos")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

         given() // test successfully created
                .contentType(ContentType.JSON)
                .when()
                .get("/todos/" + id)
                .then()
                .statusCode(200)
                .body("todos[0].title", equalTo("Todo"))
                .body("todos[0].description", equalTo("Description"));

        given() // delete it 
                .contentType(ContentType.JSON)
                .when()
                .delete("/todos/" + id)
                .then()
                .statusCode(200);
        
           given() // delete to invalid id (just deleted)
                .contentType(ContentType.JSON)
                .body("{\"title\":\"updatedTitle\",\"description\":\"updatedDesc\"}")
                .when()
                .delete("/todos/" + id)
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Head invalid id /todos/:id")
    void testHeadInvalidTodoById() {
        String id = given() // create
                .contentType(ContentType.JSON)
                .body("{\"title\":\"Todo\",\"description\":\"Description\"}")
                .when()
                .post("/todos")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

         given() // test successfully created
                .contentType(ContentType.JSON)
                .when()
                .get("/todos/" + id)
                .then()
                .statusCode(200)
                .body("todos[0].title", equalTo("Todo"))
                .body("todos[0].description", equalTo("Description"));

        given() // delete it 
                .contentType(ContentType.JSON)
                .when()
                .delete("/todos/" + id)
                .then()
                .statusCode(200);
        
           given() // head to invalid id (just deleted)
                .contentType(ContentType.JSON)
                .body("{\"title\":\"updatedTitle\",\"description\":\"updatedDesc\"}")
                .when()
                .head("/todos/" + id)
                .then()
                .statusCode(404);
    }
}
