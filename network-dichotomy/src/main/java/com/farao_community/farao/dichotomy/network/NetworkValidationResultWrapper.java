package com.farao_community.farao.dichotomy.network;

import com.farao_community.farao.dichotomy.api.StepResult;

import java.util.Optional;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public class NetworkValidationResultWrapper<I extends NetworkValidationResult> implements StepResult {
    private final double stepValue;
    private final boolean secure;
    private final  I networkValidationResult;
    private final ReasonUnsecure reasonUnsecure;

    public NetworkValidationResultWrapper(double stepValue, boolean secure, I networkValidationResult, ReasonUnsecure reasonUnsecure) {
        this.stepValue = stepValue;
        this.secure = secure;
        this.networkValidationResult = networkValidationResult;
        this.reasonUnsecure = reasonUnsecure;
    }

    public static <T extends NetworkValidationResult> NetworkValidationResultWrapper<T> withGlskLimitation(double stepValue) {
        return new NetworkValidationResultWrapper<>(stepValue, false, null, ReasonUnsecure.GLSK_LIMITATION);
    }

    public static <T extends NetworkValidationResult> NetworkValidationResultWrapper<T> fromNetworkValidationResult(double stepValue,
                                                                                                                    T networkValidationResult) {
        return new NetworkValidationResultWrapper<>(
                stepValue,
                networkValidationResult.isSecure(),
                networkValidationResult,
                networkValidationResult.isSecure() ? ReasonUnsecure.NONE : ReasonUnsecure.UNSECURE_AFTER_VALIDATION
        );
    }

    @Override
    public boolean isSecure() {
        return secure;
    }

    @Override
    public double stepValue() {
        return stepValue;
    }

    public ReasonUnsecure getReasonUnsecure() {
        return reasonUnsecure;
    }

    public Optional<I> getNetworkValidationResult() {
        return Optional.ofNullable(networkValidationResult);
    }
}
