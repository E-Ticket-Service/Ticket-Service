package abb.tech.ticket_service.service;

import java.io.ByteArrayOutputStream;

public interface QrCodeService {
    byte[] generateQrCode(String text, int width, int height);
}
