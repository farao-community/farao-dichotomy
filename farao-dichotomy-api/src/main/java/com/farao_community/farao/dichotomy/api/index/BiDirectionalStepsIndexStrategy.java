/*
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy.api.index;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
// TODO : rendre générique
public class BiDirectionalStepsIndexStrategy implements IndexStrategy<SingleDichotomyVariable> {
    private final double startIndex;
    private final double stepSize;

    public BiDirectionalStepsIndexStrategy(double startIndex, double stepSize) {
        this.startIndex = startIndex;
        this.stepSize = stepSize;
    }

    @Override
    public SingleDichotomyVariable nextValue(Index<?, SingleDichotomyVariable> index) {
        if (precisionReached(index)) {
            throw new AssertionError("Dichotomy engine should not ask for next value if precision is reached");
        }

        if (index.highestValidStep() == null && index.lowestInvalidStep() == null) {
            return new SingleDichotomyVariable(startIndex);
        } else if (index.highestValidStep() == null) {
            return new SingleDichotomyVariable(Math.max(index.minValue().value(), index.lowestInvalidStep().getLeft().value() - stepSize));
        } else if (index.lowestInvalidStep() == null) {
            return new SingleDichotomyVariable(Math.min(index.maxValue().value(), index.highestValidStep().getLeft().value() + stepSize));
        } else {
            return new SingleDichotomyVariable((index.lowestInvalidStep().getLeft().value() + index.highestValidStep().getLeft().value()) / 2);
        }
    }
}
