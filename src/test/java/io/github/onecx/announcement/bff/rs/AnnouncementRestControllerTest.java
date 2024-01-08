package io.github.onecx.announcement.bff.rs;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.ArrayList;
import java.util.List;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;

import gen.io.github.onecx.announcement.bff.clients.model.*;
import gen.io.github.onecx.announcement.bff.rs.internal.model.*;
import io.github.onecx.announcement.bff.rs.controller.AnnouncementRestController;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(AnnouncementRestController.class)
class AnnouncementRestControllerTest extends AbstractTest {

    private static final String ANNOUNCEMENT_SVC_INTERNAL_API_BASE_PATH = "/internal/announcements";

    @InjectMockServerClient
    MockServerClient mockServerClient;

    @AfterEach
    void resetMockserver() {
        mockServerClient.reset();
    }

    @Test
    void addAnnouncement_shouldReturnAnnouncement() {

        // Request data to svc
        Announcement data = new Announcement();
        data.setAppId("appId");
        data.setContent("AnnouncmentContent");

        // svc call prepare mock endpoint
        mockServerClient
                .when(request().withPath(ANNOUNCEMENT_SVC_INTERNAL_API_BASE_PATH)
                        .withMethod(HttpMethod.POST))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        // bff call input
        CreateAnnouncementRequestDTO input = new CreateAnnouncementRequestDTO();
        input.setAppId("appId1");

        // bff call
        var response = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(input)
                .post()
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(AnnouncementDTO.class);

        // Assertions
        Assertions.assertNotNull(response);
        Assertions.assertEquals(data.getAppId(), response.getAppId());
        Assertions.assertEquals(data.getContent(), response.getContent());
    }

    @Test
    void getAnnouncements_shouldReturnAnnouncementPageResults() {
        Announcement announcement = new Announcement();
        announcement.setAppId("appId");
        announcement.setContent("AnnouncmentContent");
        List<Announcement> announcements = new ArrayList<>();
        announcements.add(announcement);

        // Request data to svc
        AnnouncementPageResult data = new AnnouncementPageResult();
        data.setSize(5);
        data.setTotalPages(1L);
        data.setNumber(2);
        data.setStream(announcements);

        // svc call prepare mock endpoint
        mockServerClient
                .when(request().withPath(ANNOUNCEMENT_SVC_INTERNAL_API_BASE_PATH + "/search")
                        .withMethod(HttpMethod.POST))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        // bff call input
        AnnouncementSearchCriteria input = new AnnouncementSearchCriteria();

        // bff call
        var response = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(input)
                .post("/search")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(AnnouncementPageResult.class);

        // Assertions
        Assertions.assertNotNull(response);
        Assertions.assertEquals(data.getNumber(), response.getNumber());
        Assertions.assertEquals(data.getSize(), response.getSize());
        Assertions.assertEquals(1, data.getStream().size());
        Assertions.assertEquals(data.getStream().get(0), announcement);
    }

    @Test
    void getAnnouncementById_shouldReturnAnnouncement() {
        // Request data to svc
        Announcement data = new Announcement();
        data.setAppId("appIdTest1");

        // svc call prepare mock endpoint
        mockServerClient
                .when(request()
                        .withPath(ANNOUNCEMENT_SVC_INTERNAL_API_BASE_PATH + "/1")
                        .withMethod(HttpMethod.GET))
                .withPriority(100)
                .respond(
                        httpRequest -> response()
                                .withStatusCode(Response.Status.OK.getStatusCode())
                                .withContentType(MediaType.APPLICATION_JSON)
                                .withBody(JsonBody.json(data)));

        // bff call
        var response = given()
                .when()
                .contentType(APPLICATION_JSON)
                .get("/1")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(AnnouncementDTO.class);

        // Assertions
        Assertions.assertNotNull(response);
        Assertions.assertEquals("appIdTest1", response.getAppId());
    }

    @Test
    void getAllAppsWithAnnouncements_shouldReturnAnnouncementApps() {
        // Request data to svc
        AnnouncementApps data = new AnnouncementApps();
        List<String> appIds = new ArrayList<>();
        appIds.add("1");
        appIds.add("2");
        data.setAppIds(appIds);

        // svc call prepare mock endpoint
        mockServerClient
                .when(request()
                        .withPath(ANNOUNCEMENT_SVC_INTERNAL_API_BASE_PATH + "/appIds")
                        .withMethod(HttpMethod.GET))
                .withPriority(100)
                .respond(
                        httpRequest -> response()
                                .withStatusCode(Response.Status.OK.getStatusCode())
                                .withContentType(MediaType.APPLICATION_JSON)
                                .withBody(JsonBody.json(data)));

        System.out.println("ARRAY: " + data);

        // bff call
        var response = given()
                .when()
                .contentType(APPLICATION_JSON)
                .get("/appIds")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(AnnouncementApps.class);

        // Assertions
        Assertions.assertNotNull(response);
        Assertions.assertEquals(2, response.getAppIds().size());
        Assertions.assertEquals(response.getAppIds(), data.getAppIds());

    }

    @Test
    void getAnnouncementById_shouldReturnNotFound() {

        String idNotFound = "notFoundId";

        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();
        problemDetailResponse.setErrorCode("404");
        // svc call prepare mock endpoint
        mockServerClient
                .when(request()
                        .withPath(ANNOUNCEMENT_SVC_INTERNAL_API_BASE_PATH + "/" + idNotFound)
                        .withMethod(HttpMethod.GET))
                .withPriority(100)
                .respond(
                        httpRequest -> response()
                                .withStatusCode(Response.Status.NOT_FOUND.getStatusCode())
                                .withContentType(MediaType.APPLICATION_JSON));

        // bff call
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .get(idNotFound)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());

    }

    @Test
    void deleteAnnouncementById() {

        String deleteId = "deleteId_NO_CONTENT";

        mockServerClient
                .when(request()
                        .withPath(ANNOUNCEMENT_SVC_INTERNAL_API_BASE_PATH + "/" + deleteId)
                        .withMethod(HttpMethod.DELETE))
                .withPriority(100)
                .respond(
                        httpRequest -> response()
                                .withStatusCode(Response.Status.NO_CONTENT.getStatusCode()));

        // bff call
        var response = given()
                .when()
                .contentType(APPLICATION_JSON)
                .delete(deleteId)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        // Assertions
        Assertions.assertNotNull(response);
    }

    @Test
    void updateAnnouncementById() {
        String updateId = "updateId_NO_CONTENT";
        Announcement data = new Announcement();
        data.setAppId("appIdTest1");

        mockServerClient
                .when(request()
                        .withPath(ANNOUNCEMENT_SVC_INTERNAL_API_BASE_PATH + "/" + updateId)
                        .withMethod(HttpMethod.PUT))
                .withPriority(100)
                .respond(
                        httpRequest -> response()
                                .withStatusCode(Response.Status.OK.getStatusCode())
                                .withBody(JsonBody.json(data)));

        UpdateAnnouncementRequestDTO input = new UpdateAnnouncementRequestDTO();
        // bff call
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(input)
                .put(updateId)
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

    }

    @Test
    void updateAnnouncementById_shouldReturnBadRequest() {
        String updateId = "updateId_NO_CONTENT";
        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();
        problemDetailResponse.setErrorCode("400");

        mockServerClient
                .when(request()
                        .withPath(ANNOUNCEMENT_SVC_INTERNAL_API_BASE_PATH + "/" + updateId)
                        .withMethod(HttpMethod.PUT))
                .withPriority(100)
                .respond(
                        httpRequest -> response()
                                .withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                                .withBody(JsonBody.json(problemDetailResponse)));
        ;

        UpdateAnnouncementRequestDTO input = new UpdateAnnouncementRequestDTO();
        // bff call
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(input)
                .put(updateId)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

    }

    @Test
    void addAnnouncements_shouldReturnBadRequest_whenRunningIntoValidationConstraints() {

        ProblemDetailResponse data = new ProblemDetailResponse();
        data.setErrorCode("CONSTRAINT_VIOLATIONS");
        data.setDetail(
                "createProduct.arg0.name: must not be null");
        List<ProblemDetailInvalidParam> list = new ArrayList<>();
        ProblemDetailInvalidParam param1 = new ProblemDetailInvalidParam();
        param1.setName("createProduct.arg0.name");
        param1.setMessage("must not be null");
        list.add(param1);
        data.setParams(null);
        data.setInvalidParams(list);

        mockServerClient.when(request().withPath(ANNOUNCEMENT_SVC_INTERNAL_API_BASE_PATH).withMethod(HttpMethod.POST))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        CreateAnnouncementRequestDTO emptyRequestDTO;

        var response = given()
                .when()
                .contentType(APPLICATION_JSON)
                .post()
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProblemDetailResponseDTO.class);

        Assertions.assertNotNull(response);
    }
}
