import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import io.restassured.response.Response;

import static org.hamcrest.Matchers.*;
import static io.restassured.RestAssured.*;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

public class TestTodoXMLDoc {
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
@DisplayName("GET /todo XML")
void testGetTodo() {
    given()
            .accept(ContentType.XML)
            .when()
            .get("/todos")
            .then()
            .statusCode(200)
            .contentType(ContentType.XML)
            .body("todos.todo.size()", greaterThan(1));
}

@Test
@DisplayName("POST /todos XML")
void testPostTodo() {

    String id = given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .body("<todo><title>NewTodo</title><description>newDescription</description></todo>")
            .when()
            .post("/todos")
            .then()
            .statusCode(201)
            .extract()
            .xmlPath()
            .getString("todo.id");

    given()
            .accept(ContentType.XML)
            .when()
            .get("/todos/" + id)
            .then()
            .statusCode(200)
            .body("todos.todo[0].title", equalTo("NewTodo"))
            .body("todos.todo[0].description", equalTo("newDescription"));

    given()
            .accept(ContentType.XML)
            .when()
            .delete("/todos/" + id)
            .then()
            .statusCode(200);
}

@Test
@DisplayName("Put /todos XML")
void testPutTodo() {
    given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .body("<todo><title>NewTodo</title><description>NewDescription</description></todo>")
            .when()
            .put("/todos")
            .then()
            .statusCode(405);
}

@Test
@DisplayName("Delete /todos XML")
void testDeleteTodo() {
    given()
            .accept(ContentType.XML)
            .when()
            .delete("/todos")
            .then()
            .statusCode(405);
}

@Test
@DisplayName("Options /todos XML")
void testOptionsTodo() {
    Response resp = given()
            .accept(ContentType.XML)
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
@DisplayName("Head /todos XML")
void testHeadTodo() {
    Response response = given()
            .accept(ContentType.XML)
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
@DisplayName("Patch /todos XML")
void testPatchTodo() {
    given()
            .accept(ContentType.XML)
            .when()
            .patch("/todos")
            .then()
            .statusCode(405);
}

@Test
@DisplayName("Get /todos/:id XML")
void testGetTodoById() {
    String id = given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .body("<todo><title>Todo</title><description>Description</description></todo>")
            .when()
            .post("/todos")
            .then()
            .statusCode(201)
            .extract()
            .xmlPath()
            .getString("todo.id");

    given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .when()
            .get("/todos/" + id)
            .then()
            .statusCode(200)
            .body("todos.todo[0].title", equalTo("Todo"))
            .body("todos.todo[0].description", equalTo("Description"));

    given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .when()
            .delete("/todos/" + id)
            .then()
            .statusCode(200);
}
@Test
@DisplayName("Put /todos/:id XML")
void testPutTodoById() {
    String id = given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .body("<todo><title>Todo</title><description>Description</description></todo>")
            .when()
            .post("/todos")
            .then()
            .statusCode(201)
            .extract()
            .xmlPath()
            .getString("todo.id");

    given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .when()
            .get("/todos/" + id)
            .then()
            .statusCode(200)
            .body("todos.todo[0].title", equalTo("Todo"))
            .body("todos.todo[0].description", equalTo("Description"));

    given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .body("<todo><title>updatedTitle</title><description>updatedDesc</description></todo>")
            .when()
            .put("/todos/" + id)
            .then()
            .statusCode(200);

    given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .when()
            .get("/todos/" + id)
            .then()
            .statusCode(200)
            .body("todos.todo[0].title", equalTo("updatedTitle"))
            .body("todos.todo[0].description", equalTo("updatedDesc"));

    given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .when()
            .delete("/todos/" + id)
            .then()
            .statusCode(200);
}
@Test
@DisplayName("Post /todos/:id XML")
void testPostTodoById() {
    String id = given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .body("<todo><title>Todo</title><description>Description</description></todo>")
            .when()
            .post("/todos")
            .then()
            .statusCode(201)
            .extract()
            .xmlPath()
            .getString("todo.id");

    given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .when()
            .get("/todos/" + id)
            .then()
            .statusCode(200)
            .body("todos.todo[0].title", equalTo("Todo"))
            .body("todos.todo[0].description", equalTo("Description"));

    given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .body("<todo><title>updatedTitle</title><description>updatedDesc</description></todo>")
            .when()
            .post("/todos/" + id)
            .then()
            .statusCode(200);

    given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .when()
            .get("/todos/" + id)
            .then()
            .statusCode(200)
            .body("todos.todo[0].title", equalTo("updatedTitle"))
            .body("todos.todo[0].description", equalTo("updatedDesc"));

    given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .when()
            .delete("/todos/" + id)
            .then()
            .statusCode(200);
}
@Test
@DisplayName("Delete /todos/:id XML")
void testDeleteTodoById() {
    String id = given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .body("<todo><title>Todo</title><description>Description</description></todo>")
            .when()
            .post("/todos")
            .then()
            .statusCode(201)
            .extract()
            .xmlPath()
            .getString("todo.id");

    given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .when()
            .get("/todos/" + id)
            .then()
            .statusCode(200)
            .body("todos.todo[0].title", equalTo("Todo"))
            .body("todos.todo[0].description", equalTo("Description"));

    given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .when()
            .delete("/todos/" + id)
            .then()
            .statusCode(200);

    given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .when()
            .get("/todos/" + id)
            .then()
            .statusCode(404);
}
@Test
@DisplayName("Options /todos/:id XML")
void testOptionsByTodo() {
    String id = given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .body("<todo><title>Todo</title><description>Description</description></todo>")
            .when()
            .post("/todos")
            .then()
            .statusCode(201)
            .extract()
            .xmlPath()
            .getString("todo.id");

    Response resp = given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
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
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .when()
            .delete("/todos/" + id)
            .then()
            .statusCode(200);
}
@Test
@DisplayName("Head /todos/:id XML")
void testHeadTodoById() {
    String id = given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .body("<todo><title>Todo</title><description>Description</description></todo>")
            .when()
            .post("/todos")
            .then()
            .statusCode(201)
            .extract()
            .xmlPath()
            .getString("todo.id");

    Response response = given()
            .accept(ContentType.XML)
            .when()
            .head("/todos/" + id)
            .then()
            .statusCode(200)
            .extract()
            .response();

    String body = response.getBody().asString();
    assert body == null || body.isEmpty();

    given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .when()
            .delete("/todos/" + id)
            .then()
            .statusCode(200);
}
@Test
@DisplayName("Patch /todos/:id XML")
void testPatchTodoById() {
    String id = given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .body("<todo><title>Todo</title><description>Description</description></todo>")
            .when()
            .post("/todos")
            .then()
            .statusCode(201)
            .extract()
            .xmlPath()
            .getString("todo.id");

    given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .when()
            .patch("/todos/" + id)
            .then()
            .statusCode(405);

    given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .when()
            .delete("/todos/" + id)
            .then()
            .statusCode(200);
}
@Test
@DisplayName("Post no title /todos")
void testIncompleteTitleTodo() {
    given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .body("<todo><description>Description</description></todo>")
            .when()
            .post("/todos")
            .then()
            .statusCode(400);
}

@Test
@DisplayName("Get invalid id /todos/:id")
void testGetInvalidTodoById() {
    String id = given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .body("<todo><title>Todo</title><description>Description</description></todo>")
            .when()
            .post("/todos")
            .then()
            .statusCode(201)
            .extract()
            .xmlPath()
            .getString("todo.id");

    given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .when()
            .get("/todos/" + id)
            .then()
            .statusCode(200)
            .body("todos.todo[0].title", equalTo("Todo"))
            .body("todos.todo[0].description", equalTo("Description"));

    given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .when()
            .delete("/todos/" + id)
            .then()
            .statusCode(200);

    given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .when()
            .get("/todos/" + id)
            .then()
            .statusCode(404);
}

@Test
@DisplayName("Put invalid id /todos/:id")
void testPutInvalidTodoById() {
    String id = given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .body("<todo><title>Todo</title><description>Description</description></todo>")
            .when()
            .post("/todos")
            .then()
            .statusCode(201)
            .extract()
            .xmlPath()
            .getString("todo.id");

    given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .when()
            .get("/todos/" + id)
            .then()
            .statusCode(200)
            .body("todos.todo[0].title", equalTo("Todo"))
            .body("todos.todo[0].description", equalTo("Description"));

    given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .when()
            .delete("/todos/" + id)
            .then()
            .statusCode(200);

    given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .body("<todo><title>updatedTitle</title><description>updatedDesc</description></todo>")
            .when()
            .put("/todos/" + id)
            .then()
            .statusCode(404);
}

@Test
@DisplayName("Post invalid id /todos/:id")
void testPostInvalidTodoById() {
    String id = given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .body("<todo><title>Todo</title><description>Description</description></todo>")
            .when()
            .post("/todos")
            .then()
            .statusCode(201)
            .extract()
            .xmlPath()
            .getString("todo.id");

    given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .when()
            .get("/todos/" + id)
            .then()
            .statusCode(200)
            .body("todos.todo[0].title", equalTo("Todo"))
            .body("todos.todo[0].description", equalTo("Description"));

    given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .when()
            .delete("/todos/" + id)
            .then()
            .statusCode(200);

    given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .body("<todo><title>updatedTitle</title><description>updatedDesc</description></todo>")
            .when()
            .post("/todos/" + id)
            .then()
            .statusCode(404);
}

@Test
@DisplayName("Delete invalid id /todos/:id")
void testDeleteInvalidTodoById() {
    String id = given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .body("<todo><title>Todo</title><description>Description</description></todo>")
            .when()
            .post("/todos")
            .then()
            .statusCode(201)
            .extract()
            .xmlPath()
            .getString("todo.id");

    given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .when()
            .get("/todos/" + id)
            .then()
            .statusCode(200)
            .body("todos.todo[0].title", equalTo("Todo"))
            .body("todos.todo[0].description", equalTo("Description"));

    given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .when()
            .delete("/todos/" + id)
            .then()
            .statusCode(200);

    given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .body("<todo><title>updatedTitle</title><description>updatedDesc</description></todo>")
            .when()
            .delete("/todos/" + id)
            .then()
            .statusCode(404);
}

@Test
@DisplayName("Head invalid id /todos/:id")
void testHeadInvalidTodoById() {
    String id = given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .body("<todo><title>Todo</title><description>Description</description></todo>")
            .when()
            .post("/todos")
            .then()
            .statusCode(201)
            .extract()
            .xmlPath()
            .getString("todo.id");

    given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .when()
            .get("/todos/" + id)
            .then()
            .statusCode(200)
            .body("todos.todo[0].title", equalTo("Todo"))
            .body("todos.todo[0].description", equalTo("Description"));

    given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .when()
            .delete("/todos/" + id)
            .then()
            .statusCode(200);

    given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .body("<todo><title>updatedTitle</title><description>updatedDesc</description></todo>")
            .when()
            .head("/todos/" + id)
            .then()
            .statusCode(404);
}
}
