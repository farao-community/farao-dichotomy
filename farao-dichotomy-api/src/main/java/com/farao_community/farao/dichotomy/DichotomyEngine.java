/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Dichotomy engine.
 *
 * This engine is agnostic of the type of validation that occurs during the dichotomic research.
 * This knowledge is delegated to the ValidationStrategy and the IndexStrategy that is used.
 *
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class DichotomyEngine<T extends StepResult> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DichotomyEngine.class);
    private static final int DEFAULT_MAX_ITERATION_NUMBER = 100;
    private final Index<T> index;
    private final IndexStrategy indexStrategy;
    private final ValidationStrategy<T> validationStrategy;
    private final int maxIteration;

    public DichotomyEngine(Index<T> index, IndexStrategy indexStrategy, ValidationStrategy<T> validationStrategy) {
        this(index, indexStrategy, validationStrategy, DEFAULT_MAX_ITERATION_NUMBER);
    }

    public DichotomyEngine(Index<T> index, IndexStrategy indexStrategy, ValidationStrategy<T> validationStrategy, int maxIteration) {
        if (maxIteration < 3) {
            throw new DichotomyException("Max number of iterations of the dichotomy engine should be at least 3.");
        }
        this.index = Objects.requireNonNull(index);
        this.indexStrategy = Objects.requireNonNull(indexStrategy);
        this.validationStrategy = Objects.requireNonNull(validationStrategy);
        this.maxIteration = maxIteration;
    }

    public void run() {
        int iterationCounter = 0;
        while (!index.precisionReached() && iterationCounter < maxIteration) {
            double nextValue = indexStrategy.nextValue(index);
            T result = validationStrategy.validateStep(nextValue);
            index.addDichotomyStepResult(result);
            iterationCounter++;
        }

        if (iterationCounter == maxIteration) {
            LOGGER.error("Max number of iteration reached during dichotomy, research precision has not been reached.");
        }
    }
}
