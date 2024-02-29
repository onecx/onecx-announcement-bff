package org.tkit.onecx.announcement.bff.rs.mappers;

import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.announcement.bff.rs.internal.model.*;
import gen.org.tkit.onecx.announcement.client.model.*;
import gen.org.tkit.onecx.workspace.client.model.WorkspaceAbstract;
import gen.org.tkit.onecx.workspace.client.model.WorkspacePageResult;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface AnnouncementMapper {

    default Set<String> workspaceNames(WorkspacePageResult result) {
        if (result == null || result.getStream() == null) {
            return Set.of();
        }
        return result.getStream().stream().map(WorkspaceAbstract::getName).collect(Collectors.toSet());
    }

    CreateAnnouncementRequest mapCreateAnnouncement(CreateAnnouncementRequestDTO createAnnouncementRequestDTO);

    UpdateAnnouncementRequest mapUpdateAnnouncement(UpdateAnnouncementRequestDTO updateAnnouncementRequestDTO);

    AnnouncementSearchCriteria mapAnnouncementSearchCriteria(AnnouncementSearchCriteriaDTO searchAnnouncementRequestDTO);

    AnnouncementDTO mapAnnouncementToAnnouncementDTO(Announcement announcement);

    @Mapping(target = "removeStreamItem", ignore = true)
    AnnouncementPageResultDTO mapAnnouncementPageResultToAnnouncementPageResultDTO(
            AnnouncementPageResult announcementPageResult);
}
