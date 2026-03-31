package abb.tech.ticket_service.service.impl;

import abb.tech.ticket_service.dto.request.SectionCreationRequest;
import abb.tech.ticket_service.dto.request.SectionUpdateRequest;
import abb.tech.ticket_service.mapper.SectionMapper;
import abb.tech.ticket_service.model.Hall;
import abb.tech.ticket_service.model.Section;
import abb.tech.ticket_service.repository.SectionRepository;
import abb.tech.ticket_service.service.BlockService;
import abb.tech.ticket_service.service.SectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SectionServiceImpl implements SectionService {

    private final SectionRepository sectionRepository;
    private final SectionMapper sectionMapper;
    private final BlockService blockService;

    public Section getById(Long id){
        return sectionRepository.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Section with id: %d not found", id)));
    }

    @Override
    public void createSections(List<SectionCreationRequest> requests, Hall hall){
        for(var request: requests){
            var section = sectionMapper.toEntity(request);
            section.setHall(hall);
            sectionRepository.save(section);

            if(request.getBlocks() != null && !request.getBlocks().isEmpty()){
                blockService.createBlocks(request.getBlocks(), section);
            }
        }
    }

    @Override
    public void updateSection(Long id, SectionUpdateRequest request){
        var section = getById(id);
        sectionMapper.updateSection(section, request);
        sectionRepository.save(section);
    }

    @Override
    public void deleteSection(Long id){
        var section = getById(id);
        sectionRepository.delete(section);
    }
}
