package io.volcamp;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
public class GreetingResourceTest {

  @Test
  public void testClassicEndpoint() {
    given()
      .when().get("/classic")
      .then()
      .contentType(ContentType.JSON)
      .statusCode(200)
      .body("index", notNullValue())
      .body("text", notNullValue());
  }

  @Test
  public void testReactiveEndpoint() {
    given()
      .when().get("/reactive")
      .then()
      .contentType(ContentType.JSON)
      .statusCode(200)
      .body("index", notNullValue())
      .body("text", notNullValue());
  }
}
