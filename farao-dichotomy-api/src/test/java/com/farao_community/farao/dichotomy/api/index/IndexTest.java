/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy.api.index;

import com.farao_community.farao.dichotomy.api.exceptions.DichotomyException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
class IndexTest {
    private static final double EPSILON = 1e-3;

    @Test
    void checkStandardIndexManipulation() {
        Index<?, SingleDichotomyVariable> index = new Index<>(new SingleDichotomyVariable(-1000), new SingleDichotomyVariable(-200), 100);

        assertEquals(-1000, index.minValue().value(), EPSILON);
        assertEquals(-200, index.maxValue().value(), EPSILON);
        assertEquals(100, index.precision(), EPSILON);
    }

    @Test
    void checkIndexCreationFailsIfMinHigherThanMax() {
        Exception e = assertThrows(DichotomyException.class, () -> new Index<>(new SingleDichotomyVariable(-200), new SingleDichotomyVariable(-1000), 100));
        assertEquals("Index creation impossible, minValue is supposed to be lower than maxValue.", e.getMessage());
    }

    @Test
    void checkIndexCreationSucceedsIfPrecisionIsLowerThanSearchInterval() {
        Index<?, SingleDichotomyVariable> index = new Index<>(new SingleDichotomyVariable(0), new SingleDichotomyVariable(100), 300);
        assertEquals(0, index.minValue().value(), EPSILON);
        assertEquals(100, index.maxValue().value(), EPSILON);
        assertEquals(300, index.precision(), EPSILON);
    }
}
