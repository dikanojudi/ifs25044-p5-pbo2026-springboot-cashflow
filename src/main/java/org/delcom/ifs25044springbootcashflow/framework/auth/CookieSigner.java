package org.delcom.ifs25044springbootcashflow.framework.auth;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Menandatangani nilai cookie sesi (HMAC-SHA256) agar ID pengguna di cookie
 * tidak bisa dipalsukan. Tanpa tanda tangan, siapa pun bisa mengirim cookie
 * dengan ID admin dan langsung dianggap login sebagai admin.
 */
final class CookieSigner {
    private static final String ALGORITHM = "HmacSHA256";

    private final byte[] key;

    CookieSigner(String secret) {
        this.key = secret.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Membuat signer dari konfigurasi: APP_COOKIE_SECRET jika ada; jika tidak,
     * diturunkan dari rahasia lain yang sudah ada di konfigurasi aplikasi
     * (ROOT_PASSWORD, DB_PASSWORD, DB_DATABASE); jika semuanya kosong, memakai
     * rahasia acak per proses (cookie lama tidak berlaku setelah restart).
     */
    static CookieSigner fromEnvironment() {
        String explicit = lookup("APP_COOKIE_SECRET");
        if (explicit != null && !explicit.isBlank()) {
            return new CookieSigner(explicit);
        }
        String derived = lookup("ROOT_PASSWORD") + "|" + lookup("DB_PASSWORD") + "|" + lookup("DB_DATABASE");
        if (!derived.equals("null|null|null")) {
            return new CookieSigner("cashflow|" + derived);
        }
        return new CookieSigner(UUID.randomUUID().toString());
    }

    private static String lookup(String name) {
        String property = System.getProperty(name);
        return property != null ? property : System.getenv(name);
    }

    /** Menghasilkan nilai cookie berformat {@code <id>.<tanda-tangan>}. */
    String sign(int userId) {
        return userId + "." + signature(String.valueOf(userId));
    }

    /** Mengembalikan ID pengguna jika tanda tangan valid, atau {@code null} jika tidak. */
    Integer verify(String cookieValue) {
        if (cookieValue == null) {
            return null;
        }
        int dot = cookieValue.indexOf('.');
        if (dot <= 0) {
            return null;
        }
        String idPart = cookieValue.substring(0, dot);
        String signaturePart = cookieValue.substring(dot + 1);
        int userId;
        try {
            userId = Integer.parseInt(idPart);
        } catch (NumberFormatException ex) {
            return null;
        }
        byte[] expected = signature(idPart).getBytes(StandardCharsets.UTF_8);
        byte[] actual = signaturePart.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expected, actual) ? userId : null;
    }

    private String signature(String payload) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(key, ALGORITHM));
            return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("HMAC-SHA256 tidak tersedia", ex);
        }
    }
}
