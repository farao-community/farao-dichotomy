package com.farao_community.farao.dichotomy.api.index;

public class IndexStrategyImpl implements IndexStrategy<SingleValueDichotomyStep> {

    public SingleValueDichotomyStep nextValue(Index<?, SingleValueDichotomyStep> index) {
        return new SingleValueDichotomyStep(Double.NaN);
    }
}
