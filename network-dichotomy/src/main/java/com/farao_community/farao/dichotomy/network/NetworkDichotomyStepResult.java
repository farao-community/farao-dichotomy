/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy.network;

import com.farao_community.farao.data.rao_result_api.OptimizationState;
import com.farao_community.farao.data.rao_result_api.RaoResult;
import com.farao_community.farao.dichotomy.api.StepResult;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public class NetworkDichotomyStepResult<I> implements StepResult {
    private final double stepValue;
    private final boolean secure;
    private final NetworkValidationResult<I> networkValidationResult;
    private final ReasonInvalid reasonInvalid;
    private final String failureMessage;

    public NetworkDichotomyStepResult(double stepValue, ReasonInvalid reasonInvalid, String failureMessage) {
        this.stepValue = stepValue;
        this.secure = false;
        this.networkValidationResult = null;
        this.reasonInvalid = reasonInvalid;
        this.failureMessage = failureMessage;
    }

    public NetworkDichotomyStepResult(double stepValue, NetworkValidationResult<I> networkValidationResult) {
        this.stepValue = stepValue;
        this.networkValidationResult = networkValidationResult;
        this.secure = raoResultIsSecure(networkValidationResult.getRaoResult());
        this.reasonInvalid = secure ? ReasonInvalid.NONE : ReasonInvalid.UNSECURE_AFTER_VALIDATION;
        this.failureMessage = "None";
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
     * @return An 'empty' wrapper that contains only meta-information on invalidity reasons
     */
    public static <J> NetworkDichotomyStepResult<J> fromFailure(double stepValue,
                                                                ReasonInvalid reasonInvalid,
                                                                String failureMessage) {
        return new NetworkDichotomyStepResult<>(stepValue, reasonInvalid, failureMessage);
    }

    /**
     * In case network validation works properly there are only two wys out, either it is secured or unsecured.
     * According to that corresponding {@link ReasonInvalid} would be chosen: {@code NONE} for a secured network --
     * which is then valid -- and {@code UNSECURE_AFTER_VALIDATION} for an unsecured network.
     *
     * @param stepValue: Current step value
     * @param networkValidationResult: Concrete network validation result that would contain validation data
     * @return A network dichotomy step result containing validation data and meta-data about its security
     */
    public static <J> NetworkDichotomyStepResult<J> fromNetworkValidationResult(double stepValue,
                                                                                NetworkValidationResult<J> networkValidationResult) {
        return new NetworkDichotomyStepResult<>(stepValue, networkValidationResult);
    }

    private static boolean raoResultIsSecure(RaoResult raoResult) {
        return raoResult.getFunctionalCost(OptimizationState.AFTER_CRA) <= 0;
    }

    public double getStepValue() {
        return stepValue;
    }

    public NetworkValidationResult<I> getNetworkValidationResult() {
        return networkValidationResult;
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

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
