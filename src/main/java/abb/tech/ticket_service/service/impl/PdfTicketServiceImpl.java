package abb.tech.ticket_service.service.impl;

import abb.tech.ticket_service.config.KafkaConfig;
import abb.tech.ticket_service.dto.event.TicketCreatedEvent;
import abb.tech.ticket_service.model.Order;
import abb.tech.ticket_service.model.Ticket;
import abb.tech.ticket_service.service.PdfTicketService;
import com.itextpdf.barcodes.BarcodeQRCode;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.layout.renderer.CellRenderer;
import com.itextpdf.layout.renderer.DrawContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PdfTicketServiceImpl implements PdfTicketService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    @Async
    public void generateAndSendTickets(List<Ticket> tickets, String userEmail, Order order) {
        log.info("Generating PDFs and sending Kafka event for order: {} with {} tickets", order.getId(), tickets.size());

        List<TicketCreatedEvent.TicketDetails> ticketDetailsList = tickets.stream()
                .map(ticket -> {
                    byte[] pdf = generateTicketPdf(ticket);
                    return TicketCreatedEvent.TicketDetails.builder()
                            .ticketId(ticket.getId())
                            .ticketNumber(ticket.getTicketNumber().toString())
                            .pdfBase64(Base64.getEncoder().encodeToString(pdf))
                            .build();
                })
                .toList();

        TicketCreatedEvent event = TicketCreatedEvent.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .userEmail(userEmail)
                .tickets(ticketDetailsList)
                .status("ORDER_TICKETS_CREATED")
                .build();

        kafkaTemplate.send(KafkaConfig.TICKET_CREATED_TOPIC, order.getId().toString(), event);
        log.info("TicketCreatedEvent published to Kafka for order: {}", order.getId());
    }

    @Override
    public byte[] generateTicketPdf(Ticket ticket) {
        log.info("Generating PDF for ticket: {}", ticket.getTicketNumber());

        DeviceRgb headerBlue = new DeviceRgb(0, 95, 189);
        DeviceRgb secondaryGray = new DeviceRgb(220, 220, 220);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            PageSize mobileSize = new PageSize(280, 520);
            Document document = new Document(pdf, mobileSize);
            document.setMargins(10, 10, 10, 10);

            float cardWidth = mobileSize.getWidth() * 0.98f;
            Table mainCard = new Table(1).setWidth(cardWidth).setHorizontalAlignment(HorizontalAlignment.CENTER);
            mainCard.setBorder(Border.NO_BORDER);
            mainCard.setBackgroundColor(DeviceRgb.WHITE);

            mainCard.addCell(createHeader(ticket, headerBlue, secondaryGray));
            mainCard.addCell(createInfoSection(ticket, secondaryGray, pdf));
            mainCard.addCell(createFooter(ticket, headerBlue, secondaryGray, pdf));

            document.add(mainCard);
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generating PDF for ticket: {}", ticket.getTicketNumber(), e);
            return ("ERROR_GENERATING_PDF").getBytes();
        }
    }

    private Cell createHeader(Ticket ticket, DeviceRgb headerBlue, DeviceRgb secondaryGray) {
        Cell headerCell = new Cell().setBackgroundColor(headerBlue).setPadding(10).setBorder(Border.NO_BORDER);
        headerCell.setNextRenderer(new CellRenderer(headerCell) {
            @Override
            public void draw(DrawContext drawContext) {
                PdfCanvas canvas = drawContext.getCanvas();
                Rectangle drawRect = getOccupiedAreaBBox();
                canvas.saveState();
                canvas.roundRectangle(drawRect.getLeft(), drawRect.getBottom(), drawRect.getWidth(), drawRect.getHeight() + 15, 15);
                canvas.setFillColor(headerBlue);
                canvas.fill();
                canvas.restoreState();
                super.draw(drawContext);
            }
        });

        Table boardingPassLabel = new Table(1).setWidth(80).setHorizontalAlignment(HorizontalAlignment.CENTER);
        boardingPassLabel.addCell(new Cell().add(new Paragraph("BOARDING PASS").setFontSize(7).setBold().setFontColor(headerBlue))
                .setBackgroundColor(DeviceRgb.WHITE).setTextAlignment(TextAlignment.CENTER).setPadding(1).setBorder(Border.NO_BORDER));
        headerCell.add(boardingPassLabel.setMarginBottom(8));

        Table eventInfo = new Table(UnitValue.createPercentArray(new float[]{65, 35})).useAllAvailableWidth();
        String eventName = ticket.getEventSession().getEvent().getName().toUpperCase();
        String date = ticket.getEventSession().getStartTime() != null ? ticket.getEventSession().getStartTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "N/A";
        String time = ticket.getEventSession().getStartTime() != null ? ticket.getEventSession().getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "N/A";

        eventInfo.addCell(new Cell().add(new Paragraph(eventName).setFontSize(12).setBold().setFontColor(DeviceRgb.WHITE)).add(new Paragraph("EVENT").setFontSize(6).setFontColor(secondaryGray)).setBorder(Border.NO_BORDER));

        eventInfo.addCell(new Cell().add(new Paragraph(date + "\n" + time).setFontSize(10).setBold().setFontColor(DeviceRgb.WHITE)).add(new Paragraph("DATE & TIME").setFontSize(6).setFontColor(secondaryGray)).setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER));
        headerCell.add(eventInfo);
        return headerCell;
    }

    private Cell createInfoSection(Ticket ticket, DeviceRgb secondaryGray, PdfDocument pdf) {
        Cell infoCell = new Cell().setPadding(10).setBorder(Border.NO_BORDER);

        String hallName = ticket.getEventSession().getHall() != null ? ticket.getEventSession().getHall().getName() : "N/A";
        String sectionName = (ticket.getSeat() != null && ticket.getSeat().getRow() != null && ticket.getSeat().getRow().getBlock() != null && ticket.getSeat().getRow().getBlock().getSection() != null) ? ticket.getSeat().getRow().getBlock().getSection().getName() : "N/A";
        String blockName = (ticket.getSeat() != null && ticket.getSeat().getRow() != null && ticket.getSeat().getRow().getBlock() != null) ? ticket.getSeat().getRow().getBlock().getName() : "N/A";
        String rowNum = (ticket.getSeat() != null && ticket.getSeat().getRow() != null) ? ticket.getSeat().getRow().getRowNumber().toString() : "N/A";
        String seatNum = (ticket.getSeat() != null) ? ticket.getSeat().getSeatNumber().toString() : "N/A";
        String ticketNum = ticket.getTicketNumber().toString().substring(0, 8).toUpperCase();

        Table topQrInfo = new Table(UnitValue.createPercentArray(new float[]{33, 34, 33})).useAllAvailableWidth();
        topQrInfo.addCell(new Cell().add(new Paragraph("HALL").setFontSize(6).setFontColor(new DeviceRgb(150, 150, 150))).add(new Paragraph(hallName).setBold().setFontSize(9)).setBorder(Border.NO_BORDER));
        topQrInfo.addCell(new Cell().add(new Paragraph("SECTION").setFontSize(6).setFontColor(new DeviceRgb(150, 150, 150))).add(new Paragraph(sectionName).setBold().setFontSize(9)).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
        topQrInfo.addCell(new Cell().add(new Paragraph("BLOCK").setFontSize(6).setFontColor(new DeviceRgb(150, 150, 150))).add(new Paragraph(blockName).setBold().setFontSize(9)).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));

        topQrInfo.addCell(new Cell().add(new Paragraph("ROW").setFontSize(6).setFontColor(new DeviceRgb(150, 150, 150))).add(new Paragraph(rowNum).setBold().setFontSize(9)).setBorder(Border.NO_BORDER).setMarginTop(5));
        topQrInfo.addCell(new Cell().add(new Paragraph("SEAT").setFontSize(6).setFontColor(new DeviceRgb(150, 150, 150))).add(new Paragraph(seatNum).setBold().setFontSize(9)).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER).setMarginTop(5));
        topQrInfo.addCell(new Cell().add(new Paragraph("TICKET NO").setFontSize(6).setFontColor(new DeviceRgb(150, 150, 150))).add(new Paragraph(ticketNum).setBold().setFontSize(9)).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT).setMarginTop(5));

        infoCell.add(topQrInfo.setMarginBottom(8));
        infoCell.add(createSeparator(secondaryGray));

        infoCell.add(new Paragraph("SHOW YOUR TICKET & QR CODE\nAT THE ENTRANCE").setMarginTop(5).setFontSize(7).setFontColor(new DeviceRgb(120, 120, 120)).setTextAlignment(TextAlignment.CENTER).setMarginBottom(10));

        infoCell.add(createMainQrCode(ticket, pdf));

        return infoCell;
    }

    private Image createMainQrCode(Ticket ticket, PdfDocument pdf) {
        String qrContent = "Ticket: " + ticket.getTicketNumber();
        Image qrImg = createQrCode(qrContent, pdf, 140f, (DeviceRgb) DeviceRgb.BLACK);
        if (qrImg != null) {
            qrImg.setHorizontalAlignment(HorizontalAlignment.CENTER);
        }
        return qrImg;
    }

    private Cell createSeparator(DeviceRgb secondaryGray) {
        Cell separatorCell = new Cell().setPadding(0).setBorder(Border.NO_BORDER).setHeight(20);
        separatorCell.setNextRenderer(new CellRenderer(separatorCell) {
            @Override
            public void draw(DrawContext drawContext) {
                PdfCanvas canvas = drawContext.getCanvas();
                Rectangle drawRect = getOccupiedAreaBBox();
                float middleY = drawRect.getBottom() + drawRect.getHeight() / 2;
                float radius = 8;

                canvas.saveState();
                canvas.setFillColor(new DeviceRgb(255, 255, 255));
                canvas.circle(drawRect.getLeft() - 5, middleY, radius);
                canvas.fill();
                canvas.circle(drawRect.getRight() + 5, middleY, radius);
                canvas.fill();

                canvas.setStrokeColor(secondaryGray);
                canvas.setLineDash(3, 3);
                canvas.setLineWidth(1);
                canvas.moveTo(drawRect.getLeft() + radius - 5, middleY);
                canvas.lineTo(drawRect.getRight() - radius + 5, middleY);
                canvas.stroke();

                canvas.restoreState();
                super.draw(drawContext);
            }
        });
        return separatorCell;
    }

    private Cell createFooter(Ticket ticket, DeviceRgb headerBlue, DeviceRgb secondaryGray, PdfDocument pdf) {
        Cell footerCell = new Cell().setBackgroundColor(headerBlue).setPadding(12).setBorder(Border.NO_BORDER);
        footerCell.setNextRenderer(new CellRenderer(footerCell) {
            @Override
            public void draw(DrawContext drawContext) {
                PdfCanvas canvas = drawContext.getCanvas();
                Rectangle drawRect = getOccupiedAreaBBox();
                canvas.saveState();
                canvas.roundRectangle(drawRect.getLeft(), drawRect.getBottom() - 10, drawRect.getWidth(), drawRect.getHeight() + 10, 15);
                canvas.setFillColor(headerBlue);
                canvas.fill();
                canvas.restoreState();
                super.draw(drawContext);
            }
        });

        Table footerInfo = new Table(UnitValue.createPercentArray(new float[]{75, 25})).useAllAvailableWidth();

        Cell passengerCell = new Cell().add(new Paragraph("PASSENGER").setFontSize(7).setFontColor(secondaryGray))
                .add(new Paragraph("REAL USER").setFontSize(10).setBold().setFontColor(DeviceRgb.WHITE))
                .setBorder(Border.NO_BORDER);
        footerInfo.addCell(passengerCell);

        footerInfo.addCell(createAddressQrCell(ticket, pdf));

        footerCell.add(footerInfo);
        return footerCell;
    }

    private Cell createAddressQrCell(Ticket ticket, PdfDocument pdf) {
        String address = ticket.getEventSession().getHall() != null && ticket.getEventSession().getHall().getVenue() != null
                ? ticket.getEventSession().getHall().getVenue().getAddress() : "Address not available";
        String mapUrl = "https://maps.google.com/?q=" + address.replace(" ", "+");
        Image mapQrImg = createQrCode(mapUrl, pdf, 40f, (DeviceRgb) DeviceRgb.WHITE);

        Cell mapCell = new Cell().setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE);
        if (mapQrImg != null) {
            Table qrWithText = new Table(1).setBorder(Border.NO_BORDER);
            qrWithText.addCell(new Cell().add(mapQrImg.setHorizontalAlignment(HorizontalAlignment.RIGHT)).setBorder(Border.NO_BORDER).setPadding(0));
            qrWithText.addCell(new Cell().add(new Paragraph("SCAN TO OPEN MAP").setFontSize(5).setFontColor(DeviceRgb.WHITE).setBold())
                    .setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT).setPadding(0).setMarginTop(-2));
            mapCell.add(qrWithText.setHorizontalAlignment(HorizontalAlignment.RIGHT));
        }
        return mapCell;
    }

    private Image createQrCode(String content, PdfDocument pdf, float size, DeviceRgb color) {
        try {
            BarcodeQRCode qrCode = new BarcodeQRCode(content);
            PdfFormXObject qrCodeObject = qrCode.createFormXObject(color, pdf);
            return new Image(qrCodeObject).setWidth(size).setHeight(size);
        } catch (Exception e) {
            log.error("Error generating QR code", e);
            return null;
        }
    }

}
