package abb.tech.ticket_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ticket Service API")
                        .version("1.0")
                        .description("Ticket Service üçün API sənədləri")
                        .contact(new Contact()
                                .name("ABB Tech")
                                .email("support@abb-tech.az")));
    }
}
