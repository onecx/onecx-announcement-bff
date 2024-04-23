package org.tkit.onecx.announcement.bff.rs.mappers;

import java.util.*;
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

    @Mapping(target = "type", ignore = true)
    @Mapping(target = "title", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "startDateTo", source = "currentDate")
    @Mapping(target = "startDateFrom", ignore = true)
    @Mapping(target = "priority", ignore = true)
    @Mapping(target = "endDateTo", ignore = true)
    @Mapping(target = "endDateFrom", source = "currentDate")
    @Mapping(target = "content", ignore = true)
    @Mapping(target = "appId", ignore = true)
    AnnouncementSearchCriteria mapActiveAnnouncementSearchCriteria(
            ActiveAnnouncementsSearchCriteriaDTO activeAnnouncementsSearchCriteriaDTO);

    @Mapping(target = "removeStreamItem", ignore = true)
    ActiveAnnouncementsPageResultDTO mapAnnouncementPageResultToActiveAnnouncementPageResultDTO(
            AnnouncementPageResult announcementPageResult);

    default ActiveAnnouncementsPageResultDTO merge(ActiveAnnouncementsPageResultDTO announcementPageResultDTO,
            ActiveAnnouncementsPageResultDTO announcementUnassignedPageResultDTO,
            ActiveAnnouncementsSearchCriteriaDTO searchCriteria) {
        var mergedWithoutDuplicates = new HashSet<AnnouncementAbstractDTO>();
        mergedWithoutDuplicates.addAll(announcementPageResultDTO.getStream().stream().toList());
        if (!announcementUnassignedPageResultDTO.getStream().isEmpty()) {
            mergedWithoutDuplicates.addAll(announcementUnassignedPageResultDTO.getStream().stream().toList());
        }

        ActiveAnnouncementsPageResultDTO mergedResult = new ActiveAnnouncementsPageResultDTO();
        mergedResult.setTotalElements((long) mergedWithoutDuplicates.size());
        mergedResult.setStream(sort(mergedWithoutDuplicates.stream().toList()));
        mergedResult.setNumber(searchCriteria.getPageNumber());
        mergedResult.setSize(searchCriteria.getPageSize());
        mergedResult.setTotalPages(1L);
        if (mergedResult.getTotalElements() / mergedResult.getSize() > 0) {
            mergedResult.setTotalPages(mergedResult.getTotalElements() / mergedResult.getSize());
        }
        int startIndex = 0;
        int endIndex = Math.toIntExact(mergedResult.getTotalElements());
        if (searchCriteria.getPageNumber() != 0) {
            startIndex = (searchCriteria.getPageSize() * searchCriteria.getPageNumber());
            endIndex = startIndex + searchCriteria.getPageSize() - 1;
        }
        if (searchCriteria.getPageSize() != 100) {
            endIndex = startIndex + searchCriteria.getPageSize();
        }
        if (!(startIndex > mergedResult.getStream().size())) {
            mergedResult.setStream(
                    mergedResult.getStream().subList(startIndex, Math.min(endIndex, mergedResult.getStream().size())));
        } else {
            mergedResult.setStream(new ArrayList<>());
        }

        return mergedResult;
    }

    default List<AnnouncementAbstractDTO> sort(List<AnnouncementAbstractDTO> mergedResult) {
        List<AnnouncementAbstractDTO> sortedList = new ArrayList<>(mergedResult);
        sortedList.sort(Comparator.comparing(AnnouncementAbstractDTO::getPriority)
                .thenComparing((AnnouncementAbstractDTO item) -> item.getWorkspaceName() == null ? 0 : 1)
                .thenComparing(AnnouncementAbstractDTO::getTitle));
        return sortedList;
    }
}
