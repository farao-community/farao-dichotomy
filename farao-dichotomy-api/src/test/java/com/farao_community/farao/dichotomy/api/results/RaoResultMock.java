/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy.api.results;

import com.powsybl.iidm.network.TwoSides;
import com.powsybl.openrao.commons.MinOrMax;
import com.powsybl.openrao.commons.PhysicalParameter;
import com.powsybl.openrao.commons.Unit;
import com.powsybl.openrao.data.crac.api.Instant;
import com.powsybl.openrao.data.crac.api.RemedialAction;
import com.powsybl.openrao.data.crac.api.State;
import com.powsybl.openrao.data.crac.api.cnec.AngleCnec;
import com.powsybl.openrao.data.crac.api.cnec.FlowCnec;
import com.powsybl.openrao.data.crac.api.cnec.VoltageCnec;
import com.powsybl.openrao.data.crac.api.networkaction.NetworkAction;
import com.powsybl.openrao.data.crac.api.rangeaction.PstRangeAction;
import com.powsybl.openrao.data.crac.api.rangeaction.RangeAction;
import com.powsybl.openrao.data.raoresult.api.ComputationStatus;
import com.powsybl.openrao.data.raoresult.api.RaoResult;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Map;
import java.util.Set;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public class RaoResultMock implements RaoResult {
    private final double cost;
    private final boolean secure;

    public RaoResultMock(boolean secure) {
        this.cost = secure ? -10 : 10;
        this.secure = secure;
    }

    @Override
    public ComputationStatus getComputationStatus() {
        return null;
    }

    @Override
    public ComputationStatus getComputationStatus(State state) {
        return null;
    }

    @Override
    public double getFlow(Instant optimizedInstant, FlowCnec flowCnec, TwoSides side, Unit unit) {
        return 0;
    }

    @Override
    public double getAngle(Instant optimizedInstant, AngleCnec angleCnec, Unit unit) {
        return 0;
    }

    @Override
    public double getVoltage(Instant optimizedInstant, VoltageCnec voltageCnec, MinOrMax minOrMax, Unit unit) {
        return 0;
    }

    @Override
    public double getMargin(Instant optimizedInstant, FlowCnec flowCnec, Unit unit) {
        return 0;
    }

    @Override
    public double getMargin(Instant optimizedInstant, AngleCnec angleCnec, Unit unit) {
        return 0;
    }

    @Override
    public double getMargin(Instant optimizedInstant, VoltageCnec voltageCnec, Unit unit) {
        return 0;
    }

    @Override
    public double getRelativeMargin(Instant optimizedInstant, FlowCnec flowCnec, Unit unit) {
        return 0;
    }

    @Override
    public double getCommercialFlow(Instant optimizedInstant, FlowCnec flowCnec, TwoSides side, Unit unit) {
        return 0;
    }

    @Override
    public double getLoopFlow(Instant optimizedInstant, FlowCnec flowCnec, TwoSides side, Unit unit) {
        return 0;
    }

    @Override
    public double getPtdfZonalSum(Instant optimizedInstant, FlowCnec flowCnec, TwoSides side) {
        return 0;
    }

    @Override
    public double getCost(Instant optimizedInstant) {
        return 0;
    }

    @Override
    public double getFunctionalCost(Instant optimizedInstant) {
        if (optimizedInstant.isCurative()) {
            return cost;
        } else {
            throw new NotImplementedException("Only after CRA is handled");
        }
    }

    @Override
    public double getVirtualCost(Instant optimizedInstant) {
        return 0;
    }

    @Override
    public Set<String> getVirtualCostNames() {
        return null;
    }

    @Override
    public double getVirtualCost(Instant optimizedInstant, String s) {
        return 0;
    }

    @Override
    public boolean isActivatedDuringState(State state, RemedialAction<?> remedialAction) {
        return false;
    }

    @Override
    public boolean wasActivatedBeforeState(State state, NetworkAction networkAction) {
        return false;
    }

    @Override
    public boolean isActivatedDuringState(State state, NetworkAction networkAction) {
        return false;
    }

    @Override
    public boolean isActivated(State state, NetworkAction networkAction) {
        return RaoResult.super.isActivated(state, networkAction);
    }

    @Override
    public Set<NetworkAction> getActivatedNetworkActionsDuringState(State state) {
        return null;
    }

    @Override
    public boolean isActivatedDuringState(State state, RangeAction rangeAction) {
        return false;
    }

    @Override
    public int getPreOptimizationTapOnState(State state, PstRangeAction pstRangeAction) {
        return 0;
    }

    @Override
    public int getOptimizedTapOnState(State state, PstRangeAction pstRangeAction) {
        return 0;
    }

    @Override
    public double getPreOptimizationSetPointOnState(State state, RangeAction rangeAction) {
        return 0;
    }

    @Override
    public double getOptimizedSetPointOnState(State state, RangeAction rangeAction) {
        return 0;
    }

    @Override
    public Set<RangeAction<?>> getActivatedRangeActionsDuringState(State state) {
        return null;
    }

    @Override
    public Map<PstRangeAction, Integer> getOptimizedTapsOnState(State state) {
        return null;
    }

    @Override
    public Map<RangeAction<?>, Double> getOptimizedSetPointsOnState(State state) {
        return null;
    }

    @Override
    public String getExecutionDetails() {
        return null;
    }

    @Override
    public void setExecutionDetails(final String s) {
        // do nothing: this is a mock
    }

    @Override
    public boolean isSecure(Instant instant, PhysicalParameter... physicalParameters) {
        return secure;
    }

    @Override
    public boolean isSecure(PhysicalParameter... physicalParameters) {
        return secure;
    }

    @Override
    public boolean isSecure() {
        return secure;
    }
}
