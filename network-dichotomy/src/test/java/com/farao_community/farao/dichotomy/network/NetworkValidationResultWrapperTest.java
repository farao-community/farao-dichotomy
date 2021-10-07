package com.farao_community.farao.dichotomy.network;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
class NetworkValidationResultWrapperTest {

    @Test
    void testWithGlskLimitation() {
        NetworkValidationResultWrapper<?> result = NetworkValidationResultWrapper.fromNetworkValidationFailure(200, ReasonInvalid.GLSK_LIMITATION, "GLSK limits");
        assertEquals(200, result.stepValue());
        assertFalse(result.isValid());
        assertEquals(ReasonInvalid.GLSK_LIMITATION, result.getReasonInvalid());
        assertTrue(result.getNetworkValidationResult().isEmpty());
    }

    @Test
    void testWithSecureNetworkValidationResult() {
        NetworkValidationResultWrapper<?> result = NetworkValidationResultWrapper.fromNetworkValidationResult(
                200,
                new NetworkValidationResultImpl(true));
        assertEquals(200, result.stepValue());
        assertTrue(result.isValid());
        assertEquals(ReasonInvalid.NONE, result.getReasonInvalid());
        assertTrue(result.getNetworkValidationResult().isPresent());
        assertTrue(result.getNetworkValidationResult().get().isSecure());
    }

    @Test
    void testWithUnsecureNetworkValidationResult() {
        NetworkValidationResultWrapper<?> result = NetworkValidationResultWrapper.fromNetworkValidationResult(
                200,
                new NetworkValidationResultImpl(false));
        assertEquals(200, result.stepValue());
        assertFalse(result.isValid());
        assertEquals(ReasonInvalid.UNSECURE_AFTER_VALIDATION, result.getReasonInvalid());
        assertTrue(result.getNetworkValidationResult().isPresent());
        assertFalse(result.getNetworkValidationResult().get().isSecure());
    }
}
