package io.github.jesusblazquez.ledger.api;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class OpenApiConfiguration {

    @Bean
    OpenAPI ledgerApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bank ledger API")
                        .version("v1")
                        .description("""
                                Accounts, deposits, withdrawals and transfers with real domain rules.

                                Commands require an `Idempotency-Key` header: repeating a request with the same
                                key returns the original result instead of moving money twice.
                                """)
                        .license(new License().name("MIT")));
    }
}
