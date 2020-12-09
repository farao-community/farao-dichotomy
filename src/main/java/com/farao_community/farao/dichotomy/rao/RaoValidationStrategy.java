/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy.rao;

import com.farao_community.farao.commons.ZonalData;
import com.farao_community.farao.data.crac_api.Crac;
import com.farao_community.farao.data.crac_io_json.JsonExport;
import com.farao_community.farao.data.crac_io_json.JsonImport;
import com.farao_community.farao.dichotomy.ValidationStrategy;
import com.farao_community.farao.rao_api.Rao;
import com.farao_community.farao.rao_api.RaoInput;
import com.farao_community.farao.rao_api.RaoResult;
import com.powsybl.action.util.Scalable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.xml.NetworkXml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class RaoValidationStrategy implements ValidationStrategy<RaoStepResult> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RaoValidationStrategy.class);
    private final Network network;
    private final Crac crac;
    private final ZonalData<Scalable> scalables;
    private final Map<String, Double> splittingFactors;

    public RaoValidationStrategy(Network network, Crac crac, ZonalData<Scalable> scalables, Map<String, Double> splittingFactors) {
        this.network = network;
        this.crac = crac;
        this.scalables = scalables;
        this.splittingFactors = splittingFactors;
    }

    @Override
    public RaoStepResult validateStep(double stepValue) {
        LOGGER.info("Validating step value: {}", stepValue);
        Network duplicatedNetwork = duplicateNetwork(network);
        Crac duplicatedCrac = duplicateCrac(crac);
        splittingFactors.forEach((zoneId, splittingFactor) -> scalables.getData(zoneId).scale(duplicatedNetwork, stepValue * splittingFactor));
        RaoInput raoInput = RaoInput.build(duplicatedNetwork, duplicatedCrac).build();
        RaoResult raoResult = Rao.run(raoInput);
        return new RaoStepResult(stepValue, duplicatedCrac, raoResult);
    }

    private Network duplicateNetwork(Network network) {
        return NetworkXml.copy(network);
    }

    private Crac duplicateCrac(Crac crac) {
        JsonImport jsonImport = new JsonImport();
        JsonExport jsonExport = new JsonExport();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        jsonExport.exportCrac(crac, baos);
        return jsonImport.importCrac(new ByteArrayInputStream(baos.toByteArray()), null);
    }
}
