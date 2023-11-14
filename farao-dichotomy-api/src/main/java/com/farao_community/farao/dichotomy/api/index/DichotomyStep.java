package com.farao_community.farao.dichotomy.api.index;

public interface DichotomyStep<U extends DichotomyStep<U>> {
    boolean isGreaterThan(U other);

    double distanceTo(U other);

    U halfRangeWith(U other);

    String print();
}
