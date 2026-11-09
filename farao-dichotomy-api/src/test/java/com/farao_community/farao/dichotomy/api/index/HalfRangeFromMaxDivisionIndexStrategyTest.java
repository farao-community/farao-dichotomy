/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy.api.index;

import com.farao_community.farao.dichotomy.api.results.DichotomyStepResult;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.AssertionsForClassTypes.offset;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HalfRangeFromMaxDivisionIndexStrategyTest {

    private final Index<Object> index = mock(Index.class);
    private final HalfRangeFromMaxDivisionIndexStrategy<Object> strategy = new HalfRangeFromMaxDivisionIndexStrategy<>();

    @BeforeEach
    void init() {
        Mockito.reset(index);
    }

    @Test
    void nextValueMaxTest() {
        when(index.minValue()).thenReturn(0.0);
        when(index.maxValue()).thenReturn(1.0);
        Assertions.assertThat(strategy.nextValue(index)).isEqualTo(1.0);
    }

    @Test
    void nextValueWithLowestInvalidTest() {
        when(index.minValue()).thenReturn(0.0);
        when(index.maxValue()).thenReturn(1.0);
        when(index.lowestInvalidStep()).thenReturn(Pair.of(0.5, null));
        Assertions.assertThat(strategy.nextValue(index)).isEqualTo(0.25);

    }

    @Test
    void nextValueWithLowestInvalidAndHighestValidTest() {
        final Index<Object> index2 = new Index<>(0.0, 1.0, 0.0125);
        DichotomyStepResult<Object> stepInvalid = mock(DichotomyStepResult.class);
        DichotomyStepResult<Object> stepValid = mock(DichotomyStepResult.class);
        when(stepInvalid.isValid()).thenReturn(false);
        when(stepValid.isValid()).thenReturn(true);
        index2.addDichotomyStepResult(0.8, stepInvalid);
        index2.addDichotomyStepResult(0.4, stepValid);
        Assertions.assertThat(strategy.nextValue(index2)).isEqualTo(0.60, offset(0.001));

    }

    @Test
    void precisionReachedInBoundsTest() {
        when(index.isInBounds()).thenReturn(true);
        Assertions.assertThat(strategy.precisionReached(index)).isTrue();
    }

    @Test
    void precisionReachedInPrecisionTest() {
        when(index.isInBounds()).thenReturn(false);
        when(index.isWithinPrecision()).thenReturn(true);
        Assertions.assertThat(strategy.precisionReached(index)).isTrue();
    }

    @Test
    void precisionReachedOutOfPrecisionTest() {
        when(index.isInBounds()).thenReturn(false);
        when(index.isWithinPrecision()).thenReturn(false);
        Assertions.assertThat(strategy.precisionReached(index)).isFalse();
    }

    @Test
    void precisionReachedPrecisionReachedTest() {
        when(index.isInBounds()).thenReturn(false);
        when(index.lowestInvalidStep()).thenReturn(Pair.of(0.1, null));
        when(index.minValue()).thenReturn(0.0);
        when(index.precision()).thenReturn(0.125);
        Assertions.assertThat(strategy.precisionReached(index)).isTrue();
    }

    @Test
    void precisionReachedPrecisionNotReachedTest() {
        when(index.isInBounds()).thenReturn(false);
        when(index.lowestInvalidStep()).thenReturn(Pair.of(0.3, null));
        when(index.minValue()).thenReturn(0.0);
        when(index.precision()).thenReturn(0.125);
        Assertions.assertThat(strategy.precisionReached(index)).isFalse();
    }
}
