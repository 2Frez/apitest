package api;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.restassured.RestAssured;
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

public class AddProductTest {

    private static final String SUCCESS = "{\"code\": 200,\"description\": \"success\"}";
    private static final String BAD_REQUEST = "{\"code\": 400,\"description\": \"Bad Request\"}";
    private static final String INTERNAL_ERROR = "{\"code\": 500,\"description\": \"Internal Server Error\"}";
    private WireMockServer wireMockServer;

    @BeforeClass
    public void init() {
        wireMockServer = new WireMockServer(wireMockConfig().port(8089));
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
        stubFor(post("/api/product/add/success")
                .willReturn(okJson(SUCCESS)));
        stubFor(post("/api/product/add/error")
                .willReturn(okJson(INTERNAL_ERROR)));
        stubFor(post("/api/product/add/bad-request")
                .willReturn(okJson(BAD_REQUEST)));
    }

    @AfterClass
    public void stopMockServer() {
        wireMockServer.stop();
    }

    @Test
    public void addNewProduct() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("id", 674);
        requestBody.put("name", "banana");
        requestBody.put("price", 100);
        RequestSpecification request = RestAssured.given();
        request.header("Content-Type", "application/json");
        request.body(requestBody.toString());

        Response response = request.post("http://localhost:8089/api/product/add/success");
        assertThat("Check Json schema",
                response.body().asString(), matchesJsonSchemaInClasspath("statusResponseSchema.json"));
        assertThat("Unexpected code", response.jsonPath().get("code").equals(200));
        assertThat("Unexpected description", response.jsonPath().get("description").equals("success"));
    }

    @Test
    public void addProductWithPriceAsString() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("id", 666);
        requestBody.put("name", "banana");
        requestBody.put("price", "100");
        RequestSpecification request = RestAssured.given();
        request.header("Content-Type", "application/json");
        request.body(requestBody.toString());

        Response response = request.post("http://localhost:8089/api/product/add/error");
        assertThat("Check Json schema",
                response.body().asString(), matchesJsonSchemaInClasspath("statusResponseSchema.json"));
        assertThat("Unexpected code", response.jsonPath().get("code").equals(500));
        assertThat("Unexpected description", response.jsonPath().get("description").equals("Internal Server Error"));
    }

    @Test
    public void addProductWithoutName() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("id", 671);
        requestBody.put("price", 100);
        RequestSpecification request = RestAssured.given();
        request.header("Content-Type", "application/json");
        request.body(requestBody.toString());

        Response response = request.post("http://localhost:8089/api/product/add/bad-request");
        assertThat("Check Json schema",
                response.body().asString(), matchesJsonSchemaInClasspath("statusResponseSchema.json"));
        assertThat("Unexpected code", response.jsonPath().get("code").equals(400));
        assertThat("Unexpected description", response.jsonPath().get("description").equals("Bad Request"));
    }
}
