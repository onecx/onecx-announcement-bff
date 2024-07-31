package org.tkit.onecx.announcement.bff.rs.mappers;

import java.util.*;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.announcement.bff.rs.internal.model.*;
import gen.org.tkit.onecx.announcement.client.model.*;
import gen.org.tkit.onecx.product.store.model.ProductItemPageResult;
import gen.org.tkit.onecx.product.store.model.ProductItemSearchCriteria;
import gen.org.tkit.onecx.workspace.client.model.WorkspacePageResult;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface AnnouncementMapper {

    default List<WorkspaceAbstractDTO> workspaceNames(WorkspacePageResult result) {
        if (result == null || result.getStream() == null) {
            return List.of();
        }
        List<WorkspaceAbstractDTO> abstracts = new ArrayList<>();
        result.getStream().forEach(workspaceAbstract -> {
            WorkspaceAbstractDTO abstractDTO = new WorkspaceAbstractDTO();
            abstractDTO.setName(workspaceAbstract.getName());
            abstractDTO.setDisplayName(workspaceAbstract.getDisplayName());
            abstracts.add(abstractDTO);
        });
        return abstracts;
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
    @Mapping(target = "productName", ignore = true)
    @Mapping(target = "workspaceName", ignore = true)
    AnnouncementSearchCriteria mapActiveAnnouncementSearchCriteria(
            ActiveAnnouncementsSearchCriteriaDTO activeAnnouncementsSearchCriteriaDTO);

    @Mapping(target = "removeStreamItem", ignore = true)
    ActiveAnnouncementsPageResultDTO mapAnnouncementPageResultToActiveAnnouncementPageResultDTO(
            AnnouncementPageResult announcementPageResult);

    default List<AnnouncementAbstractDTO> sort(List<AnnouncementAbstractDTO> mergedResult) {
        List<AnnouncementAbstractDTO> sortedList = new ArrayList<>(mergedResult);
        sortedList.sort(Comparator.comparing(AnnouncementAbstractDTO::getPriority)
                .thenComparing((AnnouncementAbstractDTO item) -> item.getWorkspaceName() == null ? 0 : 1)
                .thenComparing(AnnouncementAbstractDTO::getTitle));
        return sortedList;
    }

    default ActiveAnnouncementsPageResultDTO filterAndSort(ActiveAnnouncementsPageResultDTO announcementPageResultDTO,
            ActiveAnnouncementsSearchCriteriaDTO activeAnnouncementsSearchCriteriaDTO) {
        ActiveAnnouncementsPageResultDTO pageResult = new ActiveAnnouncementsPageResultDTO();

        if (activeAnnouncementsSearchCriteriaDTO.getWorkspaceName() != null) {
            pageResult.setStream(announcementPageResultDTO.getStream().stream()
                    .filter(announcementAbstractDTO -> announcementAbstractDTO.getWorkspaceName() == null
                            || announcementAbstractDTO.getWorkspaceName().equals(activeAnnouncementsSearchCriteriaDTO
                                    .getWorkspaceName()))
                    .toList());
        } else {
            pageResult.setStream(announcementPageResultDTO.getStream().stream()
                    .filter(announcementAbstractDTO -> announcementAbstractDTO.getWorkspaceName() == null).toList());
        }

        if (activeAnnouncementsSearchCriteriaDTO.getProductName() != null) {
            pageResult.setStream(pageResult.getStream().stream()
                    .filter(announcementAbstractDTO -> announcementAbstractDTO.getProductName() == null
                            || announcementAbstractDTO.getProductName().equals(activeAnnouncementsSearchCriteriaDTO
                                    .getProductName()))
                    .toList());
        } else {
            pageResult.setStream(pageResult.getStream().stream()
                    .filter(announcementAbstractDTO -> announcementAbstractDTO.getProductName() == null).toList());
        }
        //remove duplicates
        HashSet<AnnouncementAbstractDTO> set = new HashSet<>(pageResult.getStream());
        pageResult.setStream(sort(set.stream().toList()));

        pageResult.setTotalElements((long) pageResult.getStream().size());
        pageResult.setNumber(activeAnnouncementsSearchCriteriaDTO.getPageNumber());
        pageResult.setSize(activeAnnouncementsSearchCriteriaDTO.getPageSize());
        pageResult.setTotalPages(1L);
        if (pageResult.getTotalElements() / pageResult.getSize() > 0) {
            pageResult.setTotalPages(pageResult.getTotalElements() / pageResult.getSize());
        }
        int startIndex = 0;
        int endIndex = Math.toIntExact(pageResult.getTotalElements());
        if (activeAnnouncementsSearchCriteriaDTO.getPageNumber() != 0) {
            startIndex = (activeAnnouncementsSearchCriteriaDTO.getPageSize()
                    * activeAnnouncementsSearchCriteriaDTO.getPageNumber());
            endIndex = startIndex + activeAnnouncementsSearchCriteriaDTO.getPageSize() - 1;
        }
        if (activeAnnouncementsSearchCriteriaDTO.getPageSize() != 100) {
            endIndex = startIndex + activeAnnouncementsSearchCriteriaDTO.getPageSize();
        }
        if (startIndex <= pageResult.getStream().size()) {
            pageResult.setStream(
                    pageResult.getStream().subList(startIndex, Math.min(endIndex, pageResult.getStream().size())));
        } else {
            pageResult.setStream(new ArrayList<>());
        }
        return pageResult;
    }

    @Mapping(target = "productNames", ignore = true)
    ProductItemSearchCriteria map(ProductsSearchCriteriaDTO productsSearchCriteriaDTO);

    @Mapping(target = "removeStreamItem", ignore = true)
    ProductsPageResultDTO map(ProductItemPageResult productItemPageResult);

    @Mapping(target = "removeWorkspaceNamesItem", ignore = true)
    @Mapping(target = "removeProductNamesItem", ignore = true)
    AnnouncementAssignmentsDTO map(AnnouncementProducts announcementProducts);

    AnnouncementBannerSearchCriteria mapAnnouncementBannerSearchCriteria(
            AnnouncementBannerSearchCriteriaDTO announcementBannerSearchCriteriaDTO);
}
