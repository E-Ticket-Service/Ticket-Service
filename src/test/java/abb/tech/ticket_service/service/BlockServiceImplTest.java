package abb.tech.ticket_service.service;

import abb.tech.ticket_service.dto.request.BlockUpdateRequest;
import abb.tech.ticket_service.mapper.BlockMapper;
import abb.tech.ticket_service.model.Block;
import abb.tech.ticket_service.repository.BlockRepository;
import abb.tech.ticket_service.service.impl.BlockServiceImpl;
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
class BlockServiceImplTest {

    @Mock
    private BlockRepository blockRepository;

    @Mock
    private BlockMapper blockMapper;

    @InjectMocks
    private BlockServiceImpl blockService;

    private static final Long BLOCK_ID = 1L;
    private Block block;

    @BeforeEach
    void setUp() {
        block = new Block();
        block.setId(BLOCK_ID);
    }

    @Nested
    @DisplayName("getById Testləri")
    class GetByIdTests {

        @Test
        @DisplayName("Uğurlu — block tapıldıqda obyekti qaytarmalıdır")
        void getById_success() {
            when(blockRepository.findById(BLOCK_ID)).thenReturn(Optional.of(block));

            Block result = blockService.getById(BLOCK_ID);

            assertNotNull(result);
            assertEquals(block, result);
            verify(blockRepository).findById(BLOCK_ID);
        }

        @Test
        @DisplayName("Xəta — block tapılmadıqda 404 ResponseStatusException atmalıdır")
        void getById_notFound() {
            when(blockRepository.findById(BLOCK_ID)).thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> blockService.getById(BLOCK_ID));

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertTrue(exception.getReason().contains("not found"));
        }
    }

    @Nested
    @DisplayName("updateBlock Testləri")
    class UpdateBlockTests {

        @Test
        @DisplayName("Uğurlu — block yenilənməli və yadda saxlanılmalıdır")
        void updateBlock_success() {
            BlockUpdateRequest request = new BlockUpdateRequest();
            when(blockRepository.findById(BLOCK_ID)).thenReturn(Optional.of(block));

            blockService.updateBlock(BLOCK_ID, request);

            verify(blockMapper).updateBlock(block, request);
            verify(blockRepository).save(block);
        }
    }

    @Nested
    @DisplayName("deleteBlock Testləri")
    class DeleteBlockTests {

        @Test
        @DisplayName("Uğurlu — block silinməlidir")
        void deleteBlock_success() {
            when(blockRepository.findById(BLOCK_ID)).thenReturn(Optional.of(block));
            blockService.deleteBlock(BLOCK_ID);
            verify(blockRepository).delete(block);
        }

        @Test
        @DisplayName("Xəta — silinəcək block tapılmadıqda xəta atmalıdır")
        void deleteBlock_notFound() {
            when(blockRepository.findById(BLOCK_ID)).thenReturn(Optional.empty());

            assertThrows(ResponseStatusException.class, () -> blockService.deleteBlock(BLOCK_ID));
            verify(blockRepository, never()).delete(any());
        }
    }
}