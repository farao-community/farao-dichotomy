package com.farao_community.farao.dichotomy.api.index;

public class SingleDichotomyVariable implements DichotomyVariable<SingleDichotomyVariable> {
    private final double value;

    public SingleDichotomyVariable(double value) {
        this.value = value;
    }

    public double value() {
        return value;
    }

    @Override
    public boolean isGreaterThan(SingleDichotomyVariable other) {
        return this.value > other.value;
    }

    public double distanceTo(double otherValue) {
        return Math.abs(this.value - otherValue);
    }

    @Override
    public double distanceTo(SingleDichotomyVariable other) {
        return distanceTo(other.value);
    }

    @Override
    public SingleDichotomyVariable halfRangeWith(SingleDichotomyVariable other) {
        return new SingleDichotomyVariable((value + other.value) / 2);
    }

    @Override
    public String print() {
        return String.format("%.0f", value);
    }
}
