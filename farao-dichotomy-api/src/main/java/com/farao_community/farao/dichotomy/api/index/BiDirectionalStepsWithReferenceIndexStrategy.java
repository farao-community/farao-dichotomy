/*
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy.api.index;

import com.farao_community.farao.dichotomy.api.results.DichotomyStepResult;
import com.farao_community.farao.dichotomy.api.results.ReasonInvalid;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public class BiDirectionalStepsWithReferenceIndexStrategy implements IndexStrategy {
    private final double startIndex;
    private final double stepSize;
    private final double referenceExchange;
    private Pair<Double, ? extends DichotomyStepResult<?>> closestGlskLimitationBelowReference;
    private Pair<Double, ? extends DichotomyStepResult<?>> lowestUnsecureStep;
    private Pair<Double, ? extends DichotomyStepResult<?>> highestSecureStep;

    public BiDirectionalStepsWithReferenceIndexStrategy(double startIndex, double stepSize, double referenceExchange) {
        this.startIndex = startIndex;
        this.stepSize = stepSize;
        this.referenceExchange = referenceExchange;
    }

    @Override
    public double nextValue(Index<?> index) {
        if (index.lowestInvalidStep() != null &&
            index.lowestInvalidStep().getRight().getReasonInvalid().equals(ReasonInvalid.UNSECURE_AFTER_VALIDATION)) {
            lowestUnsecureStep = index.lowestInvalidStep();
        }
        if (index.highestValidStep() != null) {
            highestSecureStep = index.highestValidStep();
        }

        if (index.lowestInvalidStep() != null &&
            index.lowestInvalidStep().getRight().getReasonInvalid().equals(ReasonInvalid.GLSK_LIMITATION) &&
            index.lowestInvalidStep().getLeft() < referenceExchange) {
            closestGlskLimitationBelowReference = index.lowestInvalidStep();
        }

        if (index.lowestInvalidStep() != null &&
            !index.lowestInvalidStep().getRight().getReasonInvalid().equals(ReasonInvalid.GLSK_LIMITATION)
            && index.precisionReached(index.highestValidStep(), index.lowestInvalidStep(), index)) {
            throw new AssertionError("Dichotomy engine should not ask for next value if precision is reached");
        }

        if (closestGlskLimitationBelowReference != null
            && index.lowestInvalidStep().getRight().getReasonInvalid().equals(ReasonInvalid.UNSECURE_AFTER_VALIDATION)
            && index.precisionReached(closestGlskLimitationBelowReference, index.lowestInvalidStep(), index)) {
            throw new AssertionError("Dichotomy engine should not ask for next value if precision is reached");
        }

        if (index.highestValidStep() == null && index.lowestInvalidStep() == null) {
            return startIndex;
        } else if (index.highestValidStep() == null) {

            if (closestGlskLimitationBelowReference != null && index.lowestInvalidStep().equals(closestGlskLimitationBelowReference)) {
                if (lowestUnsecureStep != null) {
                    return (closestGlskLimitationBelowReference.getLeft() + lowestUnsecureStep.getLeft()) / 2;
                } else {
                    return Math.min(index.maxValue(), index.lowestInvalidStep().getLeft() + stepSize);
                }
            }
            return Math.max(index.minValue(), index.lowestInvalidStep().getLeft() - (stepSize / 2));

        } else if (index.lowestInvalidStep() == null) {
            return Math.min(index.maxValue(), highestSecureStep.getLeft() + stepSize);
        } else {
            if (lowestUnsecureStep != null) {
                return (lowestUnsecureStep.getLeft() + highestSecureStep.getLeft()) / 2;
            } else {
                return Math.min(index.maxValue(), index.highestValidStep().getLeft() + stepSize);
            }
        }
    }
}
