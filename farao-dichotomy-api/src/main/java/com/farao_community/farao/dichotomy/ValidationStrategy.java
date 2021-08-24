/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy;

/**
 * Interface responsible actual validation of a dichotomy index value.
 * Each implementation is responsible of defining the method that ensure, given a specific dichotomy index value,
 * if it is considered as secure or not. It can provide as an output a dedicated StepResult implementation to embed
 * extra information
 *
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public interface ValidationStrategy<T extends StepResult> {
    T validateStep(double stepValue);
}
