package abb.tech.ticket_service.service;

import abb.tech.ticket_service.dto.request.ReqBucketDto;
import abb.tech.ticket_service.dto.response.RespBucketItemDto;
import abb.tech.ticket_service.dto.response.RespBucketDto;
import abb.tech.ticket_service.model.Bucket;
import abb.tech.ticket_service.model.BucketItem;

import java.util.List;

public interface BucketService {

    Bucket getBucketEntityByUserId(Long userId);
    List<BucketItem> getSelectedBucketItemsByBucketId(Long bucketId);
    void deleteBucketItem(BucketItem bucketItem);

    /**
     * userId-yə uyğun bucket varsa mövcud bucket-ə, yoxdursa yeni bucket yaradıb
     * həmin bucket-ə BucketItem əlavə edir. selected default true olur.
     */
    RespBucketItemDto addItem(ReqBucketDto request);

    /**
     * Bucket-dəki konkret BucketItem-i silir.
     */
    void removeItem(Long bucketItemId);

    /**
     * userId-yə aid bucket və bütün BucketItem-ləri qaytarır.
     */
    RespBucketDto getBucketByUserId(Long userId);

    /**
     * userId-yə aid bucket-in bütün BucketItem-lərini qaytarır.
     */
    List<RespBucketItemDto> getItemsByUserId(Long userId);
}
