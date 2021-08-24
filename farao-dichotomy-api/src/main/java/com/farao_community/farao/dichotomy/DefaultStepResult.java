/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy;

/**
 * Default implementation of StepResult interface.
 *
 * @see StepResult
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class DefaultStepResult implements StepResult {
    private final boolean isSecure;
    private final double stepValue;

    public DefaultStepResult(boolean isSecure, double stepValue) {
        this.isSecure = isSecure;
        this.stepValue = stepValue;
    }

    @Override
    public boolean isSecure() {
        return isSecure;
    }

    @Override
    public double stepValue() {
        return stepValue;
    }
}
