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
import abb.tech.ticket_service.repository.EventSessionRepository;
import abb.tech.ticket_service.repository.SeatRepository;
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

    @Mock BucketRepository bucketRepository;
    @Mock BucketItemRepository bucketItemRepository;
    @Mock EventSessionRepository eventSessionRepository;
    @Mock SeatRepository seatRepository;
    @Mock BucketMapper mapper;

    @InjectMocks
    BucketServiceImpl service;

    // ─── Fixtures ───────────────────────────────────────────────────────────────

    private static final Long USER_ID       = 1L;
    private static final Long BUCKET_ID     = 10L;
    private static final Long SESSION_ID    = 20L;
    private static final Long SEAT_ID       = 30L;
    private static final Long ITEM_ID       = 40L;

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
        bucketItem.setSelected(true);
        bucketItem.setCount(2);

        itemResponse = new RespBucketItemDto(
                ITEM_ID, BUCKET_ID, SESSION_ID, SEAT_ID, true, 2,
                LocalDateTime.now(), LocalDateTime.now());
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // addItem
    // ─────────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("addItem()")
    class AddItemTests {

        @Test
        @DisplayName("Mövcud bucket varsa yeni bucket yaratmır, item əlavə edir")
        void addItem_existingBucket_addsItem() {
            ReqBucketDto request = new ReqBucketDto(USER_ID, SESSION_ID, SEAT_ID, 2);

            when(bucketRepository.findByUserId(USER_ID)).thenReturn(Optional.of(bucket));
            when(eventSessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(eventSession));
            when(seatRepository.findById(SEAT_ID)).thenReturn(Optional.of(seat));
            when(bucketItemRepository.save(any())).thenReturn(bucketItem);
            when(mapper.toItemResponse(bucketItem)).thenReturn(itemResponse);

            RespBucketItemDto result = service.addItem(request);

            assertThat(result).isEqualTo(itemResponse);
            // Yeni bucket yaradılmamalıdır
            verify(bucketRepository, never()).save(any());
        }

        @Test
        @DisplayName("Bucket yoxdursa yeni bucket yaradılır, sonra item əlavə edilir")
        void addItem_noBucket_createsNewBucketThenAddsItem() {
            ReqBucketDto request = new ReqBucketDto(USER_ID, SESSION_ID, SEAT_ID, 1);

            when(bucketRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
            when(bucketRepository.save(any(Bucket.class))).thenReturn(bucket);
            when(eventSessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(eventSession));
            when(seatRepository.findById(SEAT_ID)).thenReturn(Optional.of(seat));
            when(bucketItemRepository.save(any())).thenReturn(bucketItem);
            when(mapper.toItemResponse(bucketItem)).thenReturn(itemResponse);

            service.addItem(request);

            verify(bucketRepository).save(any(Bucket.class));
            verify(bucketItemRepository).save(any(BucketItem.class));
        }

        @Test
        @DisplayName("Yeni bucket yaradılanda userId düzgün set edilir")
        void addItem_newBucket_setsUserIdCorrectly() {
            ReqBucketDto request = new ReqBucketDto(USER_ID, SESSION_ID, null, 1);

            when(bucketRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
            when(bucketRepository.save(any(Bucket.class))).thenReturn(bucket);
            when(eventSessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(eventSession));
            when(bucketItemRepository.save(any())).thenReturn(bucketItem);
            when(mapper.toItemResponse(any())).thenReturn(itemResponse);

            service.addItem(request);

            ArgumentCaptor<Bucket> bucketCaptor = ArgumentCaptor.forClass(Bucket.class);
            verify(bucketRepository).save(bucketCaptor.capture());
            assertThat(bucketCaptor.getValue().getUserId()).isEqualTo(USER_ID);
        }

        @Test
        @DisplayName("selected field default olaraq true olur")
        void addItem_selectedIsDefaultTrue() {
            ReqBucketDto request = new ReqBucketDto(USER_ID, SESSION_ID, null, 1);

            when(bucketRepository.findByUserId(USER_ID)).thenReturn(Optional.of(bucket));
            when(eventSessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(eventSession));
            when(bucketItemRepository.save(any(BucketItem.class))).thenReturn(bucketItem);
            when(mapper.toItemResponse(any())).thenReturn(itemResponse);

            service.addItem(request);

            ArgumentCaptor<BucketItem> itemCaptor = ArgumentCaptor.forClass(BucketItem.class);
            verify(bucketItemRepository).save(itemCaptor.capture());
            assertThat(itemCaptor.getValue().isSelected()).isTrue();
        }

        @Test
        @DisplayName("seatId null olduqda Seat aranmır, item yenə yaradılır")
        void addItem_noSeatId_skipsSeatLookup() {
            ReqBucketDto request = new ReqBucketDto(USER_ID, SESSION_ID, null, 3);

            when(bucketRepository.findByUserId(USER_ID)).thenReturn(Optional.of(bucket));
            when(eventSessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(eventSession));
            when(bucketItemRepository.save(any())).thenReturn(bucketItem);
            when(mapper.toItemResponse(any())).thenReturn(itemResponse);

            service.addItem(request);

            verifyNoInteractions(seatRepository);
            verify(bucketItemRepository).save(any());
        }

        @Test
        @DisplayName("seatId null olduqda BucketItem.seat null olaraq qalır")
        void addItem_noSeatId_itemSeatIsNull() {
            ReqBucketDto request = new ReqBucketDto(USER_ID, SESSION_ID, null, 1);

            when(bucketRepository.findByUserId(USER_ID)).thenReturn(Optional.of(bucket));
            when(eventSessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(eventSession));
            when(bucketItemRepository.save(any(BucketItem.class))).thenReturn(bucketItem);
            when(mapper.toItemResponse(any())).thenReturn(itemResponse);

            service.addItem(request);

            ArgumentCaptor<BucketItem> captor = ArgumentCaptor.forClass(BucketItem.class);
            verify(bucketItemRepository).save(captor.capture());
            assertThat(captor.getValue().getSeat()).isNull();
        }

        @Test
        @DisplayName("EventSession tapılmadıqda NotFoundException atılır")
        void addItem_eventSessionNotFound_throwsNotFound() {
            ReqBucketDto request = new ReqBucketDto(USER_ID, SESSION_ID, null, 1);

            when(bucketRepository.findByUserId(USER_ID)).thenReturn(Optional.of(bucket));
            when(eventSessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.addItem(request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(String.valueOf(SESSION_ID));

            verify(bucketItemRepository, never()).save(any());
        }

        @Test
        @DisplayName("Seat tapılmadıqda NotFoundException atılır")
        void addItem_seatNotFound_throwsNotFound() {
            ReqBucketDto request = new ReqBucketDto(USER_ID, SESSION_ID, SEAT_ID, 1);

            when(bucketRepository.findByUserId(USER_ID)).thenReturn(Optional.of(bucket));
            when(eventSessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(eventSession));
            when(seatRepository.findById(SEAT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.addItem(request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(String.valueOf(SEAT_ID));

            verify(bucketItemRepository, never()).save(any());
        }

        @Test
        @DisplayName("Item-in count dəyəri request-dən düzgün set edilir")
        void addItem_countSetCorrectly() {
            int expectedCount = 5;
            ReqBucketDto request = new ReqBucketDto(USER_ID, SESSION_ID, null, expectedCount);

            when(bucketRepository.findByUserId(USER_ID)).thenReturn(Optional.of(bucket));
            when(eventSessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(eventSession));
            when(bucketItemRepository.save(any(BucketItem.class))).thenReturn(bucketItem);
            when(mapper.toItemResponse(any())).thenReturn(itemResponse);

            service.addItem(request);

            ArgumentCaptor<BucketItem> captor = ArgumentCaptor.forClass(BucketItem.class);
            verify(bucketItemRepository).save(captor.capture());
            assertThat(captor.getValue().getCount()).isEqualTo(expectedCount);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // removeItem
    // ─────────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("removeItem()")
    class RemoveItemTests {

        @Test
        @DisplayName("Mövcud item uğurla silinir")
        void removeItem_success() {
            when(bucketItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(bucketItem));

            service.removeItem(ITEM_ID);

            verify(bucketItemRepository).delete(bucketItem);
        }

        @Test
        @DisplayName("Item tapılmadıqda NotFoundException atılır, delete çağırılmır")
        void removeItem_notFound_throwsNotFoundException() {
            when(bucketItemRepository.findById(ITEM_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.removeItem(ITEM_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(String.valueOf(ITEM_ID));

            verify(bucketItemRepository, never()).delete(any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // getBucketByUserId
    // ─────────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getBucketByUserId()")
    class GetBucketByUserIdTests {

        @Test
        @DisplayName("userId-yə uyğun bucket və item-lər qaytarılır")
        void getBucketByUserId_success() {
            RespBucketDto expected = new RespBucketDto(
                    BUCKET_ID, USER_ID, List.of(itemResponse), null, null);

            when(bucketRepository.findByUserId(USER_ID)).thenReturn(Optional.of(bucket));
            when(bucketItemRepository.findByBucketId(BUCKET_ID)).thenReturn(List.of(bucketItem));
            when(mapper.toBucketResponse(bucket, List.of(bucketItem))).thenReturn(expected);

            RespBucketDto result = service.getBucketByUserId(USER_ID);

            assertThat(result).isEqualTo(expected);
            assertThat(result.userId()).isEqualTo(USER_ID);
        }

        @Test
        @DisplayName("Bucket tapılmadıqda NotFoundException atılır")
        void getBucketByUserId_notFound() {
            when(bucketRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getBucketByUserId(USER_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(String.valueOf(USER_ID));

            verifyNoInteractions(bucketItemRepository);
        }

        @Test
        @DisplayName("Bucket boş olduqda boş item list qaytarılır")
        void getBucketByUserId_emptyItems() {
            RespBucketDto emptyBucket = new RespBucketDto(
                    BUCKET_ID, USER_ID, List.of(), null, null);

            when(bucketRepository.findByUserId(USER_ID)).thenReturn(Optional.of(bucket));
            when(bucketItemRepository.findByBucketId(BUCKET_ID)).thenReturn(List.of());
            when(mapper.toBucketResponse(bucket, List.of())).thenReturn(emptyBucket);

            RespBucketDto result = service.getBucketByUserId(USER_ID);

            assertThat(result.items()).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // getItemsByUserId
    // ─────────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getItemsByUserId()")
    class GetItemsByUserIdTests {

        @Test
        @DisplayName("userId-yə uyğun bütün item-lər qaytarılır")
        void getItemsByUserId_success() {
            when(bucketRepository.findByUserId(USER_ID)).thenReturn(Optional.of(bucket));
            when(bucketItemRepository.findByBucketId(BUCKET_ID)).thenReturn(List.of(bucketItem));
            when(mapper.toItemResponse(bucketItem)).thenReturn(itemResponse);

            List<RespBucketItemDto> result = service.getItemsByUserId(USER_ID);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst()).isEqualTo(itemResponse);
        }

        @Test
        @DisplayName("Bucket tapılmadıqda NotFoundException atılır")
        void getItemsByUserId_bucketNotFound() {
            when(bucketRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getItemsByUserId(USER_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(String.valueOf(USER_ID));
        }

        @Test
        @DisplayName("Bucket boşdursa boş list qaytarılır")
        void getItemsByUserId_emptyBucket() {
            when(bucketRepository.findByUserId(USER_ID)).thenReturn(Optional.of(bucket));
            when(bucketItemRepository.findByBucketId(BUCKET_ID)).thenReturn(List.of());

            List<RespBucketItemDto> result = service.getItemsByUserId(USER_ID);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Bir neçə item olduqda hamısı qaytarılır")
        void getItemsByUserId_multipleItems() {
            BucketItem item2 = new BucketItem();
            item2.setId(41L);
            item2.setBucket(bucket);
            RespBucketItemDto resp2 = new RespBucketItemDto(
                    41L, BUCKET_ID, SESSION_ID, null, true, 1, null, null);

            when(bucketRepository.findByUserId(USER_ID)).thenReturn(Optional.of(bucket));
            when(bucketItemRepository.findByBucketId(BUCKET_ID)).thenReturn(List.of(bucketItem, item2));
            when(mapper.toItemResponse(bucketItem)).thenReturn(itemResponse);
            when(mapper.toItemResponse(item2)).thenReturn(resp2);

            List<RespBucketItemDto> result = service.getItemsByUserId(USER_ID);

            assertThat(result).hasSize(2);
        }
    }
}
