/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy.api.index;

import com.farao_community.farao.dichotomy.api.results.DichotomyStepResult;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Implementation of IndexStrategy that consists of a basic dichotomy between minimum index value and maximum one.
 * First, it will validate minimum or maximum value (depending on startWitnMin value) and then validate recursively
 * the middle of the dichotomy interval.
 * I.E. if startWithMin is true then we start with min value then (min + max) / 2 value
 * if startWithMin is false then we start with (min + max) / 2 value
 *
 * @author Marc Schwitzguebel {@literal <marc.schwitzguebel at rte-france.com>}
 */
public class HalfRangeDivisionIndexStrategy<T> implements IndexStrategy<T> {

    private final boolean startWithMin;

    public HalfRangeDivisionIndexStrategy(boolean startWithMin) {
        this.startWithMin = startWithMin;
    }

    @Override
    public double nextValue(Index<T> index) {
        if (precisionReached(index)) {
            throw new AssertionError("Dichotomy engine should not ask for next value if precision is reached");
        }
        final double maxValue = index.maxValue();
        final double minValue = index.minValue();
        final Pair<Double, DichotomyStepResult<T>> highestValidStep = index.highestValidStep();
        final Pair<Double, DichotomyStepResult<T>> lowestValidStep = index.lowestInvalidStep();
        if (startWithMin) {
            if (highestValidStep == null) {
                return minValue;
            }
            if (lowestValidStep == null) {
                return mid(highestValidStep.getLeft(), maxValue);
            }
        } else {
            //coreso request for swe process: start the dichotomy with (max + min) /2
            if (lowestValidStep == null && highestValidStep == null) {
                return mid(minValue, maxValue);
            }
            if (lowestValidStep == null && highestValidStep != null) {
                return mid(highestValidStep.getLeft(), maxValue);
            }
            if (highestValidStep == null) {
                return mid(minValue, lowestValidStep.getLeft());
            }
        }
        return index.meanOfStepVoltages();
    }

    private double mid(double min, double max) {
        return (min + max) / 2.0;
    }

    @Override
    public boolean precisionReached(Index<T> index) {
        if (index.isInBounds()) {
            return true;
        }
        if (index.lowestInvalidStep() != null && index.highestValidStep() == null) {
            return Math.abs(index.lowestInvalidStep().getLeft() - index.minValue()) <= index.precision();
        }
        if (index.lowestInvalidStep() == null) {
            return false;
        }
        return index.isWithinPrecision();
    }
}
