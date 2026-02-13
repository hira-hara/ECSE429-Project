import static io.restassured.RestAssured.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestDemo {
    @Test
    @DisplayName("Check if the Todo Manager API is running")
    void testApiIsUp() {
        // Most Todo Manager APIs run on port 4567 or 8080 by default
        given()
            .baseUri("http://localhost:4567")
        .when()
            .get("/todos")
        .then()
            .statusCode(200); // If this fails, your JAR isn't running!
    }
}


