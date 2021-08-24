/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy;

import com.farao_community.farao.data.rao_result_api.RaoResult;
import com.farao_community.farao.rao_api.RaoInput;
import com.farao_community.farao.rao_api.RaoProvider;
import com.farao_community.farao.rao_api.parameters.RaoParameters;
import com.google.auto.service.AutoService;

import java.util.concurrent.CompletableFuture;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
@AutoService(RaoProvider.class)
public class BusinessTestCaseRaoProvider implements RaoProvider {

    @Override
    public CompletableFuture<RaoResult> run(RaoInput raoInput, RaoParameters raoParameters) {
        RaoResult raoResult;
        if (raoInput.getNetwork().getLoad("Load Italy").getP0() < 6000) {
            raoResult = new BusinessTestCaseRaoResult(BusinessTestCaseRaoResult.Status.SECURED);
        } else {
            raoResult = new BusinessTestCaseRaoResult(BusinessTestCaseRaoResult.Status.UNSECURED);
        }
        return CompletableFuture.completedFuture(raoResult);
    }

    @Override
    public String getName() {
        return "TestCaseRao";
    }

    @Override
    public String getVersion() {
        return "v0";
    }
}
