/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy.network;

import com.farao_community.farao.dichotomy.api.Index;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public final class NetworkDichotomyResult<I> {

    private final NetworkDichotomyStepResult<I> highestValidStep;
    private final NetworkDichotomyStepResult<I> lowestInvalidStep;
    private final LimitingCause limitingCause;
    private final String limitingFailureMessage;

    private NetworkDichotomyResult(NetworkDichotomyStepResult<I> highestValidStep,
                                   NetworkDichotomyStepResult<I> lowestInvalidStep,
                                   LimitingCause limitingCause,
                                   String limitingFailureMessage) {
        this.highestValidStep = highestValidStep;
        this.lowestInvalidStep = lowestInvalidStep;
        this.limitingCause = limitingCause;
        this.limitingFailureMessage = limitingFailureMessage;
    }

    private NetworkDichotomyResult(NetworkDichotomyStepResult<I> highestValidStep,
                                   NetworkDichotomyStepResult<I> lowestInvalidStep,
                                   LimitingCause limitingCause) {
        this.highestValidStep = highestValidStep;
        this.lowestInvalidStep = lowestInvalidStep;
        this.limitingCause = limitingCause;
        this.limitingFailureMessage = "None";
    }

    public static <J> NetworkDichotomyResult<J> buildFromIndex(Index<NetworkDichotomyStepResult<J>> index) {
        // If one the steps are null it means that it stops due to index evaluation otherwise it could have continued.
        // If both are present, it is the expected case we just have to differentiate if the invalid step failed or if
        // it is just unsecure.
        LimitingCause limitingCause = LimitingCause.INDEX_EVALUATION_OR_MAX_ITERATION;
        String failureMessage = "None";
        if (index.lowestInvalidStep() != null && index.highestValidStep() != null) {
            if (index.lowestInvalidStep().isFailed()) {
                limitingCause = index.lowestInvalidStep().getReasonInvalid() == ReasonInvalid.GLSK_LIMITATION ?
                    LimitingCause.GLSK_LIMITATION : LimitingCause.COMPUTATION_FAILURE;
                failureMessage = index.lowestInvalidStep().getFailureMessage();
            } else {
                limitingCause = LimitingCause.CRITICAL_BRANCH;
            }
        }

        NetworkDichotomyStepResult<J> highestValidStepResponse = index.highestValidStep();
        NetworkDichotomyStepResult<J> lowestInvalidStepResponse = index.lowestInvalidStep();
        return new NetworkDichotomyResult<>(highestValidStepResponse, lowestInvalidStepResponse, limitingCause, failureMessage);
    }

    public NetworkDichotomyStepResult<I> getHighestValidStep() {
        return highestValidStep;
    }

    public NetworkDichotomyStepResult<I> getLowestInvalidStep() {
        return lowestInvalidStep;
    }

    public LimitingCause getLimitingCause() {
        return limitingCause;
    }

    public String getLimitingFailureMessage() {
        return limitingFailureMessage;
    }

    @JsonIgnore
    public boolean hasValidStep() {
        return highestValidStep != null;
    }

    @JsonIgnore
    public double getHighestValidStepValue() {
        return highestValidStep != null ? highestValidStep.getStepValue() : Double.NaN;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
