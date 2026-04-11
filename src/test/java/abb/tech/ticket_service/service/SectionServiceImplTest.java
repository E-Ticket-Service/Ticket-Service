package abb.tech.ticket_service.service;

import abb.tech.ticket_service.dto.request.SectionUpdateRequest;
import abb.tech.ticket_service.mapper.SectionMapper;
import abb.tech.ticket_service.model.Section;
import abb.tech.ticket_service.repository.SectionRepository;
import abb.tech.ticket_service.service.impl.SectionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SectionServiceImplTest {

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private SectionMapper sectionMapper;

    @InjectMocks
    private SectionServiceImpl sectionService;

    private Section section;
    private static final Long SECTION_ID = 1L;

    @BeforeEach
    void setUp() {
        section = new Section();
        section.setId(SECTION_ID);
        section.setName("VIP Section");
    }

    @Nested
    @DisplayName("getById Testləri")
    class GetByIdTests {

        @Test
        @DisplayName("Uğurlu — Sektor tapılmalıdır")
        void getById_success() {
            when(sectionRepository.findById(SECTION_ID)).thenReturn(Optional.of(section));

            Section result = sectionService.getById(SECTION_ID);

            assertNotNull(result);
            assertEquals(SECTION_ID, result.getId());
            assertEquals("VIP Section", result.getName());
            verify(sectionRepository).findById(SECTION_ID);
        }

        @Test
        @DisplayName("Xəta — Sektor tapılmadıqda 404 xətası atmalıdır")
        void getById_notFound() {
            when(sectionRepository.findById(SECTION_ID)).thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> sectionService.getById(SECTION_ID));

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertTrue(exception.getReason().contains("not found"));
        }
    }

    @Nested
    @DisplayName("Update və Delete Testləri")
    class ModifyTests {

        @Test
        @DisplayName("Uğurlu — Sektor məlumatları yenilənməlidir")
        void updateSection_success() {
            SectionUpdateRequest request = new SectionUpdateRequest();
            when(sectionRepository.findById(SECTION_ID)).thenReturn(Optional.of(section));

            sectionService.updateSection(SECTION_ID, request);

            verify(sectionMapper).updateSection(section, request);
            verify(sectionRepository).save(section);
        }

        @Test
        @DisplayName("Uğurlu — Sektor sistemdən silinməlidir")
        void deleteSection_success() {
            when(sectionRepository.findById(SECTION_ID)).thenReturn(Optional.of(section));

            sectionService.deleteSection(SECTION_ID);

            verify(sectionRepository).delete(section);
        }
    }
}