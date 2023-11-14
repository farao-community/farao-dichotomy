package com.farao_community.farao.dichotomy.api.index;

public class IndexStrategyImpl implements IndexStrategy<SingleDichotomyVariable> {

    public SingleDichotomyVariable nextValue(Index<?, SingleDichotomyVariable> index) {
        return new SingleDichotomyVariable(Double.NaN);
    }
}
