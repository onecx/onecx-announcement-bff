package io.github.onecx.announcement.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.io.github.onecx.announcement.bff.clients.model.ProblemDetailResponse;
import gen.io.github.onecx.announcement.bff.rs.internal.model.ProblemDetailResponseDTO;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface ProblemDetailMapper {
    @Mapping(target = "removeParamsItem", ignore = true)
    @Mapping(target = "removeInvalidParamsItem", ignore = true)
    ProblemDetailResponseDTO mapProblemDetailResponse(ProblemDetailResponse problemDetailResponse);
}
