package abb.tech.ticket_service.service.serviceImpl;

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
import abb.tech.ticket_service.service.BucketService;
import abb.tech.ticket_service.service.EventSessionService;
import abb.tech.ticket_service.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BucketServiceImpl implements BucketService {

    private final BucketRepository bucketRepository;
    private final BucketItemRepository bucketItemRepository;
    private final EventSessionService eventSessionService;
    private final SeatService seatService;
    private final BucketMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Bucket getBucketEntityByUserId(Long userId) {
        return bucketRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(
                        "Bu user üçün bucket tapılmadı, userId: " + userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BucketItem> getSelectedBucketItemsByBucketId(Long bucketId) {
        return bucketItemRepository.findByBucketIdAndSelectedTrue(bucketId);
    }

    @Override
    @Transactional
    public void deleteBucketItem(BucketItem bucketItem) {
        bucketItemRepository.delete(bucketItem);
    }

    /**
     * userId-yə uyğun bucket varsa mövcud bucket-ə, yoxdursa yeni bucket yaradıb
     * həmin bucket-ə BucketItem əlavə edir. selected default true olur.
     */
    @Override
    @Transactional
    public RespBucketItemDto addItem(ReqBucketDto request) {
        Bucket bucket = bucketRepository.findByUserId(request.userId())
                .orElseGet(() -> {
                    Bucket newBucket = new Bucket();
                    newBucket.setUserId(request.userId());
                    return bucketRepository.save(newBucket);
                });

        EventSession eventSession = eventSessionService.findById(request.eventSessionId());

        Optional<BucketItem> existingItem = bucketItemRepository
                .findByBucketIdAndEventSessionIdAndSeatId(
                        bucket.getId(),
                        request.eventSessionId(),
                        request.seatId()
                );

        if (existingItem.isPresent()) {
            BucketItem item = existingItem.get();
            item.setCount(item.getCount() + request.count());
            return mapper.toItemResponse(bucketItemRepository.save(item));
        }

        Seat seat = null;
        if (request.seatId() != null) {
            seat = seatService.getById(request.seatId());
        }

        BucketItem newItem = new BucketItem();
        newItem.setBucket(bucket);
        newItem.setEventSession(eventSession);
        newItem.setSeat(seat);
        newItem.setSelected(true);
        newItem.setCount(request.count());

        return mapper.toItemResponse(bucketItemRepository.save(newItem));
    }

    /**
     * Bucket-dəki konkret BucketItem-i silir.
     */
    @Override
    @Transactional
    public void removeItem(Long bucketItemId) {
        BucketItem item = bucketItemRepository.findById(bucketItemId)
                .orElseThrow(() -> new NotFoundException(
                        "BucketItem tapılmadı, id: " + bucketItemId));
        bucketItemRepository.delete(item);
    }

    /**
     * userId-yə aid bucket və bütün BucketItem-ləri qaytarır.
     */
    @Override
    @Transactional(readOnly = true)
    public RespBucketDto getBucketByUserId(Long userId) {
        Bucket bucket = bucketRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(
                        "Bu user üçün bucket tapılmadı, userId: " + userId));

        List<BucketItem> items = bucketItemRepository.findByBucketId(bucket.getId());
        return mapper.toBucketResponse(bucket, items);
    }

    /**
     * userId-yə aid bucket-in bütün BucketItem-lərini qaytarır.
     */
    @Override
    @Transactional(readOnly = true)
    public List<RespBucketItemDto> getItemsByUserId(Long userId) {
        Bucket bucket = bucketRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(
                        "Bu user üçün bucket tapılmadı, userId: " + userId));

        return bucketItemRepository.findByBucketId(bucket.getId())
                .stream()
                .map(mapper::toItemResponse)
                .toList();
    }
}
