package io.github.onecx.announcement.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.io.github.onecx.announcement.bff.clients.model.*;
import gen.io.github.onecx.announcement.bff.rs.internal.model.*;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface AnnouncementMapper {

    CreateAnnouncementRequest mapCreateAnnouncement(CreateAnnouncementRequestDTO createAnnouncementRequestDTO);

    UpdateAnnouncementRequest mapUpdateAnnouncement(UpdateAnnouncementRequestDTO updateAnnouncementRequestDTO);

    AnnouncementSearchCriteria mapAnnouncementSearchCriteria(AnnouncementSearchCriteriaDTO searchAnnouncementRequestDTO);

    AnnouncementDTO mapAnnouncementToAnnouncementDTO(Announcement announcement);

    @Mapping(target = "removeStreamItem", ignore = true)
    AnnouncementPageResultDTO mapAnnouncementPageResultToAnnouncementPageResultDTO(
            AnnouncementPageResult announcementPageResult);
}
