/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy.api;

import com.farao_community.farao.dichotomy.api.exceptions.DichotomyException;
import com.farao_community.farao.dichotomy.api.exceptions.GlskLimitationException;
import com.farao_community.farao.dichotomy.api.exceptions.RaoInterruptionException;
import com.farao_community.farao.dichotomy.api.exceptions.ShiftingException;
import com.farao_community.farao.dichotomy.api.exceptions.ValidationException;
import com.farao_community.farao.dichotomy.api.index.Index;
import com.farao_community.farao.dichotomy.api.index.IndexStrategy;
import com.farao_community.farao.dichotomy.api.results.DichotomyResult;
import com.farao_community.farao.dichotomy.api.results.DichotomyStepResult;
import com.farao_community.farao.dichotomy.api.results.ReasonInvalid;
import com.farao_community.farao.dichotomy.api.utils.Formatter;
import com.powsybl.iidm.network.Network;

import java.util.Objects;

import static com.farao_community.farao.dichotomy.api.logging.DichotomyLoggerProvider.BUSINESS_LOGS;
import static com.farao_community.farao.dichotomy.api.logging.DichotomyLoggerProvider.BUSINESS_WARNS;

/**
 * Dichotomy engine.
 *
 * <p>This is a generic engine to perform a dichotomy on an IIDM network. The generic algorithm is as follows: a target
 * index is defined according to the {@link IndexStrategy}, then according to this value a shift on the network is
 * performed by the {@link NetworkShifter}. After the shift a validation is performed and gathers the results in the
 * {@link DichotomyStepResult}. Thanks to the index, in the end a {@link DichotomyResult} is defined that would define
 * a highest secure step and a lower unsecure step to characterize the dichotomy.</p>
 *
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 * @author Vincent Bochet {@literal <vincent.bochet at rte-france.com>}
 */
public class DichotomyEngine<T> {
    private static final int DEFAULT_MAX_ITERATION_NUMBER = 100;
    private final Index<T> index;
    private final IndexStrategy indexStrategy;
    private final InterruptionStrategy interruptionStrategy;
    private final NetworkShifter networkShifter;
    private final NetworkValidator<T> networkValidator;
    private final int maxIteration;
    private final String taskId;

    /**
     * Use this constructor to use the engine WITHOUT soft-interruption feature
     */
    public DichotomyEngine(Index<T> index, IndexStrategy indexStrategy, NetworkShifter networkShifter, NetworkValidator<T> networkValidator) {
        this(index, indexStrategy, null, networkShifter, networkValidator, null);
    }

    /**
     * Use this constructor to use the engine with the soft-interruption feature
     */
    public DichotomyEngine(Index<T> index, IndexStrategy indexStrategy, InterruptionStrategy interruptionStrategy, NetworkShifter networkShifter, NetworkValidator<T> networkValidator, String taskId) {
        this(index, indexStrategy, interruptionStrategy, networkShifter, networkValidator, DEFAULT_MAX_ITERATION_NUMBER, taskId);
    }

    /**
     * Use this constructor to use the engine WITHOUT soft-interruption feature
     */
    public DichotomyEngine(Index<T> index, IndexStrategy indexStrategy, NetworkShifter networkShifter, NetworkValidator<T> networkValidator, int maxIteration) {
        this(index, indexStrategy, null, networkShifter, networkValidator, maxIteration, null);
    }

    /**
     * Use this constructor to use the engine with the soft-interruption feature
     */
    public DichotomyEngine(Index<T> index, IndexStrategy indexStrategy, InterruptionStrategy interruptionStrategy, NetworkShifter networkShifter, NetworkValidator<T> networkValidator, int maxIteration, String taskId) {
        if (maxIteration < 3) {
            throw new DichotomyException("Max number of iterations of the dichotomy engine should be at least 3.");
        }
        this.index = Objects.requireNonNull(index);
        this.indexStrategy = Objects.requireNonNull(indexStrategy);
        this.interruptionStrategy = interruptionStrategy;
        this.networkShifter = networkShifter;
        this.networkValidator = Objects.requireNonNull(networkValidator);
        this.maxIteration = maxIteration;
        this.taskId = taskId;
    }

    public DichotomyResult<T> run(Network network) {
        int iterationCounter = 0;
        String initialVariant = network.getVariantManager().getWorkingVariantId();
        while (!indexStrategy.precisionReached(index) && iterationCounter < maxIteration) {
            double nextValue = indexStrategy.nextValue(index);
            DichotomyStepResult<T> lastDichotomyStepResult = !index.testedSteps().isEmpty() ? index.testedSteps().get(index.testedSteps().size() - 1).getRight() : null;

            if (interruptionStrategy != null && interruptionStrategy.shouldTaskBeInterruptedSoftly(taskId)) {
                BUSINESS_WARNS.warn("Next dichotomy step is interrupted, previous steps results will be returned");
                DichotomyResult<T> dichotomyResult = DichotomyResult.buildFromIndex(index);
                dichotomyResult.setInterrupted(true);
                return dichotomyResult;
            } else {
                BUSINESS_LOGS.info(String.format("Next dichotomy step: %s", Formatter.formatDoubleDecimals(nextValue)));
                DichotomyStepResult<T> dichotomyStepResult = validate(nextValue, network, initialVariant, lastDichotomyStepResult);
                if (dichotomyStepResult.isValid()) {
                    BUSINESS_LOGS.info(String.format("Network at dichotomy step %s is secure", Formatter.formatDoubleDecimals(nextValue)));
                } else if (dichotomyStepResult.getReasonInvalid().equals(ReasonInvalid.RAO_INTERRUPTION)) {
                    BUSINESS_LOGS.info(String.format("Got interrupted before it could determine whether the network at dichotomy step %s is secure or not", Formatter.formatDoubleDecimals(nextValue)));
                } else {
                    BUSINESS_LOGS.info(String.format("Network at dichotomy step %s is unsecure", Formatter.formatDoubleDecimals(nextValue)));
                }
                if (!dichotomyStepResult.getReasonInvalid().equals(ReasonInvalid.RAO_INTERRUPTION)) {
                    index.addDichotomyStepResult(nextValue, dichotomyStepResult);
                }
                iterationCounter++;
            }
        }

        if (iterationCounter == maxIteration) {
            BUSINESS_WARNS.warn("Max number of iteration {} reached during dichotomy, research precision has not been reached.", maxIteration);
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
            BUSINESS_WARNS.warn(String.format("GLSK limits have been reached for step value %s", Formatter.formatDoubleDecimals(stepValue)));
            return DichotomyStepResult.fromFailure(ReasonInvalid.GLSK_LIMITATION, e.getMessage());
        } catch (ShiftingException | ValidationException e) {
            BUSINESS_WARNS.warn(String.format("Validation failed for step value %s", Formatter.formatDoubleDecimals(stepValue)));
            return DichotomyStepResult.fromFailure(ReasonInvalid.VALIDATION_FAILED, e.getMessage());
        } catch (RaoInterruptionException e) {
            BUSINESS_WARNS.warn(String.format("RAO interrupted during step value %.2f", stepValue));
            return DichotomyStepResult.fromFailure(ReasonInvalid.RAO_INTERRUPTION, e.getMessage());
        } finally {
            network.getVariantManager().setWorkingVariant(initialVariant);
            network.getVariantManager().removeVariant(newVariant);
        }
    }

    private String variantName(double stepValue, String initialVariant) {
        return String.format("%s-ScaledBy-%d", initialVariant, (int) stepValue);
    }
}
