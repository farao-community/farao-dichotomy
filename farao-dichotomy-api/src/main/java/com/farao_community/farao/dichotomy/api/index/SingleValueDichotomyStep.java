package com.farao_community.farao.dichotomy.api.index;

public class SingleValueDichotomyStep implements DichotomyStep<SingleValueDichotomyStep> {
    private final double value;

    public SingleValueDichotomyStep(double value) {
        this.value = value;
    }

    public double value() {
        return value;
    }

    @Override
    public boolean isGreaterThan(SingleValueDichotomyStep other) {
        return this.value > other.value;
    }

    public double distanceTo(double otherValue) {
        return Math.abs(this.value - otherValue);
    }

    @Override
    public double distanceTo(SingleValueDichotomyStep other) {
        return distanceTo(other.value);
    }

    @Override
    public SingleValueDichotomyStep halfRangeWith(SingleValueDichotomyStep other) {
        return new SingleValueDichotomyStep((value + other.value) / 2);
    }

    @Override
    public String print() {
        return String.format("%.0f", value);
    }
}
