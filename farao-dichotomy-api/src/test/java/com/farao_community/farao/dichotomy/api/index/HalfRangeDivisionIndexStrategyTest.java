/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy.api.index;

/**
 * @author Marc Schwitzguebel {@literal <marc.schwitzguebel at rte-france.com>}
 */
import com.farao_community.farao.dichotomy.api.results.DichotomyStepResult;
import com.farao_community.farao.dichotomy.api.results.RaoResultMock;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HalfRangeDivisionIndexStrategyTest {

    private static final double MIN_VALUE = 200.0;
    private static final double MAX_VALUE = 6400.0;
    private static final double PRECISION = 50.0;

    @Test
    void nextValueStartWithMinTest() {
        Index<Boolean> index = new Index<>(MIN_VALUE, MAX_VALUE, PRECISION);
        HalfRangeDivisionIndexStrategy strategy = new HalfRangeDivisionIndexStrategy(true);
        assertEquals(MIN_VALUE, strategy.nextValue(index), PRECISION);
        // if min is insecure the process stops otherwise we have a lowestValid
        DichotomyStepResult<Boolean> result = DichotomyStepResult.fromNetworkValidationResult(new RaoResultMock(true), true);
        index.addDichotomyStepResult(MIN_VALUE, result);
        assertEquals((MIN_VALUE + MAX_VALUE) / 2, strategy.nextValue(index), PRECISION);
        //we now have two options either valid or not: here we test valid
        DichotomyStepResult<Boolean> result2 = DichotomyStepResult.fromNetworkValidationResult(new RaoResultMock(true), true);
        index.addDichotomyStepResult((MIN_VALUE + MAX_VALUE) / 2, result2);
        assertEquals(((MIN_VALUE + MAX_VALUE) / 2 + MAX_VALUE) / 2, strategy.nextValue(index), PRECISION);
    }

    @Test
    void nextValueStartWithMinTest2() {
        Index<Boolean> index = new Index<>(MIN_VALUE, MAX_VALUE, PRECISION);
        HalfRangeDivisionIndexStrategy strategy = new HalfRangeDivisionIndexStrategy(true);
        assertEquals(MIN_VALUE, strategy.nextValue(index), PRECISION);
        // if min is insecure the process stops otherwise we have a lowestValid
        DichotomyStepResult<Boolean> result = DichotomyStepResult.fromNetworkValidationResult(new RaoResultMock(true), true);
        index.addDichotomyStepResult(MIN_VALUE, result);
        assertEquals((MIN_VALUE + MAX_VALUE) / 2, strategy.nextValue(index), PRECISION);
        //we now have two options either valid or not: here we test invalid
        DichotomyStepResult<Boolean> result2 = DichotomyStepResult.fromNetworkValidationResult(new RaoResultMock(false), false);
        index.addDichotomyStepResult((MIN_VALUE + MAX_VALUE) / 2, result2);
        assertEquals(((MIN_VALUE + MAX_VALUE) / 2 + MIN_VALUE) / 2, strategy.nextValue(index), PRECISION);
    }

    @Test
    void nextValueStartWithMaxTest() {
        Index<Boolean> index = new Index<>(MIN_VALUE, MAX_VALUE, PRECISION);
        HalfRangeDivisionIndexStrategy strategy = new HalfRangeDivisionIndexStrategy(false);
        assertEquals(MAX_VALUE, strategy.nextValue(index), PRECISION);
        // if max is secure the process stops otherwise we have a highesInvalid
        DichotomyStepResult<Boolean> result = DichotomyStepResult.fromNetworkValidationResult(new RaoResultMock(false), false);
        index.addDichotomyStepResult(MAX_VALUE, result);
        assertEquals((MIN_VALUE + MAX_VALUE) / 2, strategy.nextValue(index), PRECISION);
        //we now have two options either valid or not: here we test valid
        DichotomyStepResult<Boolean> result2 = DichotomyStepResult.fromNetworkValidationResult(new RaoResultMock(true), true);
        index.addDichotomyStepResult((MIN_VALUE + MAX_VALUE) / 2, result2);
        assertEquals(((MIN_VALUE + MAX_VALUE) / 2 + MAX_VALUE) / 2, strategy.nextValue(index), PRECISION);
    }

    @Test
    void nextValueStartWithMaxTest2() {
        Index<Boolean> index = new Index<>(MIN_VALUE, MAX_VALUE, PRECISION);
        HalfRangeDivisionIndexStrategy strategy = new HalfRangeDivisionIndexStrategy(false);
        assertEquals(MAX_VALUE, strategy.nextValue(index), PRECISION);
        // if  max is secure the process stops otherwise we have a highesInvalid
        DichotomyStepResult<Boolean> result = DichotomyStepResult.fromNetworkValidationResult(new RaoResultMock(false), false);
        index.addDichotomyStepResult(MAX_VALUE, result);
        assertEquals((MIN_VALUE + MAX_VALUE) / 2, strategy.nextValue(index), PRECISION);
        //we now have two options either valid or not: here we test invalid
        DichotomyStepResult<Boolean> result2 = DichotomyStepResult.fromNetworkValidationResult(new RaoResultMock(false), false);
        index.addDichotomyStepResult((MIN_VALUE + MAX_VALUE) / 2, result2);
        assertEquals(((MIN_VALUE + MAX_VALUE) / 2 + MIN_VALUE) / 2, strategy.nextValue(index), PRECISION);
    }
}
