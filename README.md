# FARAO - dichotomy
[![Actions Status](https://github.com/farao-community/farao-dichotomy/workflows/CI/badge.svg)](https://github.com/farao-community/farao-dichotomy/actions)
[![Coverage Status](https://sonarcloud.io/api/project_badges/measure?project=com.farao-community.farao%3Afarao-dichotomy&metric=coverage)](https://sonarcloud.io/component_measures?id=com.farao-community.farao%3Afarao-dichotomy&metric=coverage)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=com.farao-community.farao%3Afarao-dichotomy&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.farao-community.farao%3Afarao-dichotomy)
[![MPL-2.0 License](https://img.shields.io/badge/license-MPL_2.0-blue.svg)](https://www.mozilla.org/en-US/MPL/2.0/)
[![Join the community on Spectrum](https://withspectrum.github.io/badge/badge.svg)](https://spectrum.chat/farao-community)

## Overview

This repository is made of a main module **farao-dichotomy-api** that provides a ready-to-use **DichotomyEngine**. This engine already contains the basic requirements of a dichotomy. It runs on an IIDM network and at each iteration of the dichotomy it shifts the network and tries to validate it, each step resutl fills the index until the dichotomy precision is higher or equal to the step difference between highest valid step and lowest invalid step.
Two main interfaces are present in this module. First, **NetworkShifter** where you can find one implementation in the second module **farao-dichotomy-shift**. The only present implementation for now is a **LinearScaler** based on **SplittingFactors**, but it would be completed with further needs. The second interface is **NetworkValidator** this one has no implementation provided, clients will have to provide one. The method of this interafce will validate a Network according to the process requirements and will provide DichotomyStepResult with additional internal data if necessary.
This **DichotomyEngine** through its **DihotomyStepResult** can handle process specific results that would be accesible any time in the **DichotomyResults**. It is the role of **NetworkValidator** to fill them. 

## Quick implementations example

#### Dichotomy client
```java
public DichotomyResult<RaoResponse> runDichotomy(Network network) {
    DichotomyEngine<RaoResponse> engine = new DichotomyEngine<>(
        new Index<>(0, 10000, 50), 
        new StepsIndexStrategy(true, 650),
        new LinearScaler(glsk, new SplittingFactors(splittingfactors)),
        new MockNetworkValidator());
    return engine.run(network);
}
```
This could be a very simple implementation of a dichotomy. The index would be between 0 and 10.000, the dichotomy precison being 50. The index would start at the minimum index value incrementing by steps with a starting step of 650. Or you could also use a RangeDivisionStrategy that splits the available index interval in half at each iteration. To shift the network a basic LinearScaler is used based on a countryGlsk to associate groups/loads to a country variation and splitting factors are used to split the index (target value) between the countries. We will see what kind of Network Validator we could implement.

#### Implement a NetworkValidator

```java
public class RaoValidator implements NetworkValidator<RaoResponse> {
    private final RaoRunnerClient raoRunnerClient;

    public RaoValidator(RaoRunnerClient raoRunnerClient) {
        this.raoRunnerClient = raoRunnerClient;
    }

    @Override
    public DichotomyStepResult<RaoResponse> validateNetwork(Network network) throws ValidationException {
        RaoRequest raoRequest = buildRaoRequest(network);
        try {
            RaoResponse raoResponse = raoRunnerClient.runRao(raoRequest);
            RaoResult raoResult = importRaoResult(raoResponse.getRaoResultFileUrl());
            return DichotomyStepResult.fromNetworkValidationResult(raoResult, raoResponse);
        } catch (RuntimeException | IOException e) {
            throw new ValidationException("RAO run failed. Nested exception: " + e.getMessage());
        }
    }
    
    private RaoRequest buildRaoRequest(Network network) {
        ...
    }
    
    private RaoResult importRaoResult(String raoResultUrl) {
        ...
    }
}
```

Thanks to this basic implementation you could run a basic dichotomy that runs a RAO at each dichotomy step and fills the DichotomyStepResult with a specific RaResult. This way you could for exemple retrieve some information about what occured during the RAO of the highest valid step at the end of the dichotomy.

## License

This project is licensed under the Mozilla Public License 2.0 - see the [LICENSE.txt](https://github.com/farao-community/farao-core/blob/master/LICENSE.txt) file for details.
 
