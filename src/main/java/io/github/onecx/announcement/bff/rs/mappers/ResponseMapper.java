package io.github.onecx.announcement.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.io.github.onecx.announcement.bff.clients.model.Announcement;
import gen.io.github.onecx.announcement.bff.clients.model.AnnouncementPageResult;
import gen.io.github.onecx.announcement.bff.rs.internal.model.AnnouncementDTO;
import gen.io.github.onecx.announcement.bff.rs.internal.model.AnnouncementPageResultDTO;

@Mapper(uses = OffsetDateTimeMapper.class)
public interface ResponseMapper {

    @Mapping(target = "version", ignore = true)
    AnnouncementDTO mapAnnouncementToAnnouncementDTO(Announcement announcement);

    @Mapping(target = "removeStreamItem", ignore = true)
    AnnouncementPageResultDTO mapAnnouncementPageResultToAnnouncementPageResultDTO(
            AnnouncementPageResult announcementPageResult);

}
