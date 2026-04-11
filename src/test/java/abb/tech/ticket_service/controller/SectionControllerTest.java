package abb.tech.ticket_service.controller;

import abb.tech.ticket_service.dto.request.SectionUpdateRequest;
import abb.tech.ticket_service.exception.GlobalExceptionHandler;
import abb.tech.ticket_service.exception.ResourceNotFoundException;
import abb.tech.ticket_service.service.SectionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SectionControllerTest {

    @Mock
    private SectionService sectionService;

    @InjectMocks
    private SectionController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private static final Long SECTION_ID = 1L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("PUT /sections/{id}")
    class UpdateSectionTests {

        @Test
        @DisplayName("200 OK — uğurlu yeniləmə")
        void updateSection_success() throws Exception {
            SectionUpdateRequest request = new SectionUpdateRequest();
            doNothing().when(sectionService).updateSection(eq(SECTION_ID), any(SectionUpdateRequest.class));

            mockMvc.perform(put("/sections/{id}", SECTION_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(sectionService).updateSection(eq(SECTION_ID), any(SectionUpdateRequest.class));
        }

        @Test
        @DisplayName("404 Not Found — bölmə tapılmadıqda")
        void updateSection_returns404() throws Exception {
            doThrow(new ResourceNotFoundException("Section tapılmadı"))
                    .when(sectionService).updateSection(eq(SECTION_ID), any());

            mockMvc.perform(put("/sections/{id}", SECTION_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /sections/{id}")
    class DeleteSectionTests {

        @Test
        @DisplayName("204 No Content — uğurlu silmə")
        void deleteSection_success() throws Exception {
            doNothing().when(sectionService).deleteSection(SECTION_ID);

            mockMvc.perform(delete("/sections/{id}", SECTION_ID))
                    .andExpect(status().isNoContent());

            verify(sectionService).deleteSection(SECTION_ID);
        }

        @Test
        @DisplayName("404 Not Found — silinəcək bölmə tapılmadıqda")
        void deleteSection_returns404() throws Exception {
            doThrow(new ResourceNotFoundException("Section tapılmadı"))
                    .when(sectionService).deleteSection(SECTION_ID);

            mockMvc.perform(delete("/sections/{id}", SECTION_ID))
                    .andExpect(status().isNotFound());
        }
    }
}