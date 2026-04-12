package abb.tech.ticket_service.constant;

public final class KafkaConstants {

    public static final String TICKET_CREATED_TOPIC = "ticket-created-topic";
    public static final String ORDER_CREATED_TOPIC = "order-created-topic";
    public static final String PAYMENT_SUCCESS_TOPIC = "payment-success-topic";
    public static final String PAYMENT_FAILED_TOPIC = "payment-failed-topic";
    public static final String REFUND_REQUEST_TOPIC = "refund-request-topic";
    public static final String REFUND_RESULT_TOPIC = "refund-result-topic";
    public static final String TICKET_SERVICE_GROUP = "ticket-service-group";
}
