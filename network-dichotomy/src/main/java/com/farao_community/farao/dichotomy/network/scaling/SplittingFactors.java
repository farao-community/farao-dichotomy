package com.farao_community.farao.dichotomy.network.scaling;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public class SplittingFactors implements ShiftDispatcher {
    private final Map<String, Double> factors;

    public SplittingFactors(Map<String, Double> factors) {
        this.factors = factors;
    }

    @Override
    public final Map<String, Double> dispatch(double value) {
        return factors.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> e.getValue() * value
                ));
    }
}
