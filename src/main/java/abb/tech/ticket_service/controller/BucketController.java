package abb.tech.ticket_service.controller;

import abb.tech.ticket_service.dto.request.ReqBucketDto;
import abb.tech.ticket_service.dto.response.RespBucketItemDto;
import abb.tech.ticket_service.dto.response.RespBucketDto;
import abb.tech.ticket_service.service.BucketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ticket/buckets")
@RequiredArgsConstructor
public class BucketController {

    private final BucketService bucketService;

    /**
     * GET /api/buckets?userId={userId}
     * userId-yə aid bucket və bütün item-ləri əldə et.
     */
    @GetMapping
    public ResponseEntity<RespBucketDto> getBucket(@RequestParam Long userId) {
        return ResponseEntity.ok(bucketService.getBucketByUserId(userId));
    }

    /**
     * GET /api/buckets/items?userId={userId}
     * userId-yə aid bütün BucketItem-ləri əldə et.
     */
    @GetMapping("/items")
    public ResponseEntity<List<RespBucketItemDto>> getItems(@RequestParam Long userId) {
        return ResponseEntity.ok(bucketService.getItemsByUserId(userId));
    }
    /**
     * POST /api/buckets/items
     * BucketItem əlavə et.
     * userId-yə uyğun bucket yoxdursa avtomatik yaradılır.
     */
    @PostMapping("/items")
    public ResponseEntity<RespBucketItemDto> addItem(
            @Valid @RequestBody ReqBucketDto request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(bucketService.addItem(request));
    }

    /**
     * DELETE /api/buckets/items/{bucketItemId}
     * BucketItem-i sil.
     */
    @DeleteMapping("/items/{bucketItemId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long bucketItemId) {
        bucketService.removeItem(bucketItemId);
        return ResponseEntity.noContent().build();
    }
}
