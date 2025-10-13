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
    public double nextValue(final Index<T> index) {
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
    public boolean precisionReached(final Index<T> index) {
        updateDichotomyIntervalLimits(index);
        if (highestAdmissibleStep == null && lowestInadmissibleStep == null) {
            return false;
        } else if (highestAdmissibleStep == null) {
            return Math.abs(lowestInadmissibleStep.getLeft() - index.minValue()) < Index.EPSILON;
        } else if (lowestInadmissibleStep == null) {
            return Math.abs(highestAdmissibleStep.getLeft() - index.maxValue()) < Index.EPSILON;
        } else {
            return Math.abs(highestAdmissibleStep.getLeft() - lowestInadmissibleStep.getLeft()) < index.precision();
        }
    }

    private void updateDichotomyIntervalLimits(Index<T> index) {
        final Pair<Double, DichotomyStepResult<T>> lowestInvalidStep = index.lowestInvalidStep();
        if (lowestInvalidStep != null) {
            final ReasonInvalid reasonInvalid = lowestInvalidStep.getRight().getReasonInvalid();

            switch (reasonInvalid) {
                case VALIDATION_FAILED, UNSECURE_AFTER_VALIDATION ->
                    lowestUnsecureStep = lowestInvalidStep;
                case GLSK_LIMITATION -> {
                    if (lowestInvalidStep.getLeft() < referenceExchange) {
                        closestGlskLimitationBelowReference = lowestInvalidStep;
                    } else {
                        closestGlskLimitationAboveReference = lowestInvalidStep;
                    }
                }
                default -> {
                    //no operation
                }
            }
        }
        if (index.highestValidStep() != null) {
            highestSecureStep = index.highestValidStep();
        }

        highestAdmissibleStep = getHighestAdmissibleStep(closestGlskLimitationBelowReference, highestSecureStep);
        lowestInadmissibleStep = getLowestInAdmissibleStep(lowestUnsecureStep, closestGlskLimitationAboveReference);
    }

    private Pair<Double, DichotomyStepResult<T>> getHighestAdmissibleStep(final Pair<Double, DichotomyStepResult<T>> closestGlskLimitationBelowReference,
                                                                          final Pair<Double, DichotomyStepResult<T>> highestSecureStep) {
        return testAndGetStep(closestGlskLimitationBelowReference,
                              highestSecureStep,
                              (res1, res2) -> res1 > res2);
    }

    private Pair<Double, DichotomyStepResult<T>> getLowestInAdmissibleStep(final Pair<Double, DichotomyStepResult<T>> lowestUnsecureStep,
                                                                           final Pair<Double, DichotomyStepResult<T>> closestGlskLimitationAboveReference) {
        return testAndGetStep(lowestUnsecureStep,
                              closestGlskLimitationAboveReference,
                              (res1, res2) -> res1 < res2);
    }

    private Pair<Double, DichotomyStepResult<T>> testAndGetStep(final Pair<Double, DichotomyStepResult<T>> below,
                                                                final Pair<Double, DichotomyStepResult<T>> above,
                                                                final BiPredicate<Double, Double> rule) {
        if (below == null && above == null) {
            return null;
        } else if (below == null) {
            return above;
        } else if (above == null) {
            return below;
        } else { // step1 && step2 are != null
            if (rule.test(below.getLeft(), above.getLeft())) {
                return below;
            } else {
                return above;
            }
        }
    }

}
