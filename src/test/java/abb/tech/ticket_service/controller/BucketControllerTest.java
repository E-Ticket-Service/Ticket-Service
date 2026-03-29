package abb.tech.ticket_service.controller;

import abb.tech.ticket_service.dto.request.ReqBucketDto;
import abb.tech.ticket_service.dto.response.RespBucketItemDto;
import abb.tech.ticket_service.dto.response.RespBucketDto;
import abb.tech.ticket_service.exception.GlobalExceptionHandler;
import abb.tech.ticket_service.exception.NotFoundException;
import abb.tech.ticket_service.service.BucketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BucketControllerTest {

    @Mock
    BucketService bucketService;

    @InjectMocks
    BucketController controller;

    MockMvc mockMvc;
    ObjectMapper objectMapper;

    private static final Long USER_ID   = 1L;
    private static final Long BUCKET_ID = 10L;
    private static final Long SESSION_ID = 20L;
    private static final Long SEAT_ID   = 30L;
    private static final Long ITEM_ID   = 40L;

    RespBucketItemDto itemResponse;
    RespBucketDto bucketResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        itemResponse = new RespBucketItemDto(
                ITEM_ID, BUCKET_ID, SESSION_ID, SEAT_ID, true, 2,
                LocalDateTime.of(2025, 6, 1, 10, 0),
                LocalDateTime.of(2025, 6, 1, 10, 0));

        bucketResponse = new RespBucketDto(
                BUCKET_ID, USER_ID, List.of(itemResponse),
                LocalDateTime.of(2025, 6, 1, 9, 0),
                LocalDateTime.of(2025, 6, 1, 10, 0));
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // POST /api/ticket/buckets/items
    // ─────────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/ticket/buckets/items")
    class AddItemEndpoint {

        @Test
        @DisplayName("201 Created — valid request, mövcud bucket varsa")
        void addItem_returns201() throws Exception {
            ReqBucketDto req = new ReqBucketDto(USER_ID, SESSION_ID, SEAT_ID, 2);
            when(bucketService.addItem(any())).thenReturn(itemResponse);

            mockMvc.perform(post("/api/ticket/buckets/items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(ITEM_ID))
                    .andExpect(jsonPath("$.bucketId").value(BUCKET_ID))
                    .andExpect(jsonPath("$.selected").value(true))
                    .andExpect(jsonPath("$.count").value(2));
        }

        @Test
        @DisplayName("201 Created — seatId null olduqda da işləyir")
        void addItem_withoutSeat_returns201() throws Exception {
            ReqBucketDto req = new ReqBucketDto(USER_ID, SESSION_ID, null, 1);
            RespBucketItemDto noSeatResp = new RespBucketItemDto(
                    ITEM_ID, BUCKET_ID, SESSION_ID, null, true, 1, null, null);
            when(bucketService.addItem(any())).thenReturn(noSeatResp);

            mockMvc.perform(post("/api/ticket/buckets/items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.seatId").isEmpty());
        }

        @Test
        @DisplayName("404 Not Found — EventSession tapılmadı")
        void addItem_eventSessionNotFound_returns404() throws Exception {
            ReqBucketDto req = new ReqBucketDto(USER_ID, SESSION_ID, null, 1);
            when(bucketService.addItem(any()))
                    .thenThrow(new NotFoundException("EventSession tapılmadı, id: " + SESSION_ID));

            mockMvc.perform(post("/api/ticket/buckets/items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("400 Bad Request — userId null")
        void addItem_nullUserId_returns400() throws Exception {
            ReqBucketDto req = new ReqBucketDto(null, SESSION_ID, null, 1);

            mockMvc.perform(post("/api/ticket/buckets/items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 Bad Request — eventSessionId null")
        void addItem_nullSessionId_returns400() throws Exception {
            ReqBucketDto req = new ReqBucketDto(USER_ID, null, null, 1);

            mockMvc.perform(post("/api/ticket/buckets/items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 Bad Request — count sıfırdır")
        void addItem_zeroCount_returns400() throws Exception {
            ReqBucketDto req = new ReqBucketDto(USER_ID, SESSION_ID, null, 0);

            mockMvc.perform(post("/api/ticket/buckets/items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 Bad Request — count null")
        void addItem_nullCount_returns400() throws Exception {
            ReqBucketDto req = new ReqBucketDto(USER_ID, SESSION_ID, null, null);

            mockMvc.perform(post("/api/ticket/buckets/items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // DELETE /api/ticket/buckets/items/{bucketItemId}
    // ─────────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("DELETE /api/ticket/buckets/items/{bucketItemId}")
    class RemoveItemEndpoint {

        @Test
        @DisplayName("204 No Content — uğurlu silmə")
        void removeItem_returns204() throws Exception {
            doNothing().when(bucketService).removeItem(ITEM_ID);

            mockMvc.perform(delete("/api/ticket/buckets/items/{id}", ITEM_ID))
                    .andExpect(status().isNoContent());

            verify(bucketService).removeItem(ITEM_ID);
        }

        @Test
        @DisplayName("404 Not Found — item tapılmadı")
        void removeItem_notFound_returns404() throws Exception {
            doThrow(new NotFoundException("BucketItem tapılmadı, id: " + ITEM_ID))
                    .when(bucketService).removeItem(ITEM_ID);

            mockMvc.perform(delete("/api/ticket/buckets/items/{id}", ITEM_ID))
                    .andExpect(status().isNotFound());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // GET /api/ticket/buckets?userId=
    // ─────────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/ticket/buckets")
    class GetBucketEndpoint {

        @Test
        @DisplayName("200 OK — bucket və item-lər qaytarılır")
        void getBucket_returns200() throws Exception {
            when(bucketService.getBucketByUserId(USER_ID)).thenReturn(bucketResponse);

            mockMvc.perform(get("/api/ticket/buckets").param("userId", String.valueOf(USER_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(BUCKET_ID))
                    .andExpect(jsonPath("$.userId").value(USER_ID))
                    .andExpect(jsonPath("$.items.length()").value(1))
                    .andExpect(jsonPath("$.items[0].selected").value(true));
        }

        @Test
        @DisplayName("404 Not Found — user üçün bucket yoxdur")
        void getBucket_notFound_returns404() throws Exception {
            when(bucketService.getBucketByUserId(USER_ID))
                    .thenThrow(new NotFoundException("Bu user üçün bucket tapılmadı, userId: " + USER_ID));

            mockMvc.perform(get("/api/ticket/buckets").param("userId", String.valueOf(USER_ID)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("200 OK — boş bucket (items boşdur)")
        void getBucket_emptyItems_returns200() throws Exception {
            RespBucketDto emptyBucket = new RespBucketDto(BUCKET_ID, USER_ID, List.of(), null, null);
            when(bucketService.getBucketByUserId(USER_ID)).thenReturn(emptyBucket);

            mockMvc.perform(get("/api/ticket/buckets").param("userId", String.valueOf(USER_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items.length()").value(0));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // GET /api/ticket/buckets/items?userId=
    // ─────────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/ticket/buckets/items")
    class GetItemsEndpoint {

        @Test
        @DisplayName("200 OK — bütün item-lər qaytarılır")
        void getItems_returns200() throws Exception {
            when(bucketService.getItemsByUserId(USER_ID)).thenReturn(List.of(itemResponse));

            mockMvc.perform(get("/api/ticket/buckets/items").param("userId", String.valueOf(USER_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(ITEM_ID))
                    .andExpect(jsonPath("$[0].selected").value(true));
        }

        @Test
        @DisplayName("200 OK — boş list qaytarılır")
        void getItems_emptyList_returns200() throws Exception {
            when(bucketService.getItemsByUserId(USER_ID)).thenReturn(List.of());

            mockMvc.perform(get("/api/ticket/buckets/items").param("userId", String.valueOf(USER_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("404 Not Found — user üçün bucket yoxdur")
        void getItems_bucketNotFound_returns404() throws Exception {
            when(bucketService.getItemsByUserId(USER_ID))
                    .thenThrow(new NotFoundException("Bu user üçün bucket tapılmadı, userId: " + USER_ID));

            mockMvc.perform(get("/api/ticket/buckets/items").param("userId", String.valueOf(USER_ID)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("200 OK — bir neçə item qaytarılır")
        void getItems_multipleItems_returns200() throws Exception {
            RespBucketItemDto resp2 = new RespBucketItemDto(
                    41L, BUCKET_ID, SESSION_ID, null, true, 3, null, null);
            when(bucketService.getItemsByUserId(USER_ID)).thenReturn(List.of(itemResponse, resp2));

            mockMvc.perform(get("/api/ticket/buckets/items").param("userId", String.valueOf(USER_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }
    }
}