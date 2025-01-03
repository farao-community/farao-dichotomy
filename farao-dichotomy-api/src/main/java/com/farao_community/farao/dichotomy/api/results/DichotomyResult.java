/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy.api.results;

import com.farao_community.farao.dichotomy.api.index.Index;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 * @author Vincent Bochet {@literal <vincent.bochet at rte-france.com>}
 */
public final class DichotomyResult<I> {
    private final Pair<Double, DichotomyStepResult<I>> highestValidStep;
    private final Pair<Double, DichotomyStepResult<I>> lowestInvalidStep;
    private final LimitingCause limitingCause;
    private final String limitingFailureMessage;
    private boolean interrupted;
    private final boolean raoFailed;
    private final String raoFailureMessage;

    private DichotomyResult(Pair<Double, DichotomyStepResult<I>> highestValidStep,
                            Pair<Double, DichotomyStepResult<I>> lowestInvalidStep,
                            LimitingCause limitingCause,
                            String limitingFailureMessage) {
        this.highestValidStep = highestValidStep;
        this.lowestInvalidStep = lowestInvalidStep;
        this.limitingCause = limitingCause;
        this.limitingFailureMessage = limitingFailureMessage;
        this.interrupted = false;
        this.raoFailed = false;
        this.raoFailureMessage = null;
    }

    private DichotomyResult(String raoFailureMessage) {
        this.highestValidStep = null;
        this.lowestInvalidStep = null;
        this.limitingCause = null;
        this.limitingFailureMessage = null;
        this.raoFailed = true;
        this.raoFailureMessage = raoFailureMessage;
    }

    public static <J> DichotomyResult<J> buildFromIndex(Index<J> index) {
        // If one the steps are null it means that it stops due to index evaluation otherwise it could have continued.
        // If both are present, it is the expected case we just have to differentiate if the invalid step failed or if
        // it is just unsecure.
        LimitingCause limitingCause = LimitingCause.INDEX_EVALUATION_OR_MAX_ITERATION;
        String failureMessage = "None";
        if (index.lowestInvalidStep() != null && index.highestValidStep() != null) {
            if (index.lowestInvalidStep().getRight().isFailed()) {
                limitingCause = switch (index.lowestInvalidStep().getRight().getReasonInvalid()) {
                    case GLSK_LIMITATION -> LimitingCause.GLSK_LIMITATION;
                    case BALANCE_LOADFLOW_DIVERGENCE -> LimitingCause.BALANCE_LOADFLOW_DIVERGENCE;
                    case UNKNOWN_TERMINAL_BUS -> LimitingCause.UNKNOWN_TERMINAL_BUS;
                    default -> LimitingCause.COMPUTATION_FAILURE;
                };
                failureMessage = index.lowestInvalidStep().getRight().getFailureMessage();
            } else {
                limitingCause = LimitingCause.CRITICAL_BRANCH;
            }
        }

        Pair<Double, DichotomyStepResult<J>> highestValidStepResponse = index.highestValidStep();
        Pair<Double, DichotomyStepResult<J>> lowestInvalidStepResponse = index.lowestInvalidStep();
        return new DichotomyResult<>(highestValidStepResponse, lowestInvalidStepResponse, limitingCause, failureMessage);
    }

    public static <J> DichotomyResult<J> buildFromRaoFailure(String raoFailureMessage) {
        return new DichotomyResult<>(raoFailureMessage);
    }

    public DichotomyStepResult<I> getHighestValidStep() {
        return highestValidStep == null ? null : highestValidStep.getRight();
    }

    public DichotomyStepResult<I> getLowestInvalidStep() {
        return lowestInvalidStep == null ? null : lowestInvalidStep.getRight();
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
        return highestValidStep != null ? highestValidStep.getLeft() : Double.NaN;
    }

    @JsonIgnore
    public double getLowestInvalidStepValue() {
        return lowestInvalidStep != null ? lowestInvalidStep.getLeft() : Double.NaN;
    }

    @JsonIgnore
    public boolean isInterrupted() {
        return interrupted;
    }

    public void setInterrupted(boolean interrupted) {
        this.interrupted = interrupted;
    }

    @JsonIgnore
    public boolean isRaoFailed() {
        return raoFailed;
    }

    @JsonIgnore
    public String getRaoFailureMessage() {
        return raoFailureMessage;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
