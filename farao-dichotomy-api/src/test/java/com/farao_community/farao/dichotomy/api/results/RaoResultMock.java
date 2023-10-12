/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy.api.results;

import com.farao_community.farao.commons.Unit;
import com.farao_community.farao.data.crac_api.Instant;
import com.farao_community.farao.data.crac_api.RemedialAction;
import com.farao_community.farao.data.crac_api.State;
import com.farao_community.farao.data.crac_api.cnec.AngleCnec;
import com.farao_community.farao.data.crac_api.cnec.FlowCnec;
import com.farao_community.farao.data.crac_api.cnec.Side;
import com.farao_community.farao.data.crac_api.cnec.VoltageCnec;
import com.farao_community.farao.data.crac_api.network_action.NetworkAction;
import com.farao_community.farao.data.crac_api.range_action.PstRangeAction;
import com.farao_community.farao.data.crac_api.range_action.RangeAction;
import com.farao_community.farao.data.rao_result_api.ComputationStatus;
import com.farao_community.farao.data.rao_result_api.OptimizationStepsExecuted;
import com.farao_community.farao.data.rao_result_api.RaoResult;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Map;
import java.util.Set;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public class RaoResultMock implements RaoResult {
    private final double cost;

    public RaoResultMock(boolean secure) {
        this.cost = secure ? -10 : 10;
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
    public double getFlow(Instant optimizationState, FlowCnec flowCnec, Side side, Unit unit) {
        return 0;
    }

    @Override
    public double getAngle(Instant optimizationState, AngleCnec angleCnec, Unit unit) {
        return 0;
    }

    @Override
    public double getVoltage(Instant optimizationState, VoltageCnec voltageCnec, Unit unit) {
        return 0;
    }

    @Override
    public double getMargin(Instant optimizationState, FlowCnec flowCnec, Unit unit) {
        return 0;
    }

    @Override
    public double getMargin(Instant optimizationState, AngleCnec angleCnec, Unit unit) {
        return 0;
    }

    @Override
    public double getMargin(Instant optimizationState, VoltageCnec voltageCnec, Unit unit) {
        return 0;
    }

    @Override
    public double getRelativeMargin(Instant optimizationState, FlowCnec flowCnec, Unit unit) {
        return 0;
    }

    @Override
    public double getCommercialFlow(Instant optimizationState, FlowCnec flowCnec, Side side, Unit unit) {
        return 0;
    }

    @Override
    public double getLoopFlow(Instant optimizationState, FlowCnec flowCnec, Side side, Unit unit) {
        return 0;
    }

    @Override
    public double getPtdfZonalSum(Instant optimizationState, FlowCnec flowCnec, Side side) {
        return 0;
    }

    @Override
    public double getCost(Instant optimizationState) {
        return 0;
    }

    @Override
    public double getFunctionalCost(Instant optimizationState) {
        if (optimizationState == Instant.CURATIVE) {
            return cost;
        } else {
            throw new NotImplementedException("Only after CRA is handled");
        }
    }

    @Override
    public double getVirtualCost(Instant optimizationState) {
        return 0;
    }

    @Override
    public Set<String> getVirtualCostNames() {
        return null;
    }

    @Override
    public double getVirtualCost(Instant optimizationState, String s) {
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
    public OptimizationStepsExecuted getOptimizationStepsExecuted() {
        return null;
    }

    @Override
    public void setOptimizationStepsExecuted(OptimizationStepsExecuted optimizationStepsExecuted) {

    }
}
