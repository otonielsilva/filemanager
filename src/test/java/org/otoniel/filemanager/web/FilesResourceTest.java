package org.otoniel.filemanager.web;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.io.File;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.otoniel.resources.models.FileObject;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@QuarkusTest
class FilesResourceTest {

    @Test
    @Order(1)
    void createFile_uploadingAFile() {

        given()
                .multiPart("file", new File("src/test/resources/dummy.html"), "text/html")
                .formParam("fileName", "dummy.html")
            .when().post("/files")
            .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("originalFileName", equalTo("dummy.html"))
                .body("size", notNullValue())
                .body("lastVersion", equalTo(1));
    }

    @Test
    @Order(2)
    void createFile_notProvidingAFile_returnError() {
        given()
                .contentType(ContentType.MULTIPART)
                .when().post("/files")
                .then()
                .statusCode(400)
                .body("errorMessage", notNullValue());
    }

    @Test
    @Order(3)
    void update_existingFile() {
        final Response response = given()
                .multiPart("file", new File("src/test/resources/dummy.html"), "text/html")
                .formParam("fileName", "dummy.html")
                .when().post("/files");
        String id = response.as(FileObject.class).getId();


        given()
                .multiPart("file", new File("src/test/resources/dummy.html"), "text/html")
                .formParam("fileName", "dummy2.html")
            .when()
                .put("/files/" + id)
            .then()
                .statusCode(200)
                .body("id", equalTo(id))
                .body("originalFileName", equalTo("dummy2.html"))
                .body("size", notNullValue())
                .body("lastVersion", equalTo(2));
    }

    @Test
    @Order(4)
    void delete_existingFile() {
        final Response response = given()
                .multiPart("file", new File("src/test/resources/dummy.html"), "text/html")
                .formParam("fileName", "dummy.html")
                .when().post("/files");
        String id = response.as(FileObject.class).getId();


        given()
                .multiPart("file", new File("src/test/resources/dummy.html"), "text/html")
                .formParam("fileName", "dummy2.html")
                .when()
                .delete("/files/" + id)
                .then()
                .statusCode(204);
    }

    @Test
    @Order(5)
    void update_nonExistingFile() {
        given()
                .multiPart("file", new File("src/test/resources/dummy.html"), "text/html")
                .formParam("fileName", "dummy2.html")
            .when()
                .put("/files/non-existing")
            .then()
                .statusCode(404)
                .body("errorMessage", notNullValue());
    }

}