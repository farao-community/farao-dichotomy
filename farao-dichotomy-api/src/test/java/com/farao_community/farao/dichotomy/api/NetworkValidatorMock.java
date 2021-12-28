/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy.api;

import com.farao_community.farao.dichotomy.api.exceptions.ValidationException;
import com.farao_community.farao.dichotomy.api.results.DichotomyStepResult;
import com.powsybl.iidm.network.Network;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public class NetworkValidatorMock implements NetworkValidator<Object> {
    private final double limit;

    public NetworkValidatorMock(double limit) {
        this.limit = limit;
    }

    @Override
    public DichotomyStepResult<Object> validateNetwork(Network network) throws ValidationException {
        String[] parsedVariantId = network.getVariantManager().getWorkingVariantId().split("-");
        double stepValue = Double.parseDouble(parsedVariantId[parsedVariantId.length - 1]);
        if (parsedVariantId[parsedVariantId.length - 2].equals("")) {
            stepValue *= -1;
        }
        if (stepValue < limit) {
            return DichotomyStepResult.fromNetworkValidationResult(new RaoResultMock(true), null);
        } else {
            return DichotomyStepResult.fromNetworkValidationResult(new RaoResultMock(false), null);
        }
    }
}
