package com.farao_community.farao.dichotomy.network;

import com.farao_community.farao.dichotomy.api.StepResult;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public class NetworkStepResultWrapper<I extends NetworkValidationResult> implements StepResult {
    private final double stepValue;
    private final boolean secure;
    private final  I networkValidationResult;
    private final ReasonUnsecure reasonUnsecure;

    public NetworkStepResultWrapper(double stepValue, boolean secure, I networkValidationResult, ReasonUnsecure reasonUnsecure) {
        this.stepValue = stepValue;
        this.secure = secure;
        this.networkValidationResult = networkValidationResult;
        this.reasonUnsecure = reasonUnsecure;
    }

    public static <T extends NetworkValidationResult> NetworkStepResultWrapper<T> withGlskLimitation(double stepValue) {
        return new NetworkStepResultWrapper<>(stepValue, false, null, ReasonUnsecure.GLSK_LIMITATION);
    }

    public static <T extends NetworkValidationResult> NetworkStepResultWrapper<T> fromNetworkValidationResult(double stepValue,
                                                                                                              T networkValidationResult) {
        return new NetworkStepResultWrapper<>(
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

    public I getNetworkValidationResult() {
        return networkValidationResult;
    }
}
