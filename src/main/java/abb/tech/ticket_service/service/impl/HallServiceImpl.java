package abb.tech.ticket_service.service.impl;

import abb.tech.ticket_service.dto.request.HallCreationRequest;
import abb.tech.ticket_service.dto.request.HallUpdateRequest;
import abb.tech.ticket_service.mapper.HallMapper;
import abb.tech.ticket_service.model.Hall;
import abb.tech.ticket_service.model.Venue;
import abb.tech.ticket_service.repository.HallRepository;
import abb.tech.ticket_service.service.HallService;
import abb.tech.ticket_service.service.SectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HallServiceImpl implements HallService {

    private final HallRepository hallRepository;
    private final HallMapper hallMapper;
    private final SectionService sectionService;

    public Hall getById(Long id){
        return hallRepository.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,String.format("Hall with id: %d not found", id)));
    }

    @Override
    public void createHalls(List<HallCreationRequest> requests, Venue venue) {

        for(var request: requests){

            var hall = hallMapper.toEntity(request);
            hall.setVenue(venue);
            hallRepository.save(hall);

            if(request.getSections() != null && !request.getSections().isEmpty()){
                sectionService.createSections(request.getSections(), hall);
            }
        }
    }

    @Override
    @Transactional
    public void updateHall(Long id, HallUpdateRequest request){
        var hall = getById(id);
        hallMapper.updateHall(hall, request);
        hallRepository.save(hall);
    }

    @Override
    @Transactional
    public void deleteHall(Long id) {
        var hall = getById(id);
        hallRepository.delete(hall);
    }
}
