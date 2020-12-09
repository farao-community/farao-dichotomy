/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
class DichotomyEngineTest {
    private static final double EPSILON = 1e-3;

    @Test
    public void checkRangeDivisionIndexStrategyStartingWithMin() {
        double limit = -340;
        double minValue = -1000;
        double maxValue = 1000;
        double precision = 200;
        Index<DefaultStepResult> index = new Index<>(minValue, maxValue, precision);
        IndexStrategy indexStrategy = new RangeDivisionIndexStrategy(true);
        ValidationStrategy<DefaultStepResult> validationStrategy = new ExampleValidationStrategy(limit);
        DichotomyEngine<DefaultStepResult> engine = new DichotomyEngine<>(index, indexStrategy, validationStrategy);
        engine.run();

        assertTrue(index.higherSecureStep().isSecure());
        assertEquals(-375, index.higherSecureStep().stepValue(), EPSILON);
        assertFalse(index.lowerUnsecureStep().isSecure());
        assertEquals(-250, index.lowerUnsecureStep().stepValue(), EPSILON);

        assertEquals(-1000, index.testedSteps().get(0).stepValue(), EPSILON);
        assertTrue(index.testedSteps().get(0).isSecure());
        assertEquals(1000, index.testedSteps().get(1).stepValue(), EPSILON);
        assertFalse(index.testedSteps().get(1).isSecure());
        assertEquals(0, index.testedSteps().get(2).stepValue(), EPSILON);
        assertFalse(index.testedSteps().get(2).isSecure());
        assertEquals(-500, index.testedSteps().get(3).stepValue(), EPSILON);
        assertTrue(index.testedSteps().get(3).isSecure());
        assertEquals(-250, index.testedSteps().get(4).stepValue(), EPSILON);
        assertFalse(index.testedSteps().get(4).isSecure());
        assertEquals(-375, index.testedSteps().get(5).stepValue(), EPSILON);
        assertTrue(index.testedSteps().get(5).isSecure());
    }

    @Test
    public void checkRangeDivisionIndexStrategyStartingWithMax() {
        double limit = -340;
        double minValue = -1000;
        double maxValue = 1000;
        double precision = 200;
        Index<DefaultStepResult> index = new Index<>(minValue, maxValue, precision);
        IndexStrategy indexStrategy = new RangeDivisionIndexStrategy(false);
        ValidationStrategy<DefaultStepResult> validationStrategy = new ExampleValidationStrategy(limit);
        DichotomyEngine<DefaultStepResult> engine = new DichotomyEngine<>(index, indexStrategy, validationStrategy);
        engine.run();

        assertTrue(index.higherSecureStep().isSecure());
        assertEquals(-375, index.higherSecureStep().stepValue(), EPSILON);
        assertFalse(index.lowerUnsecureStep().isSecure());
        assertEquals(-250, index.lowerUnsecureStep().stepValue(), EPSILON);

        assertEquals(1000, index.testedSteps().get(0).stepValue(), EPSILON);
        assertFalse(index.testedSteps().get(0).isSecure());
        assertEquals(-1000, index.testedSteps().get(1).stepValue(), EPSILON);
        assertTrue(index.testedSteps().get(1).isSecure());
        assertEquals(0, index.testedSteps().get(2).stepValue(), EPSILON);
        assertFalse(index.testedSteps().get(2).isSecure());
        assertEquals(-500, index.testedSteps().get(3).stepValue(), EPSILON);
        assertTrue(index.testedSteps().get(3).isSecure());
        assertEquals(-250, index.testedSteps().get(4).stepValue(), EPSILON);
        assertFalse(index.testedSteps().get(4).isSecure());
        assertEquals(-375, index.testedSteps().get(5).stepValue(), EPSILON);
        assertTrue(index.testedSteps().get(5).isSecure());
    }

    @Test
    public void checkStepsIndexStrategyStartingWithMin() {
        double limit = -340;
        double minValue = -1000;
        double maxValue = 1000;
        double precision = 200;
        double stepsSize = 400;
        Index<DefaultStepResult> index = new Index<>(minValue, maxValue, precision);
        IndexStrategy indexStrategy = new StepsIndexStrategy(true, stepsSize);
        ValidationStrategy<DefaultStepResult> validationStrategy = new ExampleValidationStrategy(limit);
        DichotomyEngine<DefaultStepResult> engine = new DichotomyEngine<>(index, indexStrategy, validationStrategy);
        engine.run();

        assertTrue(index.higherSecureStep().isSecure());
        assertEquals(-400, index.higherSecureStep().stepValue(), EPSILON);
        assertFalse(index.lowerUnsecureStep().isSecure());
        assertEquals(-300, index.lowerUnsecureStep().stepValue(), EPSILON);

        assertEquals(-1000, index.testedSteps().get(0).stepValue(), EPSILON);
        assertTrue(index.testedSteps().get(0).isSecure());
        assertEquals(-600, index.testedSteps().get(1).stepValue(), EPSILON);
        assertTrue(index.testedSteps().get(1).isSecure());
        assertEquals(-200, index.testedSteps().get(2).stepValue(), EPSILON);
        assertFalse(index.testedSteps().get(2).isSecure());
        assertEquals(-400, index.testedSteps().get(3).stepValue(), EPSILON);
        assertTrue(index.testedSteps().get(3).isSecure());
        assertEquals(-300, index.testedSteps().get(4).stepValue(), EPSILON);
        assertFalse(index.testedSteps().get(4).isSecure());
    }

    @Test
    public void checkStepsIndexStrategyStartingWithMax() {
        double limit = -340;
        double minValue = -1000;
        double maxValue = 1000;
        double precision = 200;
        double stepsSize = 400;
        Index<DefaultStepResult> index = new Index<>(minValue, maxValue, precision);
        IndexStrategy indexStrategy = new StepsIndexStrategy(false, stepsSize);
        ValidationStrategy<DefaultStepResult> validationStrategy = new ExampleValidationStrategy(limit);
        DichotomyEngine<DefaultStepResult> engine = new DichotomyEngine<>(index, indexStrategy, validationStrategy);
        engine.run();

        assertTrue(index.higherSecureStep().isSecure());
        assertEquals(-400, index.higherSecureStep().stepValue(), EPSILON);
        assertFalse(index.lowerUnsecureStep().isSecure());
        assertEquals(-300, index.lowerUnsecureStep().stepValue(), EPSILON);

        assertEquals(1000, index.testedSteps().get(0).stepValue(), EPSILON);
        assertFalse(index.testedSteps().get(0).isSecure());
        assertEquals(600, index.testedSteps().get(1).stepValue(), EPSILON);
        assertFalse(index.testedSteps().get(1).isSecure());
        assertEquals(200, index.testedSteps().get(2).stepValue(), EPSILON);
        assertFalse(index.testedSteps().get(2).isSecure());
        assertEquals(-200, index.testedSteps().get(3).stepValue(), EPSILON);
        assertFalse(index.testedSteps().get(3).isSecure());
        assertEquals(-600, index.testedSteps().get(4).stepValue(), EPSILON);
        assertTrue(index.testedSteps().get(4).isSecure());
        assertEquals(-400, index.testedSteps().get(5).stepValue(), EPSILON);
        assertTrue(index.testedSteps().get(5).isSecure());
        assertEquals(-300, index.testedSteps().get(6).stepValue(), EPSILON);
        assertFalse(index.testedSteps().get(6).isSecure());
    }

    @Test
    public void checkDichotomyEngineStopsAtMaxIterations() {
        double limit = -340;
        double minValue = -1000;
        double maxValue = 1000;
        double precision = 200;
        int maxIterations = 5;
        Index<DefaultStepResult> index = new Index<>(minValue, maxValue, precision);
        IndexStrategy indexStrategy = new RangeDivisionIndexStrategy(true);
        ValidationStrategy<DefaultStepResult> validationStrategy = new ExampleValidationStrategy(limit);
        DichotomyEngine<DefaultStepResult> engine = new DichotomyEngine<>(index, indexStrategy, validationStrategy, maxIterations);
        engine.run();

        assertTrue(index.higherSecureStep().isSecure());
        assertEquals(-500, index.higherSecureStep().stepValue(), EPSILON);
        assertFalse(index.lowerUnsecureStep().isSecure());
        assertEquals(-250, index.lowerUnsecureStep().stepValue(), EPSILON);

        assertEquals(-1000, index.testedSteps().get(0).stepValue(), EPSILON);
        assertTrue(index.testedSteps().get(0).isSecure());
        assertEquals(1000, index.testedSteps().get(1).stepValue(), EPSILON);
        assertFalse(index.testedSteps().get(1).isSecure());
        assertEquals(0, index.testedSteps().get(2).stepValue(), EPSILON);
        assertFalse(index.testedSteps().get(2).isSecure());
        assertEquals(-500, index.testedSteps().get(3).stepValue(), EPSILON);
        assertTrue(index.testedSteps().get(3).isSecure());
        assertEquals(-250, index.testedSteps().get(4).stepValue(), EPSILON);
        assertFalse(index.testedSteps().get(4).isSecure());
    }

    @Test
    public void checkAllSecure() {
        double limit = 1500;
        double minValue = -1000;
        double maxValue = 1000;
        double precision = 200;
        Index<DefaultStepResult> index = new Index<>(minValue, maxValue, precision);
        IndexStrategy indexStrategy = new RangeDivisionIndexStrategy(true);
        ValidationStrategy<DefaultStepResult> validationStrategy = new ExampleValidationStrategy(limit);
        DichotomyEngine<DefaultStepResult> engine = new DichotomyEngine<>(index, indexStrategy, validationStrategy);
        engine.run();

        assertTrue(index.higherSecureStep().isSecure());
        assertEquals(1000, index.higherSecureStep().stepValue(), EPSILON);
        assertNull(index.lowerUnsecureStep());

        assertEquals(-1000, index.testedSteps().get(0).stepValue(), EPSILON);
        assertTrue(index.testedSteps().get(0).isSecure());
        assertEquals(1000, index.testedSteps().get(1).stepValue(), EPSILON);
        assertTrue(index.testedSteps().get(1).isSecure());
    }

    @Test
    public void checkAllUnsecure() {
        double limit = -1500;
        double minValue = -1000;
        double maxValue = 1000;
        double precision = 200;
        Index<DefaultStepResult> index = new Index<>(minValue, maxValue, precision);
        IndexStrategy indexStrategy = new RangeDivisionIndexStrategy(true);
        ValidationStrategy<DefaultStepResult> validationStrategy = new ExampleValidationStrategy(limit);
        DichotomyEngine<DefaultStepResult> engine = new DichotomyEngine<>(index, indexStrategy, validationStrategy);
        engine.run();

        assertNull(index.higherSecureStep());
        assertFalse(index.lowerUnsecureStep().isSecure());
        assertEquals(-1000, index.lowerUnsecureStep().stepValue(), EPSILON);

        assertEquals(-1000, index.testedSteps().get(0).stepValue(), EPSILON);
        assertFalse(index.testedSteps().get(0).isSecure());
    }

    @Test
    public void checkThatEngineFailsWhenMaxIterationTooLow() {
        double limit = -1500;
        double minValue = -1000;
        double maxValue = 1000;
        double precision = 200;
        Index<DefaultStepResult> index = new Index<>(minValue, maxValue, precision);
        IndexStrategy indexStrategy = new RangeDivisionIndexStrategy(true);
        ValidationStrategy<DefaultStepResult> validationStrategy = new ExampleValidationStrategy(limit);
        Assertions.assertThrows(DichotomyException.class, () -> new DichotomyEngine<>(index, indexStrategy, validationStrategy, 2));
    }

    @Test
    public void checkThatStepsIndexStrategyCreationFailsWhenStepsSizeNegative() {
        Assertions.assertThrows(DichotomyException.class, () -> new StepsIndexStrategy(true, -10));
        Assertions.assertThrows(DichotomyException.class, () -> new StepsIndexStrategy(true, 0));
    }
}
