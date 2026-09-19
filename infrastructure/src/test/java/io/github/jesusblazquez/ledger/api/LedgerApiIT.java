package io.github.jesusblazquez.ledger.api;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.jesusblazquez.ledger.TestcontainersConfiguration;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;

/**
 * End-to-end tests over real HTTP against a real PostgreSQL: the same path a client would take.
 *
 * <p>These cover what unit tests cannot: serialization, validation, status codes and the error
 * format. The business rules themselves are already tested where they live, in the domain.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class LedgerApiIT {

    @Value("${local.server.port}")
    private int port;

    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void opensAnAccountWithAValidIbanAndZeroBalance() throws Exception {
        HttpResponse<String> response = post("/api/accounts", """
                {"holderName": "Jesús Blázquez", "dailyTransferLimit": {"amount": "1000.00", "currency": "EUR"}}
                """, null);

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.headers().firstValue("Location")).isPresent();
        assertThat(json(response, "iban")).startsWith("ES");
        assertThat(json(response, "status")).isEqualTo("OPEN");
        assertThat(response.body()).contains("\"amount\":\"0.00\"");
    }

    @Test
    void movesMoneyAndRecordsItInTheHistory() throws Exception {
        String accountId = openAccount("Jesús Blázquez");

        HttpResponse<String> deposit = post("/api/accounts/" + accountId + "/deposits", """
                {"amount": {"amount": "500.00", "currency": "EUR"}}
                """, UUID.randomUUID());
        assertThat(deposit.statusCode()).isEqualTo(200);
        assertThat(deposit.body()).contains("\"amount\":\"500.00\"");

        HttpResponse<String> withdrawal = post("/api/accounts/" + accountId + "/withdrawals", """
                {"amount": {"amount": "120.50", "currency": "EUR"}}
                """, UUID.randomUUID());
        assertThat(withdrawal.statusCode()).isEqualTo(200);
        assertThat(withdrawal.body()).contains("\"amount\":\"379.50\"");

        HttpResponse<String> history = get("/api/accounts/" + accountId + "/transactions");
        assertThat(history.statusCode()).isEqualTo(200);
        assertThat(history.body()).contains("WITHDRAWAL").contains("DEPOSIT");
    }

    @Test
    void retryingARequestWithTheSameKeyDoesNotMoveMoneyTwice() throws Exception {
        String accountId = openAccount("Ada Lovelace");
        UUID idempotencyKey = UUID.randomUUID();
        String body = """
                {"amount": {"amount": "200.00", "currency": "EUR"}}
                """;

        post("/api/accounts/" + accountId + "/deposits", body, idempotencyKey);
        HttpResponse<String> retry = post("/api/accounts/" + accountId + "/deposits", body, idempotencyKey);

        assertThat(retry.statusCode()).isEqualTo(200);
        assertThat(retry.body()).contains("\"replayed\":true");
        assertThat(get("/api/accounts/" + accountId).body()).contains("\"amount\":\"200.00\"");
    }

    @Test
    void reusingAKeyForADifferentAmountIsRejected() throws Exception {
        String accountId = openAccount("Grace Hopper");
        UUID idempotencyKey = UUID.randomUUID();

        post("/api/accounts/" + accountId + "/deposits", """
                {"amount": {"amount": "10.00", "currency": "EUR"}}
                """, idempotencyKey);
        HttpResponse<String> different = post("/api/accounts/" + accountId + "/deposits", """
                {"amount": {"amount": "999.00", "currency": "EUR"}}
                """, idempotencyKey);

        assertThat(different.statusCode()).isEqualTo(409);
        assertThat(different.body()).contains("idempotency-conflict");
    }

    @Test
    void transfersMoneyBetweenTwoAccounts() throws Exception {
        String sourceId = openAccount("Jesús Blázquez");
        String destinationId = openAccount("Ada Lovelace");
        String destinationIban = json(get("/api/accounts/" + destinationId), "iban");
        post("/api/accounts/" + sourceId + "/deposits", """
                {"amount": {"amount": "500.00", "currency": "EUR"}}
                """, UUID.randomUUID());

        HttpResponse<String> transfer =
                post("/api/transfers", """
                {"sourceAccountId": "%s", "destinationIban": "%s", "amount": {"amount": "300.00", "currency": "EUR"}}
                """.formatted(sourceId, destinationIban), UUID.randomUUID());

        assertThat(transfer.statusCode()).isEqualTo(200);
        assertThat(get("/api/accounts/" + sourceId).body()).contains("\"amount\":\"200.00\"");
        assertThat(get("/api/accounts/" + destinationId).body()).contains("\"amount\":\"300.00\"");
    }

    @Test
    void refusesToOverdrawAnAccount() throws Exception {
        String accountId = openAccount("Jesús Blázquez");

        HttpResponse<String> response = post("/api/accounts/" + accountId + "/withdrawals", """
                {"amount": {"amount": "50.00", "currency": "EUR"}}
                """, UUID.randomUUID());

        assertThat(response.statusCode()).isEqualTo(409);
        assertThat(response.headers().firstValue("Content-Type")).contains("application/problem+json");
        assertThat(response.body()).contains("insufficient-balance");
    }

    @Test
    void answersWithProblemDetailsForAnUnknownAccount() throws Exception {
        HttpResponse<String> response = get("/api/accounts/" + UUID.randomUUID());

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.body()).contains("account-not-found");
    }

    @Test
    void rejectsAnAmountThatIsNotADecimalNumber() throws Exception {
        String accountId = openAccount("Jesús Blázquez");

        HttpResponse<String> response = post("/api/accounts/" + accountId + "/deposits", """
                {"amount": {"amount": "one hundred", "currency": "EUR"}}
                """, UUID.randomUUID());

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).contains("validation-failed");
    }

    @Test
    void publishesItsOpenApiDocument() throws Exception {
        HttpResponse<String> response = get("/v3/api-docs");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("Bank ledger API").contains("/api/transfers");
    }

    private String openAccount(String holder) throws Exception {
        HttpResponse<String> response = post("/api/accounts", """
                {"holderName": "%s", "dailyTransferLimit": {"amount": "1000.00", "currency": "EUR"}}
                """.formatted(holder), null);
        return json(response, "id");
    }

    private HttpResponse<String> post(String path, String body, UUID idempotencyKey) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(uri(path))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(body));
        if (idempotencyKey != null) {
            request.header("Idempotency-Key", idempotencyKey.toString());
        }
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(String path) throws Exception {
        return client.send(HttpRequest.newBuilder(uri(path)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }

    /** Minimal JSON field reader: enough for assertions, and it keeps the test free of a parser. */
    private String json(HttpResponse<String> response, String field) {
        Matcher matcher =
                Pattern.compile("\"" + field + "\"\\s*:\\s*\"([^\"]*)\"").matcher(response.body());
        assertThat(matcher.find()).as("field %s in %s", field, response.body()).isTrue();
        return matcher.group(1);
    }
}
