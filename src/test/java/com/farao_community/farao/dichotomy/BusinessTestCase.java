/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy;

import com.farao_community.farao.commons.Unit;
import com.farao_community.farao.commons.ZonalData;
import com.farao_community.farao.commons.ZonalDataImpl;
import com.farao_community.farao.data.crac_api.*;
import com.farao_community.farao.data.crac_api.threshold.BranchThresholdRule;
import com.farao_community.farao.data.crac_api.usage_rule.UsageMethod;
import com.farao_community.farao.data.crac_impl.SimpleCrac;
import com.farao_community.farao.data.crac_impl.remedial_action.network_action.Topology;
import com.farao_community.farao.data.crac_impl.usage_rule.FreeToUseImpl;
import com.farao_community.farao.dichotomy.rao.RaoStepResult;
import com.farao_community.farao.dichotomy.rao.RaoValidationStrategy;
import com.powsybl.action.util.Scalable;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
class BusinessTestCase {
    private static final double EPSILON = 1e-3;

    public Network createNetwork() {
        Network network = NetworkFactory.findDefault().createNetwork("Dichotomy test network", "code");
        Substation substationFr = network.newSubstation().setId("Substation France").setCountry(Country.FR).add();
        VoltageLevel voltageLevelFr = substationFr.newVoltageLevel().setId("Voltage level France").setNominalV(400).setTopologyKind(TopologyKind.BUS_BREAKER).add();
        voltageLevelFr.getBusBreakerView().newBus().setId("Bus France").add();
        voltageLevelFr.newGenerator().setId("Generator France").setVoltageRegulatorOn(true).setTargetP(2000).setTargetV(400).setMinP(0).setMaxP(5000).setBus("Bus France").add();

        Substation substationAt = network.newSubstation().setId("Substation Austria").setCountry(Country.AT).add();
        VoltageLevel voltageLevelAt = substationAt.newVoltageLevel().setId("Voltage level Austria").setNominalV(400).setTopologyKind(TopologyKind.BUS_BREAKER).add();
        voltageLevelAt.getBusBreakerView().newBus().setId("Bus Austria").add();
        voltageLevelAt.newGenerator().setId("Generator Austria").setVoltageRegulatorOn(true).setTargetP(1500).setTargetV(400).setMinP(0).setMaxP(5000).setBus("Bus Austria").add();

        Substation substationSi = network.newSubstation().setId("Substation Slovenia").setCountry(Country.SI).add();
        VoltageLevel voltageLevelSi = substationSi.newVoltageLevel().setId("Voltage level Slovenia").setNominalV(400).setTopologyKind(TopologyKind.BUS_BREAKER).add();
        voltageLevelSi.getBusBreakerView().newBus().setId("Bus Slovenia").add();
        voltageLevelSi.newGenerator().setId("Generator Slovenia").setVoltageRegulatorOn(true).setTargetP(1000).setTargetV(400).setMinP(0).setMaxP(5000).setBus("Bus Slovenia").add();

        Substation substationIt1 = network.newSubstation().setId("Substation Italy 1").setCountry(Country.IT).add();
        VoltageLevel voltageLevelIt1 = substationIt1.newVoltageLevel().setId("Voltage level Italy 1").setNominalV(400).setTopologyKind(TopologyKind.BUS_BREAKER).add();
        voltageLevelIt1.getBusBreakerView().newBus().setId("Bus Italy 1-1").add();
        voltageLevelIt1.getBusBreakerView().newBus().setId("Bus Italy 1-2").add();
        voltageLevelIt1.getBusBreakerView().newSwitch().setId("Switch Italy").setOpen(true).setBus1("Bus Italy 1-1").setBus2("Bus Italy 1-2").add();

        Substation substationIt2 = network.newSubstation().setId("Substation Italy 2").setCountry(Country.IT).add();
        VoltageLevel voltageLevelIt2 = substationIt2.newVoltageLevel().setId("Voltage level Italy 2").setNominalV(400).setTopologyKind(TopologyKind.BUS_BREAKER).add();
        voltageLevelIt2.getBusBreakerView().newBus().setId("Bus Italy 2").add();
        voltageLevelIt2.newLoad().setId("Load Italy").setP0(4500).setQ0(0).setBus("Bus Italy 2").add();

        network.newLine().setId("France - Italy").setR(0.1).setX(0.1).setG1(0.1).setG2(0.1).setB1(0.1).setB2(0.1).setVoltageLevel1("Voltage level France").setBus1("Bus France").setVoltageLevel2("Voltage level Italy 1").setBus2("Bus Italy 1-1").add();
        network.newLine().setId("Austria - Italy").setR(0.1).setX(0.1).setG1(0.1).setG2(0.1).setB1(0.1).setB2(0.1).setVoltageLevel1("Voltage level Austria").setBus1("Bus Austria").setVoltageLevel2("Voltage level Italy 1").setBus2("Bus Italy 1-2").add();
        network.newLine().setId("Slovenia - Italy").setR(0.1).setX(0.1).setG1(0.1).setG2(0.1).setB1(0.1).setB2(0.1).setVoltageLevel1("Voltage level Slovenia").setBus1("Bus Slovenia").setVoltageLevel2("Voltage level Italy 1").setBus2("Bus Italy 1-2").add();
        network.newLine().setId("Italy internal 1").setR(0.1).setX(0.1).setG1(0.1).setG2(0.1).setB1(0.1).setB2(0.1).setVoltageLevel1("Voltage level Italy 1").setBus1("Bus Italy 1-1").setVoltageLevel2("Voltage level Italy 2").setBus2("Bus Italy 2").add();
        network.newLine().setId("Italy internal 2").setR(0.1).setX(0.1).setG1(0.1).setG2(0.1).setB1(0.1).setB2(0.1).setVoltageLevel1("Voltage level Italy 1").setBus1("Bus Italy 1-2").setVoltageLevel2("Voltage level Italy 2").setBus2("Bus Italy 2").add();

        return network;
    }

    public Crac createCrac() {
        SimpleCrac crac = new SimpleCrac("Dichotomy test crac");
        Instant instant = crac.addInstant("N", 0);
        State preventiveState = crac.addState(null, instant);
        crac.addNetworkElement("Italy internal 1");
        crac.addNetworkElement("Italy internal 2");
        crac.newBranchCnec().setId("Italy internal 1").setInstant(instant)
                .newNetworkElement().setId("Italy internal 1").add()
                .newThreshold().setMax(3000.).setUnit(Unit.MEGAWATT).setRule(BranchThresholdRule.ON_LEFT_SIDE).add();
        crac.newBranchCnec().setId("Italy internal 2").setInstant(instant)
                .newNetworkElement().setId("Italy internal 2").add()
                .newThreshold().setMax(3000.).setUnit(Unit.MEGAWATT).setRule(BranchThresholdRule.ON_LEFT_SIDE).add();
        NetworkElement switchElement = crac.addNetworkElement("Switch Italy");
        Topology switchRa = new Topology(
                "Switch Italy",
                switchElement,
                ActionType.CLOSE
        );
        switchRa.addUsageRule(new FreeToUseImpl(UsageMethod.AVAILABLE, instant));
        crac.addNetworkAction(switchRa);
        return crac;
    }

    public ZonalData<Scalable> createGlsk() {
        Map<String, Scalable> glsks = new TreeMap<>();
        glsks.put("France", Scalable.onGenerator("Generator France"));
        glsks.put("Austria", Scalable.onGenerator("Generator Austria"));
        glsks.put("Slovenia", Scalable.onGenerator("Generator Slovenia"));
        glsks.put("Italy", Scalable.onLoad("Load Italy"));
        return new ZonalDataImpl<>(glsks);
    }

    @Test
    void runRangeDivisionDichotomy() {
        double minValue = 0;
        double maxValue = 4500;
        double precision = 200;
        Network network = createNetwork();
        Crac crac = createCrac();
        ZonalData<Scalable> glsk = createGlsk();
        Map<String, Double> splittingFactors = new HashMap<>();
        splittingFactors.put("France", 0.4);
        splittingFactors.put("Austria", 0.3);
        splittingFactors.put("Slovenia", 0.3);
        splittingFactors.put("Italy", -1.);
        Index<RaoStepResult> index = new Index<>(minValue, maxValue, precision);
        IndexStrategy indexStrategy = new RangeDivisionIndexStrategy(true);
        ValidationStrategy<RaoStepResult> validationStrategy = new RaoValidationStrategy(network, crac, glsk, splittingFactors);
        DichotomyEngine<RaoStepResult> engine = new DichotomyEngine<>(index, indexStrategy, validationStrategy);
        engine.run();

        assertEquals(1406.25, index.higherSecureStep().stepValue(), EPSILON);
        assertEquals(1546.875, index.lowerUnsecureStep().stepValue(), EPSILON);
    }

    @Test
    void runStepsDichotomy() {
        double minValue = 0;
        double maxValue = 4500;
        double precision = 200;
        double stepsSize = 600;
        Network network = createNetwork();
        Crac crac = createCrac();
        ZonalData<Scalable> glsk = createGlsk();
        Map<String, Double> splittingFactors = new HashMap<>();
        splittingFactors.put("France", 0.4);
        splittingFactors.put("Austria", 0.3);
        splittingFactors.put("Slovenia", 0.3);
        splittingFactors.put("Italy", -1.);
        Index<RaoStepResult> index = new Index<>(minValue, maxValue, precision);
        IndexStrategy indexStrategy = new StepsIndexStrategy(true, stepsSize);
        ValidationStrategy<RaoStepResult> validationStrategy = new RaoValidationStrategy(network, crac, glsk, splittingFactors);
        DichotomyEngine<RaoStepResult> engine = new DichotomyEngine<>(index, indexStrategy, validationStrategy);
        engine.run();

        assertEquals(1350, index.higherSecureStep().stepValue(), EPSILON);
        assertEquals(1500, index.lowerUnsecureStep().stepValue(), EPSILON);
    }
}
