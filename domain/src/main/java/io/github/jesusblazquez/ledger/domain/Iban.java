package io.github.jesusblazquez.ledger.domain;

import io.github.jesusblazquez.ledger.domain.exception.InvalidIbanException;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * An International Bank Account Number, validated with the modulo 97 check of ISO 13616.
 *
 * <p>The check digits catch mistyped accounts before any money moves, which is the whole point of
 * the standard. Holding an {@code Iban} instance therefore means the value is structurally valid:
 * validation lives here, not scattered across controllers.
 */
public record Iban(String value) {

    private static final Pattern STRUCTURE = Pattern.compile("[A-Z]{2}[0-9]{2}[A-Z0-9]{10,30}");
    private static final int MAX_LENGTH = 34;

    public Iban {
        Objects.requireNonNull(value, "IBAN is required");
        value = value.replaceAll("[\\s-]", "").toUpperCase();

        if (value.length() > MAX_LENGTH) {
            throw new InvalidIbanException("longer than %d characters".formatted(MAX_LENGTH));
        }
        if (!STRUCTURE.matcher(value).matches()) {
            throw new InvalidIbanException("'%s' does not follow the IBAN structure".formatted(value));
        }
        if (!hasValidCheckDigits(value)) {
            throw new InvalidIbanException("'%s' has wrong check digits".formatted(value));
        }
    }

    public static Iban of(String value) {
        return new Iban(value);
    }

    public String countryCode() {
        return value.substring(0, 2);
    }

    /** Safe representation for logs and error messages: only the last four digits are readable. */
    public String masked() {
        return value.substring(0, 4) + "*".repeat(value.length() - 8) + value.substring(value.length() - 4);
    }

    /**
     * Moves the first four characters to the end, replaces every letter by its position in the
     * alphabet plus nine, and checks that the resulting number leaves a remainder of 1 when divided
     * by 97. The remainder is computed digit by digit because the number is far larger than a long.
     */
    private static boolean hasValidCheckDigits(String iban) {
        String rearranged = iban.substring(4) + iban.substring(0, 4);
        int remainder = 0;
        for (char character : rearranged.toCharArray()) {
            int digits = Character.isDigit(character) ? Character.getNumericValue(character) : character - 'A' + 10;
            remainder = (digits > 9 ? remainder * 100 + digits : remainder * 10 + digits) % 97;
        }
        return remainder == 1;
    }
}
