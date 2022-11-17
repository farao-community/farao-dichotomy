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
            && precisionReached(index)) {
            throw new AssertionError("Dichotomy engine should not ask for next value if precision is reached");
        }

        if (closestGlskLimitationBelowReference != null
            && index.lowestInvalidStep().getRight().getReasonInvalid().equals(ReasonInvalid.UNSECURE_AFTER_VALIDATION)
            && precisionReached(index)) {
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

    @Override
    public boolean precisionReached(Index<?> index) {
        Pair<Double, ? extends DichotomyStepResult<?>> startInterval = getStartInterval(index);
        Pair<Double, ? extends DichotomyStepResult<?>> endInterval = getEndInterval(index);
        if (startInterval == null && endInterval == null) {
            return false;
        } else if (startInterval == null) {
            return Math.abs(endInterval.getLeft() - index.minValue()) <= EPSILON;
        } else if (endInterval == null) {
            return Math.abs(startInterval.getLeft() - index.maxValue()) <= EPSILON;
        } else {
            return Math.abs(endInterval.getLeft() - startInterval.getLeft()) <= index.precision();
        }
    }

    private Pair<Double, ? extends DichotomyStepResult<?>> getStartInterval(Index<?> index) {
        if (closestGlskLimitationBelowReference == null && highestSecureStep == null) {
            return null;
        } else if (closestGlskLimitationBelowReference == null) {
            return highestSecureStep;
        } else if (highestSecureStep == null) {
            return closestGlskLimitationBelowReference;
        } else {
            if (closestGlskLimitationBelowReference.getLeft() > highestSecureStep.getLeft()) {
                return closestGlskLimitationBelowReference;
            } else {
                return index.highestValidStep();
            }
        }
    }

    private Pair<Double, ? extends DichotomyStepResult<?>> getEndInterval(Index<?> index) {
        if (lowestUnsecureStep == null && index.lowestInvalidStep() == null) {
            return null;
        } else if (lowestUnsecureStep != null && index.lowestInvalidStep() == null) {
            return lowestUnsecureStep;
        } else {
            return null;
        }
    }

}
