package org.delcom.ifs25044springbootcashflow.framework.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class CookieSignerTest {

    @Test
    void signedValueRoundTrips() {
        CookieSigner signer = new CookieSigner("rahasia");
        assertEquals(Integer.valueOf(7), signer.verify(signer.sign(7)));
    }

    @Test
    void forgedOrMalformedValuesAreRejected() {
        CookieSigner signer = new CookieSigner("rahasia");
        String valid = signer.sign(2);

        assertNull(signer.verify(null));
        assertNull(signer.verify("1"));
        assertNull(signer.verify(".abc"));
        assertNull(signer.verify("abc.def"));
        assertNull(signer.verify("1.palsu"));
        // ID diganti tetapi tanda tangan milik ID lain
        assertNull(signer.verify("1" + valid.substring(valid.indexOf('.'))));
    }

    @Test
    void differentSecretsProduceDifferentSignatures() {
        assertNotEquals(new CookieSigner("a").sign(1), new CookieSigner("b").sign(1));
        assertNull(new CookieSigner("b").verify(new CookieSigner("a").sign(1)));
    }

    @Test
    void fromEnvironmentUsesExplicitSecret() {
        System.setProperty("APP_COOKIE_SECRET", "eksplisit");
        try {
            CookieSigner signer = CookieSigner.fromEnvironment();
            assertEquals(Integer.valueOf(3), signer.verify(new CookieSigner("eksplisit").sign(3)));
        } finally {
            System.clearProperty("APP_COOKIE_SECRET");
        }
    }

    @Test
    void fromEnvironmentFallsBackToDerivedOrRandomSecret() {
        System.clearProperty("APP_COOKIE_SECRET");
        CookieSigner signer = CookieSigner.fromEnvironment();
        assertNotNull(signer);
        assertEquals(Integer.valueOf(5), signer.verify(signer.sign(5)));
    }
}
