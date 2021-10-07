/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy.api;

/**
 * Interface of a validation step result. It may be implemented to store extra data
 * based on the actual validation process that is run in the ValidationStrategy implementation.
 * A default implementation is provided that may be extended.
 *
 * @see DefaultStepResult
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public interface StepResult {

    boolean isValid();

    double stepValue();
}
