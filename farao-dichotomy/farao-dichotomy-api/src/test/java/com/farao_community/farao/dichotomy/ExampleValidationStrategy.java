/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy;

/**
 * Example hard-coded validation strategy
 *
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
class ExampleValidationStrategy implements ValidationStrategy<DefaultStepResult> {
    private final double limit;

    public ExampleValidationStrategy(double limit) {
        this.limit = limit;
    }

    @Override
    public DefaultStepResult validateStep(double stepValue) {
        return new DefaultStepResult(stepValue < limit, stepValue);
    }
}
