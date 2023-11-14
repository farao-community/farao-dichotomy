package com.farao_community.farao.dichotomy.api.index;

public interface DichotomyVariable<U extends DichotomyVariable<U>> {
    boolean isGreaterThan(U other);

    double distanceTo(U other);

    U halfRangeWith(U other);

    String print();
}
