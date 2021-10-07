package com.farao_community.farao.dichotomy.network;

import com.farao_community.farao.dichotomy.api.StepResult;

import java.util.Optional;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public class NetworkValidationResultWrapper<I extends NetworkValidationResult> implements StepResult {
    private final double stepValue;
    private final  I networkValidationResult;
    private final ReasonNotValid reasonNotValid;
    private final String failureMessage;

    public NetworkValidationResultWrapper(double stepValue,
                                          I networkValidationResult,
                                          ReasonNotValid reasonNotValid,
                                          String failureMessage) {
        this.stepValue = stepValue;
        this.networkValidationResult = networkValidationResult;
        this.reasonNotValid = reasonNotValid;
        this.failureMessage = failureMessage;
    }

    public static <T extends NetworkValidationResult> NetworkValidationResultWrapper<T> fromNetworkValidationFailure(double stepValue,
                                                                                                                     ReasonNotValid reasonNotValid,
                                                                                                                     String failureMessage) {
        return new NetworkValidationResultWrapper<>(
                stepValue,
                null,
                reasonNotValid,
                failureMessage);
    }

    public static <T extends NetworkValidationResult> NetworkValidationResultWrapper<T> fromNetworkValidationResult(double stepValue,
                                                                                                                    T networkValidationResult) {
        return new NetworkValidationResultWrapper<>(
                stepValue,
                networkValidationResult,
                networkValidationResult.isSecure() ? ReasonNotValid.NONE : ReasonNotValid.UNSECURE_AFTER_VALIDATION,
                null
        );
    }

    public boolean isFailed() {
        return reasonNotValid == ReasonNotValid.GLSK_LIMITATION || reasonNotValid == ReasonNotValid.VALIDATION_FAILED;
    }

    public String getFailureMessage() {
        return isFailed() ? failureMessage : "None";
    }

    @Override
    public boolean isValid() {
        return reasonNotValid == ReasonNotValid.NONE;
    }

    public ReasonNotValid getReasonNotValid() {
        return reasonNotValid;
    }

    @Override
    public double stepValue() {
        return stepValue;
    }

    public Optional<I> getNetworkValidationResult() {
        return Optional.ofNullable(networkValidationResult);
    }
}
