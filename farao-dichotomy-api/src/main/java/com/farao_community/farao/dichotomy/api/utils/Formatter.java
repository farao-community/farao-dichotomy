/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy.api.utils;

import java.util.Locale;

/**
 * @author Theo Pascoli {@literal <theo.pascoli at rte-france.com>}
 */
public final class Formatter {

    private Formatter() {

    }

    public static String formatDoubleDecimals(double value) {
        if (value % 1 == 0) {
            return String.format(Locale.US,"%d", (int) value);
        } else {
            return String.format(Locale.US,"%.2f", value);
        }
    }
}
