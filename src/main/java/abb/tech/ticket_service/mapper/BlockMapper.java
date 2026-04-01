package abb.tech.ticket_service.mapper;

import abb.tech.ticket_service.dto.request.BlockCreationRequest;
import abb.tech.ticket_service.dto.request.BlockUpdateRequest;
import abb.tech.ticket_service.dto.response.BlockResponse;
import abb.tech.ticket_service.model.Block;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {RowMapper.class})
public interface BlockMapper {

    @Mapping(target = "rows", ignore = true)
    Block toEntity(BlockCreationRequest request);
    BlockResponse toResponse(Block block);
    void updateBlock(@MappingTarget Block block, BlockUpdateRequest request);
}
