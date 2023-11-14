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

        Map<String, SingleDichotomyVariable> newValues = Map.of(frEsIndexName, new SingleDichotomyVariable(computeNextValue(index, frEsIndexName)),
            ptEsIndexName, new SingleDichotomyVariable(computeNextValue(index, ptEsIndexName)));

        return new MultipleDichotomyVariables(newValues);
    }

    double computeNextValue(Index<?, MultipleDichotomyVariables> index, String key) {
        Set<Double> safeVariableValues = index.testedSteps().stream().filter(
            pair -> isSafeForBorder(pair.getRight().getRaoResult(), key)
        ).map(p -> p.getLeft().values().get(key).value()).collect(Collectors.toSet());
        double maxSafeValue = safeVariableValues.stream().mapToDouble(Double::doubleValue).max().orElseThrow();
        Set<Double> unsafeVariableValues = index.testedSteps().stream().map(p -> p.getLeft().values().get(key).value()).collect(Collectors.toSet());
        unsafeVariableValues.removeAll(safeVariableValues);
        double minUnsafeValue = unsafeVariableValues.stream().mapToDouble(Double::doubleValue).min().orElseThrow();
        if (Math.abs(maxSafeValue - minUnsafeValue) < index.precision()) {
            return maxSafeValue;
        }
        return (maxSafeValue + minUnsafeValue) / 2;
    }


    boolean isSafeForBorder(RaoResult raoResult, String key) {
        // TODO
        if (key.equals(frEsIndexName)) {
            return isSafeForFrEs(raoResult);
        }
        if (key.equals(ptEsIndexName)) {
            return isSafeForPtEs(raoResult);
        }
        return false; // TODO : throw
    }

    boolean isSafeForFrEs(RaoResult raoResult) {
        // TODO
        return true;
    }

    boolean isSafeForPtEs(RaoResult raoResult) {
        // TODO
        return true;
    }


}
