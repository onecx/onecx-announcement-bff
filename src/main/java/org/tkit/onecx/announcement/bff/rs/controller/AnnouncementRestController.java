package org.tkit.onecx.announcement.bff.rs.controller;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.announcement.bff.rs.mappers.AnnouncementMapper;
import org.tkit.onecx.announcement.bff.rs.mappers.ExceptionMapper;
import org.tkit.onecx.announcement.bff.rs.mappers.ProblemDetailMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.announcement.bff.clients.api.AnnouncementInternalApi;
import gen.org.tkit.onecx.announcement.bff.clients.model.*;
import gen.org.tkit.onecx.announcement.bff.rs.internal.AnnouncementInternalApiService;
import gen.org.tkit.onecx.announcement.bff.rs.internal.model.*;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
@LogService
public class AnnouncementRestController implements AnnouncementInternalApiService {

    @Inject
    @RestClient
    AnnouncementInternalApi client;

    @Inject
    AnnouncementMapper announcementMapper;

    @Inject
    ProblemDetailMapper problemDetailMapper;

    @Inject
    ExceptionMapper exceptionMapper;

    @Override
    public Response createAnnouncement(CreateAnnouncementRequestDTO createAnnouncementRequestDTO) {

        try (Response response = client
                .createAnnouncement(announcementMapper.mapCreateAnnouncement(createAnnouncementRequestDTO))) {
            Announcement announcement = response.readEntity(Announcement.class);
            AnnouncementDTO announcementDTO = announcementMapper.mapAnnouncementToAnnouncementDTO(announcement);
            return Response.status(response.getStatus()).entity(announcementDTO).build();
        }
    }

    @Override
    public Response deleteAnnouncementById(String id) {

        try (Response response = client.deleteAnnouncementById(id)) {
            return Response.status(response.getStatus()).build();
        }
    }

    @Override
    public Response getAllAppsWithAnnouncements() {

        try (Response response = client.getAllAppsWithAnnouncements()) {
            AnnouncementApps announcementApps = response.readEntity(AnnouncementApps.class);
            return Response.status(response.getStatus()).entity(announcementApps).build();
        }
    }

    @Override
    public Response getAnnouncementById(String id) {

        try (Response response = client.getAnnouncementById(id)) {
            Announcement announcement = response.readEntity(Announcement.class);
            AnnouncementDTO announcementDTO = announcementMapper.mapAnnouncementToAnnouncementDTO(announcement);
            return Response.status(response.getStatus()).entity(announcementDTO).build();
        }
    }

    @Override
    public Response getAnnouncements(AnnouncementSearchCriteriaDTO announcementSearchCriteriaDTO) {

        try (Response response = client
                .getAnnouncements(announcementMapper.mapAnnouncementSearchCriteria(announcementSearchCriteriaDTO))) {
            AnnouncementPageResult announcementPageResult = response.readEntity(AnnouncementPageResult.class);
            AnnouncementPageResultDTO announcementPageResultDTO = announcementMapper
                    .mapAnnouncementPageResultToAnnouncementPageResultDTO(announcementPageResult);
            return Response.status(response.getStatus()).entity(announcementPageResultDTO).build();
        }
    }

    @Override
    public Response updateAnnouncementById(String id, UpdateAnnouncementRequestDTO updateAnnouncementRequestDTO) {

        try (Response response = client.updateAnnouncementById(id,
                announcementMapper.mapUpdateAnnouncement(updateAnnouncementRequestDTO))) {
            Announcement announcement = response.readEntity(Announcement.class);
            AnnouncementDTO announcementDTO = announcementMapper.mapAnnouncementToAnnouncementDTO(announcement);
            return Response.status(response.getStatus()).entity(announcementDTO).build();
        }
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {

        return exceptionMapper.constraint(ex);
    }

    @ServerExceptionMapper
    public Response restException(WebApplicationException ex) {

        return Response.status(ex.getResponse().getStatus()).build();
    }
}
