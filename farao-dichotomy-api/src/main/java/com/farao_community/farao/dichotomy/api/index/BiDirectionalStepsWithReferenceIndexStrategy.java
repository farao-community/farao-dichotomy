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
public class BiDirectionalStepsWithReferenceIndexStrategy implements IndexStrategy {
    private final double startIndex;
    private final double stepSize;
    private final double referenceExchange;

    private Pair<Double, ? extends DichotomyStepResult<?>> highestSecureStep;
    private Pair<Double, ? extends DichotomyStepResult<?>> lowestUnsecureStep;
    private Pair<Double, ? extends DichotomyStepResult<?>> closestGlskLimitationBelowReference;
    private Pair<Double, ? extends DichotomyStepResult<?>> closestGlskLimitationAboveReference;

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

        if (index.lowestInvalidStep() != null && index.lowestInvalidStep().getRight().getReasonInvalid().equals(ReasonInvalid.GLSK_LIMITATION)) {
            if (index.lowestInvalidStep().getLeft() < referenceExchange) {
                closestGlskLimitationBelowReference = index.lowestInvalidStep();
            } else {
                closestGlskLimitationAboveReference = index.lowestInvalidStep();
            }
        }

        Pair<Double, ? extends DichotomyStepResult<?>> startInterval = getLowestAdmissibleStep();
        Pair<Double, ? extends DichotomyStepResult<?>> endInterval = getHighestAdmissibleStep();

        if (startInterval == null && endInterval == null) {
            return startIndex;
        } else if (startInterval == null) {
            return Math.max(index.minValue(), endInterval.getLeft() - stepSize);
        } else if (endInterval == null) {
            return Math.min(index.maxValue(), startInterval.getLeft() + stepSize);
        } else {
            return (endInterval.getLeft() + startInterval.getLeft()) / 2;
        }
    }

    @Override
    public boolean precisionReached(Index<?> index) {
        Pair<Double, ? extends DichotomyStepResult<?>> startInterval = getLowestAdmissibleStep();
        Pair<Double, ? extends DichotomyStepResult<?>> endInterval = getHighestAdmissibleStep();
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

    private Pair<Double, ? extends DichotomyStepResult<?>> getLowestAdmissibleStep() {
        return testAndGetAdmissibleValue(closestGlskLimitationBelowReference, highestSecureStep, (t, u) -> t > u);
    }

    private Pair<Double, ? extends DichotomyStepResult<?>> getHighestAdmissibleStep() {
        return testAndGetAdmissibleValue(lowestUnsecureStep, closestGlskLimitationAboveReference, (t, u) -> t < u);
    }

    private Pair<Double, ? extends DichotomyStepResult<?>> testAndGetAdmissibleValue(Pair<Double, ? extends DichotomyStepResult<?>> step1, Pair<Double, ? extends DichotomyStepResult<?>> step2, BiPredicate<Double, Double> biPredicate) {
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
