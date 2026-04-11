package abb.tech.ticket_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import abb.tech.ticket_service.service.impl.QrCodeServiceImpl;

import static org.junit.jupiter.api.Assertions.*;

class QrCodeServiceImplTest {

    private QrCodeServiceImpl qrCodeService;

    @BeforeEach
    void setUp() {
        qrCodeService = new QrCodeServiceImpl();
    }

    @Test
    @DisplayName("Uğurlu — QR kod üçün dummy byte array qaytarmalıdır")
    void generateQrCode_success() {
        String text = "Test QR Content";
        int width = 200;
        int height = 200;

        byte[] result = qrCodeService.generateQrCode(text, width, height);

        assertNotNull(result, "Nəticə null olmamalıdır");
        assertTrue(result.length > 0, "Qaytarılan byte array boş olmamalıdır");

        assertEquals((byte) 0x89, result[0]);
        assertEquals((byte) 0x50, result[1]); // 'P'
        assertEquals((byte) 0x4E, result[2]); // 'N'
        assertEquals((byte) 0x47, result[3]); // 'G'
    }

    @Test
    @DisplayName("Uğurlu — Boş string gəldikdə belə dummy array qaytarmalıdır")
    void generateQrCode_withEmptyText() {
        byte[] result = qrCodeService.generateQrCode("", 100, 100);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }
}