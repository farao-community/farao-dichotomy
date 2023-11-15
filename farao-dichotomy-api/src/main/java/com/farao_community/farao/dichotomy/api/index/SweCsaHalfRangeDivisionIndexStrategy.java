/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy.api.index;

import com.farao_community.farao.data.rao_result_api.RaoResult;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of IndexStrategy that consists of a basic dichotomy between minimum index value and maximum one, on
 * FR-ES and PT-ES borders.
 * When one of the borders is secured with a good precision, dichotomy continues on the other border until that one
 * is secured too.
 *
 * @author Peter Mitri {@literal <peter.mitri at rte-france.com>}
 */
// TODO : migrate this to gridcapa-swe-csa
// careful : min and max values are related to exchange values, which is equal to initial exchange - countertrading
// so when initializing index, min value should be 0 and max value should be initial exchange value
public class SweCsaHalfRangeDivisionIndexStrategy extends HalfRangeDivisionIndexStrategy<MultipleDichotomyVariables> {
    private final String frEsIndexName;
    private final String ptEsIndexName;

    public SweCsaHalfRangeDivisionIndexStrategy(String frEsIndexName, String ptEsIndexName) {
        super(false);
        this.frEsIndexName = frEsIndexName;
        this.ptEsIndexName = ptEsIndexName;
    }

    @Override
    public MultipleDichotomyVariables nextValue(Index<?, MultipleDichotomyVariables> index) {
        if (precisionReached(index)) {
            throw new AssertionError("Dichotomy engine should not ask for next value if precision is reached");
        }
        if (index.lowestInvalidStep() == null) {
            return index.maxValue(); // minimum counter-trading, maximum exchange
        }
        if (index.highestValidStep() == null) {
            return index.minValue(); // maximum counter-trading, minimum exchange
        }

        Map<String, Double> newValues = Map.of(
            frEsIndexName, computeNextValue(index, frEsIndexName),
            ptEsIndexName, computeNextValue(index, ptEsIndexName)
        );

        return new MultipleDichotomyVariables(newValues);
    }

    double computeNextValue(Index<?, MultipleDichotomyVariables> index, String key) {
        // Fetch tested exchange values for this border, for which the border is secure
        Set<Double> safeVariableValues = index.testedSteps().stream().filter(
            pair -> isSafeForBorder(pair.getRight().getRaoResult(), key)
        ).map(p -> p.getLeft().values().get(key).value()).collect(Collectors.toSet());
        // Compute max
        double maxSafeValue = safeVariableValues.stream().mapToDouble(Double::doubleValue).max().orElseThrow();
        // Deduce tested values that are unsafe
        Set<Double> unsafeVariableValues = index.testedSteps().stream().map(p -> p.getLeft().values().get(key).value()).collect(Collectors.toSet());
        unsafeVariableValues.removeAll(safeVariableValues);
        // Compute min
        double minUnsafeValue = unsafeVariableValues.stream().mapToDouble(Double::doubleValue).min().orElseThrow();
        // If precision is reached, keep max value
        if (Math.abs(maxSafeValue - minUnsafeValue) < index.precision()) {
            return maxSafeValue;
        }
        // Else, return average
        return (maxSafeValue + minUnsafeValue) / 2;
    }


    boolean isSafeForBorder(RaoResult raoResult, String key) {
        if (key.equals(frEsIndexName)) {
            return isSafeForFrEs(raoResult);
        }
        if (key.equals(ptEsIndexName)) {
            return isSafeForPtEs(raoResult);
        }
        return false; // TODO : throw
    }

    boolean isSafeForFrEs(RaoResult raoResult) {
        // TODO implement this
        return true;
    }

    boolean isSafeForPtEs(RaoResult raoResult) {
        // TODO implement this
        return true;
    }


}
