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

import java.util.function.BiPredicate;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public class BiDirectionalStepsWithReferenceIndexStrategy<T> implements IndexStrategy<T> {
    private final double startIndex;
    private final double stepSize;
    private final double referenceExchange;

    private Pair<Double, DichotomyStepResult<T>> highestSecureStep;
    private Pair<Double, DichotomyStepResult<T>> lowestUnsecureStep;
    private Pair<Double, DichotomyStepResult<T>> closestGlskLimitationBelowReference;
    private Pair<Double, DichotomyStepResult<T>> closestGlskLimitationAboveReference;

    private Pair<Double, DichotomyStepResult<T>> highestAdmissibleStep;
    private Pair<Double, DichotomyStepResult<T>> lowestInadmissibleStep;

    public BiDirectionalStepsWithReferenceIndexStrategy(double startIndex, double stepSize, double referenceExchange) {
        this.startIndex = startIndex;
        this.stepSize = stepSize;
        this.referenceExchange = referenceExchange;
    }

    @Override
    public double nextValue(Index<T> index) {
        updateDichotomyIntervalLimits(index);
        if (highestAdmissibleStep == null && lowestInadmissibleStep == null) {
            return startIndex;
        } else if (highestAdmissibleStep == null) {
            return Math.max(index.minValue(), lowestInadmissibleStep.getLeft() - stepSize);
        } else if (lowestInadmissibleStep == null) {
            return Math.min(index.maxValue(), highestAdmissibleStep.getLeft() + stepSize);
        } else {
            return (lowestInadmissibleStep.getLeft() + highestAdmissibleStep.getLeft()) / 2;
        }
    }

    @Override
    public boolean precisionReached(Index<T> index) {
        updateDichotomyIntervalLimits(index);
        if (highestAdmissibleStep == null && lowestInadmissibleStep == null) {
            return false;
        } else if (highestAdmissibleStep == null) {
            return Math.abs(lowestInadmissibleStep.getLeft() - index.minValue()) < EPSILON;
        } else if (lowestInadmissibleStep == null) {
            return Math.abs(highestAdmissibleStep.getLeft() - index.maxValue()) < EPSILON;
        } else {
            return Math.abs(highestAdmissibleStep.getLeft() - lowestInadmissibleStep.getLeft()) < index.precision();
        }
    }

    private void updateDichotomyIntervalLimits(Index<T> index) {
        if (index.lowestInvalidStep() != null &&
            (index.lowestInvalidStep().getRight().getReasonInvalid().equals(ReasonInvalid.UNSECURE_AFTER_VALIDATION)
                || index.lowestInvalidStep().getRight().getReasonInvalid().equals(ReasonInvalid.VALIDATION_FAILED))) {
            lowestUnsecureStep = index.lowestInvalidStep();
        }
        if (index.highestValidStep() != null) {
            highestSecureStep = index.highestValidStep();
        }

        if (index.lowestInvalidStep() != null && index.lowestInvalidStep().getRight().getReasonInvalid().equals(ReasonInvalid.GLSK_LIMITATION)) {
            if (index.lowestInvalidStep().getLeft() < referenceExchange) {
                closestGlskLimitationBelowReference = index.lowestInvalidStep();
            } else {
                closestGlskLimitationAboveReference = index.lowestInvalidStep();
            }
        }
        highestAdmissibleStep = getHighestAdmissibleStep(closestGlskLimitationBelowReference, highestSecureStep);
        lowestInadmissibleStep = getLowestInAdmissibleStep(lowestUnsecureStep, closestGlskLimitationAboveReference);
    }

    private Pair<Double, DichotomyStepResult<T>> getHighestAdmissibleStep(Pair<Double, DichotomyStepResult<T>> closestGlskLimitationBelowReference, Pair<Double, DichotomyStepResult<T>> highestSecureStep) {
        return testAndGetStep(closestGlskLimitationBelowReference, highestSecureStep, (t, u) -> t > u);
    }

    private Pair<Double, DichotomyStepResult<T>> getLowestInAdmissibleStep(Pair<Double, DichotomyStepResult<T>> lowestUnsecureStep, Pair<Double, DichotomyStepResult<T>> closestGlskLimitationAboveReference) {
        return testAndGetStep(lowestUnsecureStep, closestGlskLimitationAboveReference, (t, u) -> t < u);
    }

    private Pair<Double, DichotomyStepResult<T>> testAndGetStep(Pair<Double, DichotomyStepResult<T>> step1, Pair<Double, DichotomyStepResult<T>> step2, BiPredicate<Double, Double> biPredicate) {
        if (step1 == null && step2 == null) {
            return null;
        } else if (step1 == null) {
            return step2;
        } else if (step2 == null) {
            return step1;
        } else { // step1 && step2 are != null
            if (biPredicate.test(step1.getLeft(), step2.getLeft())) {
                return step1;
            } else {
                return step2;
            }
        }
    }

}
