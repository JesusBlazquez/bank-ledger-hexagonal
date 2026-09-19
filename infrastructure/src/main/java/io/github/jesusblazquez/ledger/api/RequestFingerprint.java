package io.github.jesusblazquez.ledger.api;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Summarises a request so a repeated idempotency key can be checked against what it was used for.
 *
 * <p>Without this, sending the same key with a different body would silently return the first
 * answer — the client would believe it transferred 500 euros when it transferred 50.
 */
final class RequestFingerprint {

    private RequestFingerprint() {}

    static String of(Object... parts) {
        StringBuilder joined = new StringBuilder();
        for (Object part : parts) {
            joined.append(part).append('|');
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(joined.toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required by every JVM", e);
        }
    }
}
