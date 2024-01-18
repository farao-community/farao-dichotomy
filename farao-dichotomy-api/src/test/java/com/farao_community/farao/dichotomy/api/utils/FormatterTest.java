/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy.api.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Theo Pascoli {@literal <theo.pascoli at rte-france.com>}
 */
class FormatterTest {

    @Test
    void testFormatDoubleWithDecimals() {
        assertEquals("123,46", Formatter.formatDoubleDecimals(123.456));
    }

    @Test
    void testFormatDoubleWithoutDecimals() {
        assertEquals("123", Formatter.formatDoubleDecimals(123.00));
    }

    @Test
    void testFormatDoubleWithExactDecimals() {
        assertEquals("123,50", Formatter.formatDoubleDecimals(123.50));
    }

    @Test
    void testFormatDoubleWithZero() {
        assertEquals("0", Formatter.formatDoubleDecimals(0.0));
    }
}
