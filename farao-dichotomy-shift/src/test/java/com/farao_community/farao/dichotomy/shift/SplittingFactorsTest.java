/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy.shift;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
class SplittingFactorsTest {
    private static final double DOUBLE_TOLERANCE = 0.001;

    @Test
    void testDispatchValue() {
        SplittingFactors splittingFactors = new SplittingFactors(ImmutableMap.of(
                "FR", 0.25,
                "CH", 0.1,
                "AT", 0.4,
                "SI", 0.25,
                "IT", -1.0
        ));

        Map<String, Double> shifts = splittingFactors.dispatch(100);
        assertEquals(5, shifts.keySet().size());
        assertEquals(25., shifts.get("FR"), DOUBLE_TOLERANCE);
        assertEquals(10., shifts.get("CH"), DOUBLE_TOLERANCE);
        assertEquals(40., shifts.get("AT"), DOUBLE_TOLERANCE);
        assertEquals(25., shifts.get("SI"), DOUBLE_TOLERANCE);
        assertEquals(-100, shifts.get("IT"), DOUBLE_TOLERANCE);
    }
}
