package abb.tech.ticket_service.controller;

import abb.tech.ticket_service.dto.request.SectionUpdateRequest;
import abb.tech.ticket_service.service.SectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sections")
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;

    @PutMapping("/{id}")
    public void updateSection(@PathVariable Long id, @RequestBody SectionUpdateRequest request){
        sectionService.updateSection(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteSection(@PathVariable Long id){
        sectionService.deleteSection(id);
    }
}
