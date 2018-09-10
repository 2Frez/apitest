package api;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;

public class GetProductTest {

    public static final String NOT_FOUND = "{\"code\": 404,\"description\": \"Not Found\"}";
    private static final String BAD_REQUEST = "{\"code\": 400,\"description\": \"Bad Request\"}";
    private static final String SUCCESS = "{\"id\": 666,\"name\": \"banana\",\"price\": 100}";
    private WireMockServer wireMockServer;

    @BeforeClass
    public void init() {
        wireMockServer = new WireMockServer(wireMockConfig().port(8089));
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
        stubFor(post("/api/product/get/success")
                .willReturn(okJson(SUCCESS)));
        stubFor(post("/api/product/get/not-found")
                .willReturn(okJson(NOT_FOUND)));
        stubFor(post("/api/product/get/bad-request")
                .willReturn(okJson(BAD_REQUEST)));
    }

    @AfterClass
    public void stopMockServer() {
        wireMockServer.stop();
    }

    @Test
    public void getProduct() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("id", 666);
        RequestSpecification request = RestAssured.given();
        request.header("Content-Type", "application/json");
        request.body(requestBody.toString());
        Response response = request.post("http://localhost:8089/api/product/get/success");

        JsonPath jsonPath = response.jsonPath();
//        В задании приведена невалидная схема для товара - проверка на схеме невозможна
//        assertThat("Json schema is not as expected",
//                response.body().asString(), matchesJsonSchemaInClasspath("productResponseSchema.json"));
        assertThat("Unexpected response code", jsonPath.get("id").equals(666));
        assertThat("Unexpected response name", jsonPath.get("name").equals("banana"));
        assertThat("Unexpected response price", jsonPath.get("price").equals(100));
    }

    @Test
    public void getNonexistentProduct() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("id", 1);
        RequestSpecification request = RestAssured.given();
        request.header("Content-Type", "application/json");
        request.body(requestBody.toString());
        Response response = request.post("http://localhost:8089/api/product/get/not-found");

        assertThat("Check Json schema",
                response.body().asString(), matchesJsonSchemaInClasspath("statusResponseSchema.json"));
        assertThat("Unexpected response code", response.jsonPath().get("code").equals(404));
        assertThat("Unexpected response description", response.jsonPath().get("description").equals("Not Found"));
    }

    @Test
    public void getProductByName() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("id", "banana");
        RequestSpecification request = RestAssured.given();
        request.header("Content-Type", "application/json");
        request.body(requestBody.toString());
        Response response = request.post("http://localhost:8089/api/product/get/bad-request");

        assertThat("Check Json schema",
                response.body().asString(), matchesJsonSchemaInClasspath("statusResponseSchema.json"));
        assertThat("Unexpected response code", response.jsonPath().get("code").equals(400));
        assertThat("Unexpected response description", response.jsonPath().get("description").equals("Bad Request"));
    }
}