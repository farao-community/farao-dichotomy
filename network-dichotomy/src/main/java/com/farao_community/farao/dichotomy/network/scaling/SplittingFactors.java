/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy.network.scaling;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public class SplittingFactors implements ShiftDispatcher {
    private final Map<String, Double> factors;

    public SplittingFactors(Map<String, Double> factors) {
        this.factors = factors;
    }

    @Override
    public final Map<String, Double> dispatch(double value) {
        return factors.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> e.getValue() * value
                ));
    }
}
