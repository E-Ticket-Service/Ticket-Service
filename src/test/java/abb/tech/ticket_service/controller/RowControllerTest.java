package abb.tech.ticket_service.controller;

import abb.tech.ticket_service.dto.request.RowCreationRequest;
import abb.tech.ticket_service.dto.request.RowUpdateRequest;
import abb.tech.ticket_service.exception.GlobalExceptionHandler;
import abb.tech.ticket_service.exception.ResourceNotFoundException;
import abb.tech.ticket_service.service.RowService;
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
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RowControllerTest {

    @Mock
    private RowService rowService;

    @InjectMocks
    private RowController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private static final Long ROW_ID = 1L;
    private static final Long BLOCK_ID = 100L;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("POST /rows/{blockId}")
    class CreateRowTests {

        @Test
        @DisplayName("201 Created — valid request")
        void createRow_success() throws Exception {
            RowCreationRequest request = new RowCreationRequest(1, List.of());

            doNothing().when(rowService).createRow(any(RowCreationRequest.class), eq(BLOCK_ID));

            mockMvc.perform(post("/rows/{blockId}", BLOCK_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            verify(rowService).createRow(any(RowCreationRequest.class), eq(BLOCK_ID));
        }

        @Test
        @DisplayName("400 Bad Request — validation xətası")
        void createRow_returns400_whenInvalid() throws Exception {
            mockMvc.perform(post("/rows/{blockId}", BLOCK_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /rows/{id}")
    class UpdateRowTests {

        @Test
        @DisplayName("200 OK — uğurlu yeniləmə")
        void updateRow_success() throws Exception {
            RowUpdateRequest request = new RowUpdateRequest();

            doNothing().when(rowService).updateRow(eq(ROW_ID), any());

            mockMvc.perform(put("/rows/{id}", ROW_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("404 Not Found — row tapılmadıqda")
        void updateRow_returns404() throws Exception {
            doThrow(new ResourceNotFoundException("Row tapılmadı"))
                    .when(rowService).updateRow(eq(ROW_ID), any());

            mockMvc.perform(put("/rows/{id}", ROW_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /rows/{id}")
    class DeleteRowTests {

        @Test
        @DisplayName("204 No Content — uğurlu silmə")
        void deleteRow_success() throws Exception {
            doNothing().when(rowService).deleteRow(ROW_ID);

            mockMvc.perform(delete("/rows/{id}", ROW_ID))
                    .andExpect(status().isNoContent());

            verify(rowService).deleteRow(ROW_ID);
        }

        @Test
        @DisplayName("404 Not Found — silinəcək row tapılmadıqda")
        void deleteRow_returns404() throws Exception {
            doThrow(new ResourceNotFoundException("Row tapılmadı"))
                    .when(rowService).deleteRow(ROW_ID);

            mockMvc.perform(delete("/rows/{id}", ROW_ID))
                    .andExpect(status().isNotFound());
        }
    }
}