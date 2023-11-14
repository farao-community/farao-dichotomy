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
// TODO : rendre générique
public class BiDirectionalStepsWithReferenceIndexStrategy implements IndexStrategy<SingleValueDichotomyStep> {
    private final double startIndex;
    private final double stepSize;
    private final double referenceExchange;

    private Pair<SingleValueDichotomyStep, ? extends DichotomyStepResult<?>> highestSecureStep;
    private Pair<SingleValueDichotomyStep, ? extends DichotomyStepResult<?>> lowestUnsecureStep;
    private Pair<SingleValueDichotomyStep, ? extends DichotomyStepResult<?>> closestGlskLimitationBelowReference;
    private Pair<SingleValueDichotomyStep, ? extends DichotomyStepResult<?>> closestGlskLimitationAboveReference;

    private Pair<SingleValueDichotomyStep, ? extends DichotomyStepResult<?>> highestAdmissibleStep;
    private Pair<SingleValueDichotomyStep, ? extends DichotomyStepResult<?>> lowestInadmissibleStep;

    public BiDirectionalStepsWithReferenceIndexStrategy(double startIndex, double stepSize, double referenceExchange) {
        this.startIndex = startIndex;
        this.stepSize = stepSize;
        this.referenceExchange = referenceExchange;
    }

    @Override
    public SingleValueDichotomyStep nextValue(Index<?, SingleValueDichotomyStep> index) {
        updateDichotomyIntervalLimits(index);
        if (highestAdmissibleStep == null && lowestInadmissibleStep == null) {
            return new SingleValueDichotomyStep(startIndex);
        } else if (highestAdmissibleStep == null) {
            return new SingleValueDichotomyStep(Math.max(index.minValue().value(), lowestInadmissibleStep.getLeft().value() - stepSize));
        } else if (lowestInadmissibleStep == null) {
            return new SingleValueDichotomyStep(Math.min(index.maxValue().value(), highestAdmissibleStep.getLeft().value() + stepSize));
        } else {
            return new SingleValueDichotomyStep((lowestInadmissibleStep.getLeft().value() + highestAdmissibleStep.getLeft().value()) / 2);
        }
    }

    @Override
    public boolean precisionReached(Index<?, SingleValueDichotomyStep> index) {
        updateDichotomyIntervalLimits(index);
        if (highestAdmissibleStep == null && lowestInadmissibleStep == null) {
            return false;
        } else if (highestAdmissibleStep == null) {
            return index.minValue().distanceTo(lowestInadmissibleStep.getLeft()) < EPSILON;
        } else if (lowestInadmissibleStep == null) {
            return index.maxValue().distanceTo(highestAdmissibleStep.getLeft()) < EPSILON;
        } else {
            return Math.abs(highestAdmissibleStep.getLeft().value() - lowestInadmissibleStep.getLeft().value()) < index.precision();
        }
    }

    private void updateDichotomyIntervalLimits(Index<?, SingleValueDichotomyStep> index) {
        if (index.lowestInvalidStep() != null &&
            (index.lowestInvalidStep().getRight().getReasonInvalid().equals(ReasonInvalid.UNSECURE_AFTER_VALIDATION)
                || index.lowestInvalidStep().getRight().getReasonInvalid().equals(ReasonInvalid.VALIDATION_FAILED))) {
            lowestUnsecureStep = index.lowestInvalidStep();
        }
        if (index.highestValidStep() != null) {
            highestSecureStep = index.highestValidStep();
        }

        if (index.lowestInvalidStep() != null && index.lowestInvalidStep().getRight().getReasonInvalid().equals(ReasonInvalid.GLSK_LIMITATION)) {
            if (index.lowestInvalidStep().getLeft().value() < referenceExchange) {
                closestGlskLimitationBelowReference = index.lowestInvalidStep();
            } else {
                closestGlskLimitationAboveReference = index.lowestInvalidStep();
            }
        }
        highestAdmissibleStep = getHighestAdmissibleStep(closestGlskLimitationBelowReference, highestSecureStep);
        lowestInadmissibleStep = getLowestInAdmissibleStep(lowestUnsecureStep, closestGlskLimitationAboveReference);
    }

    private Pair<SingleValueDichotomyStep, ? extends DichotomyStepResult<?>> getHighestAdmissibleStep(Pair<SingleValueDichotomyStep, ? extends DichotomyStepResult<?>> closestGlskLimitationBelowReference, Pair<SingleValueDichotomyStep, ? extends DichotomyStepResult<?>> highestSecureStep) {
        return testAndGetStep(closestGlskLimitationBelowReference, highestSecureStep, (t, u) -> t > u);
    }

    private Pair<SingleValueDichotomyStep, ? extends DichotomyStepResult<?>> getLowestInAdmissibleStep(Pair<SingleValueDichotomyStep, ? extends DichotomyStepResult<?>> lowestUnsecureStep, Pair<SingleValueDichotomyStep, ? extends DichotomyStepResult<?>> closestGlskLimitationAboveReference) {
        return testAndGetStep(lowestUnsecureStep, closestGlskLimitationAboveReference, (t, u) -> t < u);
    }

    private Pair<SingleValueDichotomyStep, ? extends DichotomyStepResult<?>> testAndGetStep(Pair<SingleValueDichotomyStep, ? extends DichotomyStepResult<?>> step1, Pair<SingleValueDichotomyStep, ? extends DichotomyStepResult<?>> step2, BiPredicate<Double, Double> biPredicate) {
        if (step1 == null && step2 == null) {
            return null;
        } else if (step1 == null) {
            return step2;
        } else if (step2 == null) {
            return step1;
        } else { // step1 && step2 are != null
            if (biPredicate.test(step1.getLeft().value(), step2.getLeft().value())) {
                return step1;
            } else {
                return step2;
            }
        }
    }

}
