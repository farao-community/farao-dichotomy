# FARAO - dichotomy
[![Actions Status](https://github.com/farao-community/farao-dichotomy/workflows/CI/badge.svg)](https://github.com/farao-community/farao-dichotomy/actions)
[![Coverage Status](https://sonarcloud.io/api/project_badges/measure?project=com.farao-community.farao%3Afarao-dichotomy&metric=coverage)](https://sonarcloud.io/component_measures?id=com.farao-community.farao%3Afarao-dichotomy&metric=coverage)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=com.farao-community.farao%3Afarao-dichotomy&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.farao-community.farao%3Afarao-dichotomy)
[![MPL-2.0 License](https://img.shields.io/badge/license-MPL_2.0-blue.svg)](https://www.mozilla.org/en-US/MPL/2.0/)
[![Join the community on Spectrum](https://withspectrum.github.io/badge/badge.svg)](https://spectrum.chat/farao-community)

## Overview

This package provides a generic dichotomy engine that allows creating custom dichotomy process by
providing specific validation strategy and dichotomy step result implementation.

## Quick implementations example

#### Implement a StepResult
```java
public class RaoStepResult implements StepResult {
    private final double stepValue;
    private final RaoResult raoResult;

    public RaoStepResult(double stepValue, RaoResult raoResult) {
        this.stepValue = stepValue;
        this.raoResult = raoResult;
    }

    @Override
    public boolean isSecure() {
        if (raoResult.getComputationStatus() == ComputationStatus.FAILURE) {
            return false;
        }
        return raoResult.getFunctionalCost(OptimizationState.AFTER_CRA) <= 0;
    }

    @Override
    public double stepValue() {
        return stepValue;
    }
}
```

#### Implement a ValidationStrategy

```java
public class RaoValidationStrategy implements ValidationStrategy<RaoStepResult> {
    private final Network network;
    private final Crac crac;
    private final ZonalData<Scalable> scalables;
    private final Map<String, Double> splittingFactors;

    public RaoValidationStrategy(Network network, Crac crac, ZonalData<Scalable> scalables, Map<String, Double> splittingFactors) {
        this.network = network;
        this.crac = crac;
        this.scalables = scalables;
        this.splittingFactors = splittingFactors;
    }

    @Override
    public RaoStepResult validateStep(double stepValue) {
        Network duplicatedNetwork = duplicateNetwork(network);
        splittingFactors.forEach((zoneId, splittingFactor) -> scalables.getData(zoneId).scale(duplicatedNetwork, stepValue * splittingFactor));
        RaoInput raoInput = RaoInput.build(duplicatedNetwork, crac).build();
        RaoResult raoResult = Rao.run(raoInput);
        return new RaoStepResult(stepValue, raoResult);
    }

    private Network duplicateNetwork(Network network) {
        return NetworkXml.copy(network);
    }
}
```

Thanks to these basics implementation you could run a basic dichotomy on RAO runs.

## License

This project is licensed under the Mozilla Public License 2.0 - see the [LICENSE.txt](https://github.com/farao-community/farao-core/blob/master/LICENSE.txt) file for details.
 