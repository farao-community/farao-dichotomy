package com.farao_community.farao.dichotomy.network;

import com.farao_community.farao.dichotomy.api.StepResult;

import java.util.Optional;

/**
 * This implementation of {@link StepResult} is a generic wrapper for classes that implements
 * {@link NetworkValidationResult}. It wraps pure result of network validation to add some generic information.
 * First, as the {@link StepResult} requires it holds the step value. Then it is all based on {@link ReasonInvalid}
 * if it is {@code NONE}, it would mean that the step result is valid. Otherwise, {@code UNSECURE_AFTER_VALIDATION}
 * would mean that the validation went well but the network is unsecured. Finally, {@code GLSK_LIMITATION} and
 * {@code VALIDATION_FAILURE} would mean that the validation failed (we want to treat GLSK limitation case separatly
 * because of business requirements), still in this two cases we would have additional information on the failure
 * cause thanks to {@code failureMessage}.
 * This generic implementation is used in {@link AbstractNetworkValidationStrategy} to handle without any information
 * on the {@link NetworkValidator} and on the shifting strategy that would be implemented in the concrete classes
 * inherited from {@link AbstractNetworkValidationStrategy}. The purpose is to handle the step value and the basic
 * failure cases that could occur during step validation based on a network.
 *
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public class NetworkValidationResultWrapper<I extends NetworkValidationResult> implements StepResult {
    private final double stepValue;
    private final  I networkValidationResult;
    private final ReasonInvalid reasonInvalid;
    private final String failureMessage;

    public NetworkValidationResultWrapper(double stepValue,
                                          I networkValidationResult,
                                          ReasonInvalid reasonInvalid,
                                          String failureMessage) {
        this.stepValue = stepValue;
        this.networkValidationResult = networkValidationResult;
        this.reasonInvalid = reasonInvalid;
        this.failureMessage = failureMessage;
    }

    /**
     * In case the network validation fails, we suppose that no {@link NetworkValidationResult} are available then
     * we just build the wrapper with an empty proper validation result. To remain consistent either
     * {@code GLSK_LIMITATION} or {@code VALIDATION_FAILURE} must be used here in {@link ReasonInvalid} because
     * there are the two only failure reasons that could qualify the {@link StepResult} within {@link ReasonInvalid}
     * enumeration.
     *
     * @param stepValue: Current step value
     * @param reasonInvalid: Qualify invalidity for failure, either GLSK_LIMITATION or VALIDATION_FAILURE
     * @param failureMessage: Additional information that could come from the original error or be created
     * @param <T>: Type of {@link NetworkValidationResult}
     * @return An 'empty' wrapper that contains only meta-information on invalidity reasons
     */
    public static <T extends NetworkValidationResult> NetworkValidationResultWrapper<T> fromNetworkValidationFailure(double stepValue,
                                                                                                                     ReasonInvalid reasonInvalid,
                                                                                                                     String failureMessage) {
        return new NetworkValidationResultWrapper<>(
                stepValue,
                null,
                reasonInvalid,
                failureMessage);
    }

    /**
     * In case network validation works properly there are only two wys out, either it is secured or unsecured.
     * According to that corresponding {@link ReasonInvalid} would be chosen: {@code NONE} for a secured network --
     * which is then valid -- and {@code UNSECURE_AFTER_VALIDATION} for an unsecured network.
     *
     * @param stepValue: Current step value
     * @param networkValidationResult: Concrete network validation result that would contain validation data
     * @param <T>: Type of {@link NetworkValidationResult}
     * @return A network validation result wrapper containing validation data and meta-data about its security
     */
    public static <T extends NetworkValidationResult> NetworkValidationResultWrapper<T> fromNetworkValidationResult(double stepValue,
                                                                                                                    T networkValidationResult) {
        return new NetworkValidationResultWrapper<>(
                stepValue,
                networkValidationResult,
                networkValidationResult.isSecure() ? ReasonInvalid.NONE : ReasonInvalid.UNSECURE_AFTER_VALIDATION,
                "None"
        );
    }

    public boolean isFailed() {
        return reasonInvalid == ReasonInvalid.GLSK_LIMITATION || reasonInvalid == ReasonInvalid.VALIDATION_FAILED;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    @Override
    public boolean isValid() {
        return reasonInvalid == ReasonInvalid.NONE;
    }

    public ReasonInvalid getReasonInvalid() {
        return reasonInvalid;
    }

    @Override
    public double stepValue() {
        return stepValue;
    }

    public Optional<I> getNetworkValidationResult() {
        return Optional.ofNullable(networkValidationResult);
    }
}
