import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;
// Для использования equalTo при проверке body.
// Можно заменить на org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.*;

import io.restassured.response.Response;

import org.junit.Test;

public class TestGET {

    String qmmmsgpackageURI = "http://qmmmsgpackage.msghub.qrun.diasoft.ru:80";
    String getParams = "/qmmmsgpackage/MsgPackage/v1/";
    int msgPackageID = 1070;
    int testMsgPackageIdFor404 = 548489489;

    @Test
    public void getWithExtract() {
        RestAssured.baseURI = qmmmsgpackageURI;

        Response response = given()
                .when()
                .get(getParams + msgPackageID)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("sender", equalTo("МО"))
                .body("msgPackageId", equalTo(1070))
                .extract()
                .response();

        response.prettyPrint();
    }

    @Test
    public void getWith404() {
        RestAssured.baseURI = qmmmsgpackageURI;

        given()
                .when()
                .get(getParams + testMsgPackageIdFor404)
                .then()
                .statusCode(404);

    }
}
