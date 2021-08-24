/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy;

import com.farao_community.farao.data.rao_result_api.ComputationStatus;
import com.farao_community.farao.data.rao_result_api.OptimizationState;
import com.farao_community.farao.data.rao_result_api.RaoResult;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class RaoStepResult implements StepResult {
    private final double stepValue;
    private final RaoResult raoResult;

    public RaoStepResult(double stepValue, RaoResult raoResult) {
        this.stepValue = stepValue;
        this.raoResult = raoResult;
    }

    @Override
    public boolean isSecure() {
        if (raoResult.getComputationStatus() == ComputationStatus.FAILURE) {
            return false;
        }
        return raoResult.getFunctionalCost(OptimizationState.AFTER_CRA) <= 0;
    }

    @Override
    public double stepValue() {
        return stepValue;
    }
}
