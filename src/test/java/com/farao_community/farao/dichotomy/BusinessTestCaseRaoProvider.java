/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy;

import com.farao_community.farao.data.crac_result_extensions.CracResult;
import com.farao_community.farao.data.crac_result_extensions.CracResultExtension;
import com.farao_community.farao.rao_api.RaoInput;
import com.farao_community.farao.rao_api.RaoParameters;
import com.farao_community.farao.rao_api.RaoProvider;
import com.farao_community.farao.rao_api.RaoResult;
import com.google.auto.service.AutoService;

import java.util.concurrent.CompletableFuture;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
@AutoService(RaoProvider.class)
public class BusinessTestCaseRaoProvider implements RaoProvider {
    public static class MyCracResultExtension extends CracResultExtension {
        private final MyCracResult myCracResult;

        public MyCracResultExtension(MyCracResult myCracResult) {
            this.myCracResult = myCracResult;
        }

        @Override
        public CracResult getVariant(String variantId) {
            return myCracResult;
        }
    }

    public static class MyCracResult extends CracResult {
        private final NetworkSecurityStatus networkSecurityStatus;

        public MyCracResult(NetworkSecurityStatus networkSecurityStatus) {
            this.networkSecurityStatus = networkSecurityStatus;
        }

        @Override
        public NetworkSecurityStatus getNetworkSecurityStatus() {
            return networkSecurityStatus;
        }
    }

    @Override
    public CompletableFuture<RaoResult> run(RaoInput raoInput, RaoParameters raoParameters) {
        if (raoInput.getNetwork().getLoad("Load Italy").getP0() < 6000) {
            raoInput.getCrac().addExtension(CracResultExtension.class, new MyCracResultExtension(new MyCracResult(CracResult.NetworkSecurityStatus.SECURED)));
        } else {
            raoInput.getCrac().addExtension(CracResultExtension.class, new MyCracResultExtension(new MyCracResult(CracResult.NetworkSecurityStatus.UNSECURED)));
        }
        return CompletableFuture.completedFuture(new RaoResult(RaoResult.Status.SUCCESS));
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
