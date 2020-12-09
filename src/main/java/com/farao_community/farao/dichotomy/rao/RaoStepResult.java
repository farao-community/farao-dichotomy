/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy.rao;

import com.farao_community.farao.data.crac_api.Crac;
import com.farao_community.farao.data.crac_result_extensions.CracResult;
import com.farao_community.farao.data.crac_result_extensions.CracResultExtension;
import com.farao_community.farao.dichotomy.StepResult;
import com.farao_community.farao.rao_api.RaoResult;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class RaoStepResult implements StepResult {
    private final double stepValue;
    private final Crac crac;
    private final RaoResult raoResult;

    public RaoStepResult(double stepValue, Crac crac, RaoResult raoResult) {
        this.stepValue = stepValue;
        this.crac = crac;
        this.raoResult = raoResult;
    }

    @Override
    public boolean isSecure() {
        if (!raoResult.isSuccessful() || crac.getExtension(CracResultExtension.class) == null) {
            return false;
        }
        CracResult cracResult = crac.getExtension(CracResultExtension.class).getVariant(raoResult.getPostOptimVariantId());
        return cracResult.getNetworkSecurityStatus() == CracResult.NetworkSecurityStatus.SECURED;
    }

    @Override
    public double stepValue() {
        return stepValue;
    }
}
