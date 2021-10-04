package com.farao_community.farao.dichotomy.network;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
class NetworkValidationResultWrapperTest {

    @Test
    void testWithGlskLimitation() {
        NetworkValidationResultWrapper<?> result = NetworkValidationResultWrapper.withGlskLimitation(200);
        assertEquals(200, result.stepValue());
        assertFalse(result.isSecure());
        assertEquals(ReasonUnsecure.GLSK_LIMITATION, result.getReasonUnsecure());
        assertTrue(result.getNetworkValidationResult().isEmpty());
    }

    @Test
    void testWithSecureNetworkValidationResult() {
        NetworkValidationResultWrapper<?> result = NetworkValidationResultWrapper.fromNetworkValidationResult(
                200,
                new NetworkValidationResultImpl(true));
        assertEquals(200, result.stepValue());
        assertTrue(result.isSecure());
        assertEquals(ReasonUnsecure.NONE, result.getReasonUnsecure());
        assertTrue(result.getNetworkValidationResult().isPresent());
        assertTrue(result.getNetworkValidationResult().get().isSecure());
    }

    @Test
    void testWithUnsecureNetworkValidationResult() {
        NetworkValidationResultWrapper<?> result = NetworkValidationResultWrapper.fromNetworkValidationResult(
                200,
                new NetworkValidationResultImpl(false));
        assertEquals(200, result.stepValue());
        assertFalse(result.isSecure());
        assertEquals(ReasonUnsecure.UNSECURE_AFTER_VALIDATION, result.getReasonUnsecure());
        assertTrue(result.getNetworkValidationResult().isPresent());
        assertFalse(result.getNetworkValidationResult().get().isSecure());
    }
}
