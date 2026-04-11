package abb.tech.ticket_service.service;

import abb.tech.ticket_service.dto.request.RowCreationRequest;
import abb.tech.ticket_service.dto.request.RowUpdateRequest;
import abb.tech.ticket_service.mapper.RowMapper;
import abb.tech.ticket_service.model.Block;
import abb.tech.ticket_service.model.Row;
import abb.tech.ticket_service.model.Seat;
import abb.tech.ticket_service.repository.RowRepository;
import abb.tech.ticket_service.service.impl.RowServiceImpl;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RowServiceImplTest {

    @Mock private RowRepository rowRepository;
    @Mock private RowMapper rowMapper;
    @Mock private BlockService blockService;

    @InjectMocks
    private RowServiceImpl rowService;

    private Row row;
    private static final Long ROW_ID = 1L;
    private static final Long BLOCK_ID = 10L;

    @BeforeEach
    void setUp() {
        row = new Row();
        row.setId(ROW_ID);
        row.setSeats(new ArrayList<>());
    }

    @Nested
    @DisplayName("getById Testləri")
    class GetByIdTests {

        @Test
        @DisplayName("Uğurlu — Row tapılmalıdır")
        void getById_success() {
            when(rowRepository.findById(ROW_ID)).thenReturn(Optional.of(row));

            Row result = rowService.getById(ROW_ID);

            assertNotNull(result);
            assertEquals(ROW_ID, result.getId());
        }

        @Test
        @DisplayName("Xəta — Row tapılmadıqda NOT_FOUND (404) atmalıdır")
        void getById_notFound() {
            when(rowRepository.findById(ROW_ID)).thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> rowService.getById(ROW_ID));

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertTrue(exception.getReason().contains("not found"));
        }
    }

    @Nested
    @DisplayName("createRow Testləri")
    class CreateTests {

        @Test
        @DisplayName("Uğurlu — Row yaradılmalı və oturacaqlar (seats) row-a bağlanmalıdır")
        void createRow_success() {
            RowCreationRequest request = new RowCreationRequest();
            Block block = new Block();
            block.setId(BLOCK_ID);

            Seat seat1 = new Seat();
            Seat seat2 = new Seat();
            row.setSeats(List.of(seat1, seat2));

            when(blockService.getById(BLOCK_ID)).thenReturn(block);
            when(rowMapper.toEntity(request)).thenReturn(row);

            rowService.createRow(request, BLOCK_ID);

            assertEquals(block, row.getBlock());
            assertEquals(row, seat1.getRow());
            assertEquals(row, seat2.getRow());
            verify(rowRepository).save(row);
        }
    }

    @Nested
    @DisplayName("updateRow Testləri")
    class UpdateTests {

        @Test
        @DisplayName("Uğurlu — Row update edilməli və save olunmalıdır")
        void updateRow_success() {
            RowUpdateRequest request = new RowUpdateRequest();
            when(rowRepository.findById(ROW_ID)).thenReturn(Optional.of(row));

            rowService.updateRow(ROW_ID, request);

            verify(rowMapper).updateRow(row, request);
            verify(rowRepository).save(row);
        }
    }

    @Nested
    @DisplayName("deleteRow Testləri")
    class DeleteTests {

        @Test
        @DisplayName("Uğurlu — Row tapılıb silinməlidir")
        void deleteRow_success() {
            when(rowRepository.findById(ROW_ID)).thenReturn(Optional.of(row));

            rowService.deleteRow(ROW_ID);

            verify(rowRepository).delete(row);
        }
    }
}