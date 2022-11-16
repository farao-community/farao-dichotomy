/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy.api.index;

import com.farao_community.farao.dichotomy.api.results.DichotomyStepResult;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Interface responsible of defining which index value should be tested next by the dichotomy engine
 * based on current Index state (which contains previously tested values information.
 *
 * @see RangeDivisionIndexStrategy
 * @see StepsIndexStrategy
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public interface IndexStrategy {
    double EPSILON = 1e-3;

    double nextValue(Index<?> index);

    default boolean precisionReached(Pair<Double, ? extends DichotomyStepResult<?>> startInterval, Pair<Double, ? extends DichotomyStepResult<?>> endInterval, Index<?> index) {
        if (endInterval != null
            && endInterval.getLeft() - index.maxValue() >= EPSILON) {
            return true;
        } else if (startInterval != null
            && index.minValue() - startInterval.getLeft() >= EPSILON) {
            return true;
        } else if (startInterval != null && endInterval != null) {
            return endInterval.getLeft() - startInterval.getLeft() < index.precision();
        } else {
            return false;
        }
    }

}
