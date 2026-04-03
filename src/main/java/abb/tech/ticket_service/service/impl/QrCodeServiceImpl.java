package abb.tech.ticket_service.service.impl;

import abb.tech.ticket_service.service.QrCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
@Slf4j
public class QrCodeServiceImpl implements QrCodeService {

    @Override
    public byte[] generateQrCode(String text, int width, int height) {
        log.info("Generating QR code for: {}", text);
        // Real implementasiya iText7-nin BarcodeQRCode-u ilə birbaşa PdfTicketServiceImpl daxilində edilir.
        // Bu metod hələlik geriyə uyğunluq üçün dummy PNG byte array-i qaytarır.
        try {
            return Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==");
        } catch (Exception e) {
            log.error("Error generating dummy QR code", e);
            return new byte[0];
        }
    }
}
