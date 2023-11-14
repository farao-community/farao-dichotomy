package com.farao_community.farao.dichotomy.api.index;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class MultipleValuesDichotomyStep implements DichotomyStep<MultipleValuesDichotomyStep> {
    private final Map<String, SingleValueDichotomyStep> values; // values with keys

    public MultipleValuesDichotomyStep(Map<String, SingleValueDichotomyStep> values) {
        this.values = values;
    }

    public Map<String, SingleValueDichotomyStep> values() {
        return new HashMap<>(values);
    }

    @Override
    public boolean isGreaterThan(MultipleValuesDichotomyStep other) {
        if (!values.keySet().equals(other.values.keySet())) {
            // TODO : throw
        }
        return values.entrySet().stream().allMatch(
            e -> e.getValue().isGreaterThan(other.values.get(e.getKey()))
        );
    }

    @Override
    public double distanceTo(MultipleValuesDichotomyStep other) {
        if (!values.keySet().equals(other.values.keySet())) {
            // TODO : throw
        }
        return values.entrySet().stream().mapToDouble(
            e -> e.getValue().distanceTo(other.values.get(e.getKey()))
        ).max().orElse(0.);
    }

    @Override
    public MultipleValuesDichotomyStep halfRangeWith(MultipleValuesDichotomyStep other) {
        return new MultipleValuesDichotomyStep(
            values.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().halfRangeWith(other.values.get(e.getKey()))
            )));
    }

    @Override
    public String print() {
        return values.entrySet().stream().map(e -> String.format("%s : %s", e.getKey(), e.getValue().print())).collect(Collectors.joining(", "));
    }
}
