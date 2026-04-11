package abb.tech.ticket_service.controller;

import abb.tech.ticket_service.dto.request.BlockUpdateRequest;
import abb.tech.ticket_service.exception.GlobalExceptionHandler;
import abb.tech.ticket_service.exception.ResourceNotFoundException;
import abb.tech.ticket_service.service.BlockService;
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
class BlockControllerTest {

    @Mock
    private BlockService blockService;

    @InjectMocks
    private BlockController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private static final Long BLOCK_ID = 1L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("PUT /blocks/{id}")
    class UpdateBlockTests {

        @Test
        @DisplayName("200 OK — uğurlu yeniləmə")
        void updateBlock_returns200() throws Exception {
            BlockUpdateRequest request = new BlockUpdateRequest();

            doNothing().when(blockService).updateBlock(eq(BLOCK_ID), any(BlockUpdateRequest.class));

            mockMvc.perform(put("/blocks/{id}", BLOCK_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(blockService).updateBlock(eq(BLOCK_ID), any(BlockUpdateRequest.class));
        }

        @Test
        @DisplayName("404 Not Found — block tapılmadıqda")
        void updateBlock_returns404() throws Exception {
            doThrow(new ResourceNotFoundException("Block tapılmadı"))
                    .when(blockService).updateBlock(eq(BLOCK_ID), any());

            mockMvc.perform(put("/blocks/{id}", BLOCK_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new BlockUpdateRequest())))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /blocks/{id}")
    class DeleteBlockTests {

        @Test
        @DisplayName("204 No Content — uğurlu silmə")
        void deleteBlock_returns204() throws Exception {
            doNothing().when(blockService).deleteBlock(BLOCK_ID);

            mockMvc.perform(delete("/blocks/{id}", BLOCK_ID))
                    .andExpect(status().isNoContent());

            verify(blockService).deleteBlock(BLOCK_ID);
        }

        @Test
        @DisplayName("404 Not Found — silinəcək block tapılmadıqda")
        void deleteBlock_returns404() throws Exception {
            doThrow(new ResourceNotFoundException("Block tapılmadı"))
                    .when(blockService).deleteBlock(BLOCK_ID);

            mockMvc.perform(delete("/blocks/{id}", BLOCK_ID))
                    .andExpect(status().isNotFound());
        }
    }
}