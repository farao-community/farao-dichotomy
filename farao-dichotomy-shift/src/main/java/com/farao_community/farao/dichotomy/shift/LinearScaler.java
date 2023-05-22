/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy.shift;

import com.farao_community.farao.dichotomy.api.NetworkShifter;
import com.farao_community.farao.dichotomy.api.exceptions.GlskLimitationException;
import com.farao_community.farao.dichotomy.api.exceptions.ShiftingException;
import com.powsybl.glsk.commons.ZonalData;
import com.powsybl.iidm.modification.scalable.Scalable;
import com.powsybl.iidm.modification.scalable.ScalingParameters;
import com.powsybl.iidm.network.Network;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import static com.farao_community.farao.dichotomy.api.logging.DichotomyLoggerProvider.*;

/**
 * This final implementation of network validation strategy use basic scaling strategy for network shifting. According
 * to a set of {@link Scalable} defined by {@link ZonalData} it performs a simple shift -- without balancing -- on the
 * network. The way to define shift amounts per zonal data -- how to dispatch the shift between areas from step
 * value -- is handled by {@link ShiftDispatcher}.
 *
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public final class LinearScaler implements NetworkShifter {
    private static final double DEFAULT_EPSILON = 1e-3;

    private final ZonalData<Scalable> zonalScalable;
    private final ShiftDispatcher shiftDispatcher;
    private final double shiftEpsilon;

    public LinearScaler(ZonalData<Scalable> zonalScalable, ShiftDispatcher shiftDispatcher) {
        this(zonalScalable, shiftDispatcher, DEFAULT_EPSILON);
    }

    public LinearScaler(ZonalData<Scalable> zonalScalable, ShiftDispatcher shiftDispatcher, double shiftEpsilon) {
        this.zonalScalable = zonalScalable;
        this.shiftDispatcher = shiftDispatcher;
        this.shiftEpsilon = shiftEpsilon;
    }

    @Override
    public void shiftNetwork(double stepValue, Network network) throws GlskLimitationException, ShiftingException {
        BUSINESS_LOGS.info(String.format("Starting linear scaling on network %s with step value %.2f",
            network.getVariantManager().getWorkingVariantId(), stepValue));
        Map<String, Double> scalingValuesByCountry = shiftDispatcher.dispatch(stepValue);
        List<String> limitingCountries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : scalingValuesByCountry.entrySet()) {
            String zoneId = entry.getKey();
            double asked = entry.getValue();
            BUSINESS_LOGS.info(String.format("Applying variation on zone %s (target: %.2f)", zoneId, asked));
            ScalingParameters iterativeScalingParameters = new ScalingParameters();
            iterativeScalingParameters.setIterative(true);
            double done = zonalScalable.getData(zoneId).scale(network, asked, iterativeScalingParameters);
            if (Math.abs(done - asked) > shiftEpsilon) {
                BUSINESS_WARNS.warn(String.format("Incomplete variation on zone %s (target: %.2f, done: %.2f)",
                    zoneId, asked, done));
                limitingCountries.add(zoneId);
            }
        }
        if (!limitingCountries.isEmpty()) {
            StringJoiner sj = new StringJoiner(", ", "There are Glsk limitation(s) in ", ".");
            limitingCountries.forEach(sj::add);
            throw new GlskLimitationException(sj.toString());
        }
    }
}
