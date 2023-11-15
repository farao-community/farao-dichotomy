package com.farao_community.farao.dichotomy.api.index;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class MultipleDichotomyVariables implements DichotomyVariable<MultipleDichotomyVariables> {
    private final Map<String, SingleDichotomyVariable> values; // values with keys

    public MultipleDichotomyVariables(Map<String, Double> values) {
        this.values = values.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new SingleDichotomyVariable(e.getValue())));
    }

    public Map<String, SingleDichotomyVariable> values() {
        return new HashMap<>(values);
    }

    @Override
    public boolean isGreaterThan(MultipleDichotomyVariables other) {
        if (!values.keySet().equals(other.values.keySet())) {
            // TODO : throw
        }
        return values.entrySet().stream().allMatch(
            e -> e.getValue().isGreaterThan(other.values.get(e.getKey()))
        );
    }

    @Override
    public double distanceTo(MultipleDichotomyVariables other) {
        if (!values.keySet().equals(other.values.keySet())) {
            // TODO : throw
        }
        return values.entrySet().stream().mapToDouble(
            e -> e.getValue().distanceTo(other.values.get(e.getKey()))
        ).max().orElse(0.);
    }

    @Override
    public MultipleDichotomyVariables halfRangeWith(MultipleDichotomyVariables other) {
        return new MultipleDichotomyVariables(
            values.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().halfRangeWith(other.values.get(e.getKey())).value()
            )));
    }

    @Override
    public String print() {
        return values.entrySet().stream().map(e -> String.format("%s : %s", e.getKey(), e.getValue().print())).collect(Collectors.joining(", "));
    }
}
