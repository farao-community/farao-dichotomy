/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy.api;

import com.farao_community.farao.dichotomy.api.exceptions.DichotomyException;
import com.farao_community.farao.dichotomy.api.exceptions.GlskLimitationException;
import com.farao_community.farao.dichotomy.api.exceptions.RaoFailureException;
import com.farao_community.farao.dichotomy.api.exceptions.RaoInterruptionException;
import com.farao_community.farao.dichotomy.api.exceptions.ShiftingException;
import com.farao_community.farao.dichotomy.api.exceptions.ValidationException;
import com.farao_community.farao.dichotomy.api.index.Index;
import com.farao_community.farao.dichotomy.api.index.IndexStrategy;
import com.farao_community.farao.dichotomy.api.results.DichotomyResult;
import com.farao_community.farao.dichotomy.api.results.DichotomyStepResult;
import com.farao_community.farao.dichotomy.api.utils.Formatter;
import com.powsybl.iidm.network.Network;

import java.util.Objects;

import static com.farao_community.farao.dichotomy.api.logging.DichotomyLoggerProvider.BUSINESS_LOGS;
import static com.farao_community.farao.dichotomy.api.logging.DichotomyLoggerProvider.BUSINESS_WARNS;
import static com.farao_community.farao.dichotomy.api.results.ReasonInvalid.*;

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
    private final IndexStrategy<T> indexStrategy;
    private final InterruptionStrategy interruptionStrategy;
    private final NetworkShifter networkShifter;
    private final NetworkValidator<T> networkValidator;
    private final NetworkExporter networkExporter;
    private final int maxIteration;
    private final String runId;

    /**
     * Use the builder
     */
    DichotomyEngine(Index<T> index, IndexStrategy<T> indexStrategy, InterruptionStrategy interruptionStrategy, NetworkShifter networkShifter, NetworkValidator<T> networkValidator, NetworkExporter networkExporter, int maxIteration, String runId) {
        if (maxIteration < 3) {
            throw new DichotomyException("Max number of iterations of the dichotomy engine should be at least 3.");
        }
        this.index = Objects.requireNonNull(index);
        this.indexStrategy = Objects.requireNonNull(indexStrategy);
        this.interruptionStrategy = interruptionStrategy;
        this.networkShifter = Objects.requireNonNull(networkShifter);
        this.networkValidator = Objects.requireNonNull(networkValidator);
        this.networkExporter = networkExporter;
        this.maxIteration = maxIteration;
        this.runId = runId;
    }

    public DichotomyResult<T> run(final Network network) {
        int iterationCounter = 0;
        final String initialVariant = network.getVariantManager().getWorkingVariantId();
        String raoFailure = null;

        while (!indexStrategy.precisionReached(index) && iterationCounter < maxIteration && raoFailure == null) {
            if (interruptionStrategy != null && interruptionStrategy.shouldRunBeInterruptedSoftly(runId)) {
                return buildInterruptedResult();
            } else {
                final double nextValue = indexStrategy.nextValue(index);
                BUSINESS_LOGS.info(String.format("Next dichotomy step: %s", Formatter.formatDoubleDecimals(nextValue)));
                try {
                    final DichotomyStepResult<T> result = validate(nextValue,
                                                                   network,
                                                                   initialVariant,
                                                                   getLastDichotomyStepResult());
                    logDichotomyStepResult(result, nextValue);

                    if (result.getReasonInvalid() == RAO_INTERRUPTION) {
                        return buildInterruptedResult();
                    } else {
                        index.addDichotomyStepResult(nextValue, result);
                    }
                    iterationCounter++;
                } catch (final RaoFailureException e) {
                    raoFailure = e.getMessage();
                }
            }
        }

        if (raoFailure != null) {
            return DichotomyResult.buildFromRaoFailure(raoFailure);
        }

        if (iterationCounter == maxIteration) {
            BUSINESS_WARNS.warn("Max number of iteration {} reached during dichotomy, research precision has not been reached.", maxIteration);
        }
        return DichotomyResult.buildFromIndex(index);
    }

    private DichotomyResult<T> buildInterruptedResult() {
        final DichotomyResult<T> dichotomyResult = DichotomyResult.buildFromIndex(index);
        dichotomyResult.setInterrupted(true);
        return dichotomyResult;
    }

    private DichotomyStepResult<T> getLastDichotomyStepResult() {
        return !index.testedSteps().isEmpty() ? index.testedSteps().getLast().getRight() : null;
    }

    private static <T> void logDichotomyStepResult(final DichotomyStepResult<T> dichotomyStepResult, final double nextValue) {
        if (dichotomyStepResult.isValid()) {
            BUSINESS_LOGS.info(String.format("Network at dichotomy step %s is secure", Formatter.formatDoubleDecimals(nextValue)));
        } else if (dichotomyStepResult.getReasonInvalid() == RAO_INTERRUPTION) {
            BUSINESS_LOGS.info(String.format("Got interrupted before it could determine whether the network at dichotomy step %s is secure or not", Formatter.formatDoubleDecimals(nextValue)));
        } else {
            BUSINESS_LOGS.info(String.format("Network at dichotomy step %s is unsecure", Formatter.formatDoubleDecimals(nextValue)));
        }
    }

    DichotomyStepResult<T> validate(final double stepValue, final Network network,
                                    final String initialVariant, final DichotomyStepResult<T> lastDichotomyStepResult) throws RaoFailureException {
        final String newVariant = variantName(stepValue, initialVariant);
        network.getVariantManager().cloneVariant(initialVariant, newVariant);
        network.getVariantManager().setWorkingVariant(newVariant);
        final String formattedStepValueForLogs = Formatter.formatDoubleDecimals(stepValue);
        try {
            networkShifter.shiftNetwork(stepValue, network);
            return networkValidator.validateNetwork(network, lastDichotomyStepResult);
        } catch (final GlskLimitationException e) {
            BUSINESS_WARNS.warn(String.format("GLSK limits have been reached for step value %s", formattedStepValueForLogs));
            return DichotomyStepResult.fromFailure(GLSK_LIMITATION, e.getMessage());
        } catch (final ShiftingException e) {
            return handleShiftingException(e, network, formattedStepValueForLogs);
        } catch (final ValidationException e) {
            BUSINESS_WARNS.warn(String.format("Validation failed for step value %s", formattedStepValueForLogs));
            return DichotomyStepResult.fromFailure(VALIDATION_FAILED, e.getMessage());
        } catch (final RaoInterruptionException e) {
            BUSINESS_WARNS.warn(String.format("RAO interrupted during step value %s", formattedStepValueForLogs));
            return DichotomyStepResult.fromFailure(RAO_INTERRUPTION, e.getMessage());
        } finally {
            network.getVariantManager().setWorkingVariant(initialVariant);
            network.getVariantManager().removeVariant(newVariant);
        }
    }

    private DichotomyStepResult<T> handleShiftingException(final ShiftingException e, final Network network, final String formattedStepValueForLogs) {
        if (e.getReason() == BALANCE_LOADFLOW_DIVERGENCE || e.getReason() == UNKNOWN_TERMINAL_BUS) {
            BUSINESS_WARNS.warn(String.format("%s for step value %s", e.getMessage(), formattedStepValueForLogs));

            if (networkExporter != null && e.getReason() == BALANCE_LOADFLOW_DIVERGENCE) {
                try {
                    networkExporter.export(network);
                } catch (Exception ex) {
                    BUSINESS_WARNS.warn("Exception occurred while exporting network", ex);
                }
            }

            return DichotomyStepResult.fromFailure(e.getReason(), e.getMessage());
        } else {
            BUSINESS_WARNS.warn(String.format("Validation failed for step value %s", formattedStepValueForLogs));
            return DichotomyStepResult.fromFailure(VALIDATION_FAILED, e.getMessage());
        }
    }

    private String variantName(final double stepValue, final String initialVariant) {
        return String.format("%s-ScaledBy-%d", initialVariant, (int) stepValue);
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static final class Builder<T> {
        private Index<T> index;
        private IndexStrategy<T> indexStrategy;
        private InterruptionStrategy interruptionStrategy;
        private NetworkShifter networkShifter;
        private NetworkValidator<T> networkValidator;
        private NetworkExporter networkExporter;
        private int maxIteration = DEFAULT_MAX_ITERATION_NUMBER;
        private String runId;

        private Builder() {
        }

        public Builder<T> withIndex(final Index<T> index) {
            this.index = index;
            return this;
        }

        public Builder<T> withIndexStrategy(final IndexStrategy<T> indexStrategy) {
            this.indexStrategy = indexStrategy;
            return this;
        }

        public Builder<T> withInterruptionStrategy(final InterruptionStrategy interruptionStrategy) {
            this.interruptionStrategy = interruptionStrategy;
            return this;
        }

        public Builder<T> withNetworkShifter(final NetworkShifter networkShifter) {
            this.networkShifter = networkShifter;
            return this;
        }

        public Builder<T> withNetworkValidator(final NetworkValidator<T> networkValidator) {
            this.networkValidator = networkValidator;
            return this;
        }

        public Builder<T> withNetworkExporter(final NetworkExporter networkExporter) {
            this.networkExporter = networkExporter;
            return this;
        }

        public Builder<T> withMaxIteration(final int maxIteration) {
            this.maxIteration = maxIteration;
            return this;
        }

        public Builder<T> withRunId(final String runId) {
            this.runId = runId;
            return this;
        }

        public DichotomyEngine<T> build() {
            return new DichotomyEngine<>(index, indexStrategy, interruptionStrategy, networkShifter, networkValidator, networkExporter, maxIteration, runId);
        }
    }
}
