package abb.tech.ticket_service.service;

import abb.tech.ticket_service.dto.request.ReqBucketDto;
import abb.tech.ticket_service.dto.response.RespBucketItemDto;
import abb.tech.ticket_service.dto.response.RespBucketDto;
import abb.tech.ticket_service.exception.NotFoundException;
import abb.tech.ticket_service.mapper.BucketMapper;
import abb.tech.ticket_service.model.Bucket;
import abb.tech.ticket_service.model.BucketItem;
import abb.tech.ticket_service.model.EventSession;
import abb.tech.ticket_service.model.Seat;
import abb.tech.ticket_service.repository.BucketItemRepository;
import abb.tech.ticket_service.repository.BucketRepository;
import abb.tech.ticket_service.service.serviceImpl.BucketServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BucketServiceImplTest {

    @Mock private BucketRepository bucketRepository;
    @Mock private BucketItemRepository bucketItemRepository;
    @Mock private EventSessionService eventSessionService;
    @Mock private SeatService seatService;
    @Mock private BucketMapper mapper;

    @InjectMocks
    private BucketServiceImpl service;

    private static final Long USER_ID = 1L;
    private static final Long BUCKET_ID = 10L;
    private static final Long SESSION_ID = 20L;
    private static final Long SEAT_ID = 30L;
    private static final Long ITEM_ID = 40L;

    private Bucket bucket;
    private EventSession eventSession;
    private Seat seat;
    private BucketItem bucketItem;
    private RespBucketItemDto itemResponse;

    @BeforeEach
    void setUp() {
        bucket = new Bucket();
        bucket.setId(BUCKET_ID);
        bucket.setUserId(USER_ID);

        eventSession = new EventSession();
        eventSession.setId(SESSION_ID);

        seat = new Seat();
        seat.setId(SEAT_ID);

        bucketItem = new BucketItem();
        bucketItem.setId(ITEM_ID);
        bucketItem.setBucket(bucket);
        bucketItem.setEventSession(eventSession);
        bucketItem.setSeat(seat);
        bucketItem.setCount(2);

        itemResponse = new RespBucketItemDto(ITEM_ID, BUCKET_ID, SESSION_ID, SEAT_ID, true, 2, null, null);
    }

    @Nested
    @DisplayName("addItem() Testləri")
    class AddItemTests {

        @Test
        @DisplayName("Mövcud bucket-ə yeni item əlavə edilir")
        void addItem_newBucketItem_success() {
            ReqBucketDto request = new ReqBucketDto(USER_ID, SESSION_ID, SEAT_ID, 2);

            when(bucketRepository.findByUserId(USER_ID)).thenReturn(Optional.of(bucket));
            when(eventSessionService.findById(SESSION_ID)).thenReturn(eventSession);
            when(bucketItemRepository.findByBucketIdAndEventSessionIdAndSeatId(any(), any(), any()))
                    .thenReturn(Optional.empty());
            when(seatService.getById(SEAT_ID)).thenReturn(seat);
            when(bucketItemRepository.save(any(BucketItem.class))).thenReturn(bucketItem);
            when(mapper.toItemResponse(any())).thenReturn(itemResponse);

            RespBucketItemDto result = service.addItem(request);

            assertThat(result).isNotNull();
            verify(bucketItemRepository).save(any(BucketItem.class));
            verify(bucketRepository, never()).save(any(Bucket.class));
        }

        @Test
        @DisplayName("Mövcud item tapıldıqda sayı artırılır")
        void addItem_existingItem_incrementsCount() {
            ReqBucketDto request = new ReqBucketDto(USER_ID, SESSION_ID, SEAT_ID, 3);
            int initialCount = bucketItem.getCount(); // 2

            when(bucketRepository.findByUserId(USER_ID)).thenReturn(Optional.of(bucket));
            when(eventSessionService.findById(SESSION_ID)).thenReturn(eventSession);
            when(bucketItemRepository.findByBucketIdAndEventSessionIdAndSeatId(BUCKET_ID, SESSION_ID, SEAT_ID))
                    .thenReturn(Optional.of(bucketItem));
            when(bucketItemRepository.save(any())).thenReturn(bucketItem);
            when(mapper.toItemResponse(any())).thenReturn(itemResponse);

            service.addItem(request);

            assertThat(bucketItem.getCount()).isEqualTo(initialCount + request.count()); // 2 + 3 = 5
            verify(bucketItemRepository).save(bucketItem);
            verifyNoInteractions(seatService); // Mövcud item varsa Seat-ə baxmır
        }

        @Test
        @DisplayName("Bucket yoxdursa yeni bucket yaradır")
        void addItem_noBucket_createsNew() {
            ReqBucketDto request = new ReqBucketDto(USER_ID, SESSION_ID, null, 1);

            when(bucketRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
            when(bucketRepository.save(any(Bucket.class))).thenReturn(bucket);
            when(eventSessionService.findById(SESSION_ID)).thenReturn(eventSession);
            when(bucketItemRepository.save(any())).thenReturn(bucketItem);
            when(mapper.toItemResponse(any())).thenReturn(itemResponse);

            service.addItem(request);

            verify(bucketRepository).save(any(Bucket.class));
        }
    }

    @Nested
    @DisplayName("removeItem() Testləri")
    class RemoveItemTests {

        @Test
        @DisplayName("Item tapıldıqda silinir")
        void removeItem_success() {
            when(bucketItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(bucketItem));

            service.removeItem(ITEM_ID);

            verify(bucketItemRepository).delete(bucketItem);
        }

        @Test
        @DisplayName("Item tapılmadıqda NotFoundException atır")
        void removeItem_notFound_throwsException() {
            when(bucketItemRepository.findById(ITEM_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.removeItem(ITEM_ID))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getBucketByUserId() Testləri")
    class GetBucketByUserIdTests {

        @Test
        @DisplayName("User-in bucket-i və item-ləri qaytarılır")
        void getBucketByUserId_success() {
            when(bucketRepository.findByUserId(USER_ID)).thenReturn(Optional.of(bucket));
            when(bucketItemRepository.findByBucketId(BUCKET_ID)).thenReturn(List.of(bucketItem));
            RespBucketDto expected = new RespBucketDto(BUCKET_ID, USER_ID, List.of(itemResponse), null, null);
            when(mapper.toBucketResponse(bucket, List.of(bucketItem))).thenReturn(expected);

            RespBucketDto result = service.getBucketByUserId(USER_ID);

            assertThat(result.userId()).isEqualTo(USER_ID);
            assertThat(result.items()).hasSize(1);
        }

        @Test
        @DisplayName("Bucket tapılmadıqda NotFoundException")
        void getBucketByUserId_notFound() {
            when(bucketRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getBucketByUserId(USER_ID))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getItemsByUserId() Testləri")
    class GetItemsByUserIdTests {

        @Test
        @DisplayName("Bütün item-lər Resp listi kimi qaytarılır")
        void getItemsByUserId_success() {
            when(bucketRepository.findByUserId(USER_ID)).thenReturn(Optional.of(bucket));
            when(bucketItemRepository.findByBucketId(BUCKET_ID)).thenReturn(List.of(bucketItem));
            when(mapper.toItemResponse(bucketItem)).thenReturn(itemResponse);

            List<RespBucketItemDto> result = service.getItemsByUserId(USER_ID);

            assertThat(result).hasSize(1);
            verify(mapper, times(1)).toItemResponse(any());
        }
    }
}