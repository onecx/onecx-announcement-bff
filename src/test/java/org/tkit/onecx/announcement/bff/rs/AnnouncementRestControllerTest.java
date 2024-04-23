package org.tkit.onecx.announcement.bff.rs;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.tkit.onecx.announcement.bff.rs.controller.AnnouncementRestController;

import gen.org.tkit.onecx.announcement.bff.rs.internal.model.*;
import gen.org.tkit.onecx.announcement.client.model.*;
import gen.org.tkit.onecx.workspace.client.model.WorkspaceAbstract;
import gen.org.tkit.onecx.workspace.client.model.WorkspacePageResult;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(AnnouncementRestController.class)
class AnnouncementRestControllerTest extends AbstractTest {

    private static final String ANNOUNCEMENT_SVC_INTERNAL_API_BASE_PATH = "/internal/announcements";

    @InjectMockServerClient
    MockServerClient mockServerClient;

    static final String mockId = "MOCK";

    @BeforeEach
    void resetExpectation() {
        try {
            mockServerClient.clear(mockId);
        } catch (Exception ex) {
            //  mockId not existing
        }
    }

    @Test
    void createAnnouncement_shouldReturnAnnouncement() {
        var offsetDateTime = OffsetDateTime.parse("2023-11-30T13:53:03.688710200+01:00");

        // Request data to svc
        Announcement data = new Announcement();
        data.setAppId("appId");
        data.setContent("AnnouncmentContent");
        data.setTitle("announcementTitle");
        data.startDate(offsetDateTime);

        // svc call prepare mock endpoint
        mockServerClient
                .when(request().withPath(ANNOUNCEMENT_SVC_INTERNAL_API_BASE_PATH)
                        .withMethod(HttpMethod.POST))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        // bff call input
        CreateAnnouncementRequestDTO input = new CreateAnnouncementRequestDTO();
        input.setAppId("appId1");
        input.setTitle("announcementTitle");
        input.startDate(offsetDateTime);

        // standard USER get FORBIDDEN with only READ permission
        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(USER))
                .header(APM_HEADER_PARAM, USER)
                .contentType(APPLICATION_JSON)
                .body(input)
                .post()
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());

        // bff call
        var response = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
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
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        // bff call input
        AnnouncementSearchCriteria input = new AnnouncementSearchCriteria();

        // bff call
        var response = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
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
                .withId(mockId)
                .respond(
                        httpRequest -> response()
                                .withStatusCode(Response.Status.OK.getStatusCode())
                                .withContentType(MediaType.APPLICATION_JSON)
                                .withBody(JsonBody.json(data)));

        // bff call
        var response = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
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
                .withId(mockId)
                .respond(
                        httpRequest -> response()
                                .withStatusCode(Response.Status.OK.getStatusCode())
                                .withContentType(MediaType.APPLICATION_JSON)
                                .withBody(JsonBody.json(data)));

        System.out.println("ARRAY: " + data);

        // bff call
        var response = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
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
                .withId(mockId)
                .respond(
                        httpRequest -> response()
                                .withStatusCode(Response.Status.NOT_FOUND.getStatusCode())
                                .withContentType(MediaType.APPLICATION_JSON));

        // bff call
        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
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
                .withId(mockId)
                .respond(
                        httpRequest -> response()
                                .withStatusCode(Response.Status.NO_CONTENT.getStatusCode()));

        // bff call
        var response = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .delete(deleteId)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        // Assertions
        Assertions.assertNotNull(response);
    }

    @Test
    void updateAnnouncementById() {
        var offsetDateTime = OffsetDateTime.parse("2023-11-30T13:53:03.688710200+01:00");

        String updateId = "updateId_NO_CONTENT";
        Announcement data = new Announcement();
        data.setAppId("appIdTest1");

        mockServerClient
                .when(request()
                        .withPath(ANNOUNCEMENT_SVC_INTERNAL_API_BASE_PATH + "/" + updateId)
                        .withMethod(HttpMethod.PUT))
                .withId(mockId)
                .respond(
                        httpRequest -> response()
                                .withStatusCode(Response.Status.OK.getStatusCode())
                                .withBody(JsonBody.json(data)));

        UpdateAnnouncementRequestDTO input = new UpdateAnnouncementRequestDTO();
        input.setStartDate(offsetDateTime);
        input.setTitle("appTitle");
        input.setModificationCount(0);
        // bff call
        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
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
                .withId(mockId)
                .respond(
                        httpRequest -> response()
                                .withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                                .withBody(JsonBody.json(problemDetailResponse)));
        ;

        UpdateAnnouncementRequestDTO input = new UpdateAnnouncementRequestDTO();
        // bff call
        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(input)
                .put(updateId)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

    }

    @Test
    void createAnnouncement_shouldReturnBadRequest_whenRunningIntoValidationConstraints() {

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
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        var response = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .post()
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProblemDetailResponseDTO.class);

        Assertions.assertNotNull(response);
    }

    @Test
    void getAllWorkspaceNames() {

        var result = new WorkspacePageResult().stream(List.of(
                new WorkspaceAbstract().name("testWorkspace").description("testWorkspace")));

        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/v1/workspaces/search")
                        .withMethod(HttpMethod.POST))
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(result)));

        var response = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .get("/workspaces")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(HashSet.class);

        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.contains("testWorkspace"));
    }

    @Test
    void searchActiveAnnouncements_shouldReturnAnnouncementPageResults() {

        Announcement a1 = new Announcement();
        a1.setAppId("appId");
        a1.setContent("AnnouncmentContent");
        a1.setPriority(AnnouncementPriorityType.IMPORTANT);
        a1.setTitle("A1");
        a1.setWorkspaceName("w1");
        a1.setStartDate(OffsetDateTime.parse("2024-04-11T10:09:24-04:00"));
        a1.setEndDate(OffsetDateTime.parse("2024-04-25T10:09:24-04:00"));
        Announcement a2 = new Announcement();
        a2.setAppId("appId");
        a2.setContent("AnnouncmentContent");
        a2.setPriority(AnnouncementPriorityType.NORMAL);
        a2.setTitle("A2");
        a2.setWorkspaceName("w1");
        a2.setStartDate(OffsetDateTime.parse("2024-04-11T10:09:24-04:00"));
        a2.setEndDate(OffsetDateTime.parse("2024-04-25T10:09:24-04:00"));
        Announcement a3 = new Announcement();
        a3.setAppId("appId");
        a3.setContent("AnnouncmentContent");
        a3.setPriority(AnnouncementPriorityType.IMPORTANT);
        a3.setTitle("A3");
        a3.setWorkspaceName(null);
        a3.setStartDate(OffsetDateTime.parse("2024-04-11T10:09:24-04:00"));
        a3.setEndDate(OffsetDateTime.parse("2024-04-25T10:09:24-04:00"));
        Announcement a4 = new Announcement();
        a4.setAppId("appId");
        a4.setContent("Shouldn't be returned");
        a4.setPriority(AnnouncementPriorityType.IMPORTANT);
        a4.setTitle("A4");
        a4.setWorkspaceName("w2");
        a4.setStartDate(OffsetDateTime.parse("2024-04-11T10:09:24-04:00"));
        a4.setEndDate(OffsetDateTime.parse("2024-04-25T10:09:24-04:00"));
        List<Announcement> announcements = new ArrayList<>();
        announcements.add(a1);
        announcements.add(a2);

        List<Announcement> announcements2 = new ArrayList<>();
        announcements2.add(a3);
        announcements2.add(a4);

        // Request data to svc
        AnnouncementPageResult data = new AnnouncementPageResult();
        data.setSize(2);
        data.setTotalPages(1L);
        data.setNumber(0);
        data.setStream(announcements);

        AnnouncementPageResult data2 = new AnnouncementPageResult();
        data2.setSize(1);
        data2.setTotalPages(1L);
        data2.setNumber(0);
        data2.setStream(announcements2);

        AnnouncementSearchCriteria criteria1 = new AnnouncementSearchCriteria();
        criteria1.setWorkspaceName("w1");
        AnnouncementSearchCriteria criteria2 = new AnnouncementSearchCriteria();

        // svc call prepare mock endpoint
        mockServerClient
                .when(request().withPath(ANNOUNCEMENT_SVC_INTERNAL_API_BASE_PATH + "/search")
                        .withBody(JsonBody.json(criteria1))
                        .withMethod(HttpMethod.POST))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        mockServerClient
                .when(request().withPath(ANNOUNCEMENT_SVC_INTERNAL_API_BASE_PATH + "/search")
                        .withBody(JsonBody.json(criteria2))
                        .withMethod(HttpMethod.POST))
                .withId("mock2")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data2)));

        // bff call input
        ActiveAnnouncementsSearchCriteriaDTO input = new ActiveAnnouncementsSearchCriteriaDTO();
        input.setWorkspaceName("w1");
        input.setCurrentDate(OffsetDateTime.parse("2024-04-24T12:15:50-04:00"));

        // bff call
        var response = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(input)
                .post("/active/search")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(AnnouncementPageResult.class);

        // Assertions
        Assertions.assertNotNull(response);
        Assertions.assertEquals(response.getStream().size(), 3);
        Assertions.assertEquals(response.getStream().get(0).getPriority(), AnnouncementPriorityType.IMPORTANT);
        Assertions.assertNull(response.getStream().get(0).getWorkspaceName());
        Assertions.assertEquals(response.getStream().get(1).getPriority(), AnnouncementPriorityType.IMPORTANT);
        Assertions.assertEquals(response.getStream().get(1).getWorkspaceName(), "w1");

        Assertions.assertEquals(response.getStream().get(2).getPriority(), AnnouncementPriorityType.NORMAL);
        //only globals
        ActiveAnnouncementsSearchCriteriaDTO input2 = new ActiveAnnouncementsSearchCriteriaDTO();
        input2.setCurrentDate(OffsetDateTime.parse("2024-04-24T12:15:50-04:00"));
        var responseOnlyGlobals = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(input2)
                .post("/active/search")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(AnnouncementPageResult.class);

        Assertions.assertNotNull(responseOnlyGlobals);
        Assertions.assertEquals(responseOnlyGlobals.getStream().size(), 1);
        Assertions.assertEquals(responseOnlyGlobals.getStream().get(0).getTitle(), "A3");

        mockServerClient.clear("mock2");
    }
}
