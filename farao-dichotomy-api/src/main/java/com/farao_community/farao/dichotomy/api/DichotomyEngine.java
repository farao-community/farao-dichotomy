/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy.api;

import com.farao_community.farao.dichotomy.api.exceptions.DichotomyException;
import com.farao_community.farao.dichotomy.api.exceptions.GlskLimitationException;
import com.farao_community.farao.dichotomy.api.exceptions.ShiftingException;
import com.farao_community.farao.dichotomy.api.exceptions.ValidationException;
import com.farao_community.farao.dichotomy.api.index.Index;
import com.farao_community.farao.dichotomy.api.index.IndexStrategy;
import com.farao_community.farao.dichotomy.api.results.DichotomyResult;
import com.farao_community.farao.dichotomy.api.results.DichotomyStepResult;
import com.farao_community.farao.dichotomy.api.results.ReasonInvalid;
import com.powsybl.iidm.network.Network;

import java.util.Objects;

import static com.farao_community.farao.dichotomy.api.logging.DichotomyLoggerProvider.*;

/**
 * Dichotomy engine.
 *
 * This is a generic engine to perform a dichotomy on an IIDM network. The generic algorithm is as follows: a target
 * index is defined according to the {@link IndexStrategy}, then according to this value a shift on the network is
 * performed by the {@link NetworkShifter}. After the shift a validation is performed and gathers the results in the
 * {@link DichotomyStepResult}. Thanks to the index, in the end a {@link DichotomyResult} is defined that would define
 * a highest secure step and a lower unsecure step to characterize the dichotomy.
 *
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class DichotomyEngine<T> {
    private static final int DEFAULT_MAX_ITERATION_NUMBER = 100;
    private final Index<T> index;
    private final IndexStrategy indexStrategy;
    private final NetworkShifter networkShifter;
    private final NetworkValidator<T> networkValidator;
    private final int maxIteration;
    private final String dichotomyDirection;

    public DichotomyEngine(Index<T> index, IndexStrategy indexStrategy, NetworkShifter networkShifter, NetworkValidator<T> networkValidator) {
        this(index, indexStrategy, networkShifter, networkValidator, DEFAULT_MAX_ITERATION_NUMBER, "");
    }

    public DichotomyEngine(Index<T> index, IndexStrategy indexStrategy, NetworkShifter networkShifter, NetworkValidator<T> networkValidator, String dichotomyDirection) {
        this(index, indexStrategy, networkShifter, networkValidator, DEFAULT_MAX_ITERATION_NUMBER, dichotomyDirection);
    }

    public DichotomyEngine(Index<T> index, IndexStrategy indexStrategy, NetworkShifter networkShifter, NetworkValidator<T> networkValidator, int maxIteration) {
        this(index, indexStrategy, networkShifter, networkValidator, maxIteration, "");
    }

    public DichotomyEngine(Index<T> index, IndexStrategy indexStrategy, NetworkShifter networkShifter, NetworkValidator<T> networkValidator, int maxIteration, String dichotomyDirection) {
        if (maxIteration < 3) {
            throw new DichotomyException("Max number of iterations of the dichotomy engine should be at least 3.");
        }
        this.index = Objects.requireNonNull(index);
        this.indexStrategy = Objects.requireNonNull(indexStrategy);
        this.networkShifter = networkShifter;
        this.networkValidator = Objects.requireNonNull(networkValidator);
        this.maxIteration = maxIteration;
        this.dichotomyDirection = dichotomyDirection;
    }

    public DichotomyResult<T> run(Network network) {
        int iterationCounter = 0;
        String initialVariant = network.getVariantManager().getWorkingVariantId();
        while (!indexStrategy.precisionReached(index) && iterationCounter < maxIteration) {
            double nextValue = indexStrategy.nextValue(index);
            logDichotomyStepInfo(dichotomyDirection, String.format("Next dichotomy step: %.2f", nextValue));
            DichotomyStepResult<T> lastDichotomyStepResult = !index.testedSteps().isEmpty() ? index.testedSteps().get(index.testedSteps().size() - 1).getRight() : null;
            DichotomyStepResult<T> dichotomyStepResult = validate(nextValue, network, initialVariant, lastDichotomyStepResult);
            if (dichotomyStepResult.isValid()) {
                logDichotomyStepInfo(dichotomyDirection, String.format("Network at dichotomy step %.2f is secure", nextValue));
            } else {
                logDichotomyStepInfo(dichotomyDirection, String.format("Network at dichotomy step %.2f is unsecure", nextValue));
            }
            index.addDichotomyStepResult(nextValue, dichotomyStepResult);
            iterationCounter++;
        }

        if (iterationCounter == maxIteration) {
            logDichotomyStepWarn(dichotomyDirection, String.format("Max number of iteration %s reached during dichotomy, research precision has not been reached.", maxIteration));
        }
        return DichotomyResult.buildFromIndex(index);
    }

    private DichotomyStepResult<T> validate(double stepValue, Network network, String initialVariant, DichotomyStepResult<T> lastDichotomyStepResult) {
        String newVariant = variantName(stepValue, initialVariant);
        network.getVariantManager().cloneVariant(initialVariant, newVariant);
        network.getVariantManager().setWorkingVariant(newVariant);
        try {
            networkShifter.shiftNetwork(stepValue, network);
            return networkValidator.validateNetwork(network, lastDichotomyStepResult);
        } catch (GlskLimitationException e) {
            logDichotomyStepWarn(dichotomyDirection, String.format("GLSK limits have been reached for step value %.2f", stepValue));
            return DichotomyStepResult.fromFailure(ReasonInvalid.GLSK_LIMITATION, e.getMessage());
        } catch (ShiftingException | ValidationException e) {
            logDichotomyStepWarn(dichotomyDirection, String.format("Validation failed for step value %.2f", stepValue));
            return DichotomyStepResult.fromFailure(ReasonInvalid.VALIDATION_FAILED, e.getMessage());
        } finally {
            network.getVariantManager().setWorkingVariant(initialVariant);
            network.getVariantManager().removeVariant(newVariant);
        }
    }

    private String variantName(double stepValue, String initialVariant) {
        return String.format("%s-ScaledBy-%d", initialVariant, (int) stepValue);
    }

    private void logDichotomyStepInfo(String dichotomyDirection, String message) {
        if (dichotomyDirection.isEmpty()) {
            BUSINESS_LOGS.info(message);
        } else {
            BUSINESS_LOGS.info("[{}] : {}", dichotomyDirection, message);
        }
    }

    private void logDichotomyStepWarn(String dichotomyDirection, String message) {
        if (dichotomyDirection.isEmpty()) {
            BUSINESS_WARNS.warn(message);
        } else {
            BUSINESS_WARNS.warn("[{}] : {}", dichotomyDirection, message);
        }
    }
}
