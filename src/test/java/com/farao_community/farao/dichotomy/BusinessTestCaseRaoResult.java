/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy;

import com.farao_community.farao.commons.Unit;
import com.farao_community.farao.data.crac_api.State;
import com.farao_community.farao.data.crac_api.cnec.FlowCnec;
import com.farao_community.farao.data.crac_api.network_action.NetworkAction;
import com.farao_community.farao.data.crac_api.range_action.PstRangeAction;
import com.farao_community.farao.data.crac_api.range_action.RangeAction;
import com.farao_community.farao.data.rao_result_api.ComputationStatus;
import com.farao_community.farao.data.rao_result_api.OptimizationState;
import com.farao_community.farao.data.rao_result_api.RaoResult;

import java.util.Map;
import java.util.Set;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public class BusinessTestCaseRaoResult implements RaoResult {
    public enum Status {
        SECURED(-50.),
        UNSECURED(50.);

        private double functionalCost;

        Status(double functionalCost) {
            this.functionalCost = functionalCost;
        }

        public double getFunctionalCost() {
            return functionalCost;
        }
    }

    private Status status;

    public BusinessTestCaseRaoResult(Status status) {
        this.status = status;
    }

    @Override
    public ComputationStatus getComputationStatus() {
        return ComputationStatus.DEFAULT;
    }

    @Override
    public double getFlow(OptimizationState optimizationState, FlowCnec flowCnec, Unit unit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getMargin(OptimizationState optimizationState, FlowCnec flowCnec, Unit unit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getRelativeMargin(OptimizationState optimizationState, FlowCnec flowCnec, Unit unit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getCommercialFlow(OptimizationState optimizationState, FlowCnec flowCnec, Unit unit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getLoopFlow(OptimizationState optimizationState, FlowCnec flowCnec, Unit unit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getPtdfZonalSum(OptimizationState optimizationState, FlowCnec flowCnec) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getCost(OptimizationState optimizationState) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getFunctionalCost(OptimizationState optimizationState) {
        return status.getFunctionalCost();
    }

    @Override
    public double getVirtualCost(OptimizationState optimizationState) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> getVirtualCostNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getVirtualCost(OptimizationState optimizationState, String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean wasActivatedBeforeState(State state, NetworkAction networkAction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isActivatedDuringState(State state, NetworkAction networkAction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<NetworkAction> getActivatedNetworkActionsDuringState(State state) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isActivatedDuringState(State state, RangeAction rangeAction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getPreOptimizationTapOnState(State state, PstRangeAction pstRangeAction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getOptimizedTapOnState(State state, PstRangeAction pstRangeAction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getPreOptimizationSetPointOnState(State state, RangeAction rangeAction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getOptimizedSetPointOnState(State state, RangeAction rangeAction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<RangeAction> getActivatedRangeActionsDuringState(State state) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<PstRangeAction, Integer> getOptimizedTapsOnState(State state) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<RangeAction, Double> getOptimizedSetPointsOnState(State state) {
        throw new UnsupportedOperationException();
    }
}
