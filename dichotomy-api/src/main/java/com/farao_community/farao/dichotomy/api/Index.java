/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Object responsible of storing the dichotomy index state during dichotomy calculation.
 * It stores the results of previously validated steps.
 * An important assumption for Dichotomy engine is that minimum value is supposed to be secure
 * and maximum value is supposed to be unsecure. More precisely, if minimum value is unsecure, the algorithm
 * suppose that the dichotomy is finished (same if maximum value is secure).
 *
 * This implementation should be enough for any dichotomy usage.
 *
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class Index<T extends StepResult> {
    private static final double EPSILON = 1e-3;
    private final double minValue;
    private final double maxValue;
    private final double precision;
    private final List<T> stepResults = new ArrayList<>();
    private T higherValidStep;
    private T lowerInvalidStep;

    public Index(double minValue, double maxValue, double precision) {
        if (minValue > maxValue) {
            throw new DichotomyException("Index creation impossible, minValue is supposed to be lower than maxValue.");
        }
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.precision = precision;
    }

    public double minValue() {
        return minValue;
    }

    public double maxValue() {
        return maxValue;
    }

    public double precision() {
        return precision;
    }

    public T higherValidStep() {
        return higherValidStep;
    }

    public T lowerInvalidStep() {
        return lowerInvalidStep;
    }

    public List<T> testedSteps() {
        return new ArrayList<>(stepResults);
    }

    void addDichotomyStepResult(T stepResult) {
        if (stepResult.isValid()) {
            if (higherValidStep != null && higherValidStep.stepValue() > stepResult.stepValue()) {
                throw new AssertionError("Step result tested is secure but its value is lower than higher secure step one. Should not happen");
            }
            higherValidStep = stepResult;
        } else if (!stepResult.isValid()) {
            if (lowerInvalidStep != null && lowerInvalidStep.stepValue() < stepResult.stepValue()) {
                throw new AssertionError("Step result tested is unsecure but its value is higher than lower unsecure step one. Should not happen");
            }
            lowerInvalidStep = stepResult;
        }
        stepResults.add(stepResult);
    }

    boolean precisionReached() {
        if (lowerInvalidStep != null && Math.abs(lowerInvalidStep.stepValue() - minValue) < EPSILON) {
            return true;
        }
        if (higherValidStep != null && Math.abs(higherValidStep.stepValue() - maxValue) < EPSILON) {
            return true;
        }
        if (lowerInvalidStep == null || higherValidStep == null) {
            return false;
        }
        return Math.abs(higherValidStep.stepValue() - lowerInvalidStep.stepValue()) < precision;
    }
}
