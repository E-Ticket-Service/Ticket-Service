package abb.tech.ticket_service.controller;

import abb.tech.ticket_service.dto.event.PaymentFailedEvent;
import abb.tech.ticket_service.dto.event.PaymentSuccessEvent;
import abb.tech.ticket_service.dto.request.OrderCreationRequest;
import abb.tech.ticket_service.dto.response.OrderResponse;
import abb.tech.ticket_service.exception.GlobalExceptionHandler;
import abb.tech.ticket_service.exception.ResourceNotFoundException;
import abb.tech.ticket_service.service.OrderService;
import abb.tech.ticket_service.service.PaymentEventHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock private OrderService orderService;
    @Mock private PaymentEventHandler paymentEventHandler;
    @Mock private KafkaTemplate<String, String> kafkaTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private OrderController controller;

    private MockMvc mockMvc;

    private static final Long ORDER_ID = 1L;
    private static final Long USER_ID = 100L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("Order Yaradılması Testləri")
    class CreateOrderTests {

        @Test
        @DisplayName("POST /orders — Uğurlu yaradılma")
        void createOrder_success() throws Exception {
            OrderCreationRequest request = mock(OrderCreationRequest.class);
            OrderResponse response = mock(OrderResponse.class);
            when(response.getId()).thenReturn(ORDER_ID);

            when(orderService.createOrder(any())).thenReturn(response);

            mockMvc.perform(post("/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(ORDER_ID));
        }

        @Test
        @DisplayName("POST /orders/bucket/{userId} — Səbətdən sifariş")
        void createOrderFromBucket_success() throws Exception {
            OrderResponse response = mock(OrderResponse.class);
            when(orderService.createOrderFromBucket(USER_ID)).thenReturn(response);

            mockMvc.perform(post("/orders/bucket/{userId}", USER_ID))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Sifariş Əldəetmə və Silmə")
    class GetAndDeleteTests {

        @Test
        @DisplayName("GET /orders/{id} — Uğurlu")
        void getOrderById_success() throws Exception {
            OrderResponse response = mock(OrderResponse.class);
            when(orderService.getOrderById(ORDER_ID)).thenReturn(response);

            mockMvc.perform(get("/orders/{id}", ORDER_ID))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("DELETE /orders/{id} — Sifariş ləğvi")
        void cancelOrder_success() throws Exception {
            doNothing().when(orderService).cancelOrder(ORDER_ID);

            mockMvc.perform(delete("/orders/{id}", ORDER_ID))
                    .andExpect(status().isNoContent());

            verify(orderService).cancelOrder(ORDER_ID);
        }
    }

    @Nested
    @DisplayName("Kafka Test Endpointləri")
    class KafkaTestEndpoints {

        @Test
        @DisplayName("POST /test/payment-success — Kafka-ya mesaj göndərilməlidir")
        void testPaymentSuccess_success() throws Exception {
            PaymentSuccessEvent event = mock(PaymentSuccessEvent.class);

            mockMvc.perform(post("/orders/test/payment-success")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(event)))
                    .andExpect(status().isAccepted());

            verify(kafkaTemplate).send(any(), any());
        }

        @Test
        @DisplayName("POST /test/payment-failed — Kafka-ya mesaj göndərilməlidir")
        void testPaymentFailed_success() throws Exception {
            PaymentFailedEvent event = mock(PaymentFailedEvent.class);

            mockMvc.perform(post("/orders/test/payment-failed")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(event)))
                    .andExpect(status().isAccepted());

            verify(kafkaTemplate).send(any(), any());
        }
    }
}