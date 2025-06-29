/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy.api;

import com.farao_community.farao.dichotomy.api.exceptions.DichotomyException;
import com.farao_community.farao.dichotomy.api.exceptions.GlskLimitationException;
import com.farao_community.farao.dichotomy.api.exceptions.RaoFailureException;
import com.farao_community.farao.dichotomy.api.exceptions.RaoInterruptionException;
import com.farao_community.farao.dichotomy.api.exceptions.ShiftingException;
import com.farao_community.farao.dichotomy.api.exceptions.ValidationException;
import com.farao_community.farao.dichotomy.api.index.Index;
import com.farao_community.farao.dichotomy.api.index.IndexStrategy;
import com.farao_community.farao.dichotomy.api.index.RangeDivisionIndexStrategy;
import com.farao_community.farao.dichotomy.api.index.StepsIndexStrategy;
import com.farao_community.farao.dichotomy.api.results.DichotomyResult;
import com.farao_community.farao.dichotomy.api.results.DichotomyStepResult;
import com.farao_community.farao.dichotomy.api.results.ReasonInvalid;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManager;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 * @author Vincent Bochet {@literal <vincent.bochet at rte-france.com>}
 */
class DichotomyEngineTest {
    private static final double EPSILON = 1e-3;

    private Network initialNetwork;

    @BeforeEach
    void setUp() {
        final String networkFilename = "20210901_2230_test_network.uct";
        initialNetwork = Network.read(networkFilename, getClass().getResourceAsStream(networkFilename));
    }

    @Test
    void checkRangeDivisionIndexStrategyStartingWithMin() {
        final double limit = -340;
        final double minValue = -1000;
        final double maxValue = 1000;
        final double precision = 200;
        final Index<Object> index = new Index<>(minValue, maxValue, precision);
        final IndexStrategy<Object> indexStrategy = new RangeDivisionIndexStrategy<>(true);
        final NetworkValidator<Object> networkValidator = new NetworkValidatorMock(limit);
        final DichotomyEngine<Object> engine = DichotomyEngine.builder()
                .withIndex(index)
                .withIndexStrategy(indexStrategy)
                .withNetworkShifter(mock(NetworkShifter.class))
                .withNetworkValidator(networkValidator)
                .build();

        engine.run(initialNetwork);

        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(index.testedSteps()).hasSize(6);

        assertResultValidEquals(assertions, index.highestValidStep(), -375);
        assertResultInvalidEquals(assertions, index.lowestInvalidStep(), -250);

        assertResultValidEquals(assertions, index.testedSteps().get(0), -1000);
        assertResultInvalidEquals(assertions, index.testedSteps().get(1), 1000);
        assertResultInvalidEquals(assertions, index.testedSteps().get(2), 0);
        assertResultValidEquals(assertions, index.testedSteps().get(3), -500);
        assertResultInvalidEquals(assertions, index.testedSteps().get(4), -250);
        assertResultValidEquals(assertions, index.testedSteps().get(5), -375);
        assertions.assertAll();
    }

    @Test
    void checkRangeDivisionIndexStrategyStartingWithMax() {
        final double limit = -340;
        final double minValue = -1000;
        final double maxValue = 1000;
        final double precision = 200;
        final Index<Object> index = new Index<>(minValue, maxValue, precision);
        final IndexStrategy<Object> indexStrategy = new RangeDivisionIndexStrategy<>(false);
        final NetworkValidator<Object> networkValidator = new NetworkValidatorMock(limit);
        final DichotomyEngine<Object> engine = DichotomyEngine.builder()
                .withIndex(index)
                .withIndexStrategy(indexStrategy)
                .withNetworkShifter(mock(NetworkShifter.class))
                .withNetworkValidator(networkValidator)
                .build();

        engine.run(initialNetwork);

        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(index.testedSteps().size()).isEqualTo(6);

        assertResultValidEquals(assertions, index.highestValidStep(), -375);
        assertResultInvalidEquals(assertions, index.lowestInvalidStep(), -250);

        assertResultInvalidEquals(assertions, index.testedSteps().get(0), 1000);
        assertResultValidEquals(assertions, index.testedSteps().get(1), -1000);
        assertResultInvalidEquals(assertions, index.testedSteps().get(2), 0);
        assertResultValidEquals(assertions, index.testedSteps().get(3), -500);
        assertResultInvalidEquals(assertions, index.testedSteps().get(4), -250);
        assertResultValidEquals(assertions, index.testedSteps().get(5), -375);
        assertions.assertAll();
    }

    @Test
    void checkStepsIndexStrategyStartingWithMin() {
        final double limit = -340;
        final double minValue = -1000;
        final double maxValue = 1000;
        final double precision = 200;
        final double stepsSize = 400;
        final Index<Object> index = new Index<>(minValue, maxValue, precision);
        final IndexStrategy<Object> indexStrategy = new StepsIndexStrategy<>(true, stepsSize);
        final NetworkValidator<Object> networkValidator = new NetworkValidatorMock(limit);
        final DichotomyEngine<Object> engine = DichotomyEngine.builder()
                .withIndex(index)
                .withIndexStrategy(indexStrategy)
                .withNetworkShifter(mock(NetworkShifter.class))
                .withNetworkValidator(networkValidator)
                .build();

        engine.run(initialNetwork);

        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(index.testedSteps().size()).isEqualTo(4);

        assertResultValidEquals(assertions, index.highestValidStep(), -400);
        assertResultInvalidEquals(assertions, index.lowestInvalidStep(), -200);

        assertResultValidEquals(assertions, index.testedSteps().get(0), -1000);
        assertResultValidEquals(assertions, index.testedSteps().get(1), -600);
        assertResultInvalidEquals(assertions, index.testedSteps().get(2), -200);
        assertResultValidEquals(assertions, index.testedSteps().get(3), -400);
        assertions.assertAll();
    }

    @Test
    void checkStepsIndexStrategyStartingWithMax() {
        final double limit = -340;
        final double minValue = -1000;
        final double maxValue = 1000;
        final double precision = 200;
        final double stepsSize = 400;
        final Index<Object> index = new Index<>(minValue, maxValue, precision);
        final IndexStrategy<Object> indexStrategy = new StepsIndexStrategy<>(false, stepsSize);
        final NetworkValidator<Object> networkValidator = new NetworkValidatorMock(limit);
        final DichotomyEngine<Object> engine = DichotomyEngine.builder()
                .withIndex(index)
                .withIndexStrategy(indexStrategy)
                .withNetworkShifter(mock(NetworkShifter.class))
                .withNetworkValidator(networkValidator)
                .build();

        engine.run(initialNetwork);

        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(index.testedSteps().size()).isEqualTo(6);

        assertResultValidEquals(assertions, index.highestValidStep(), -400);
        assertResultInvalidEquals(assertions, index.lowestInvalidStep(), -200);

        assertResultInvalidEquals(assertions, index.testedSteps().get(0), 1000);
        assertResultInvalidEquals(assertions, index.testedSteps().get(1), 600);
        assertResultInvalidEquals(assertions, index.testedSteps().get(2), 200);
        assertResultInvalidEquals(assertions, index.testedSteps().get(3), -200);
        assertResultValidEquals(assertions, index.testedSteps().get(4), -600);
        assertResultValidEquals(assertions, index.testedSteps().get(5), -400);
        assertions.assertAll();
    }

    @Test
    void checkDichotomyEngineStopsAtMaxIterations() {
        final double limit = -340;
        final double minValue = -1000;
        final double maxValue = 1000;
        final double precision = 200;
        final int maxIterations = 5;
        final Index<Object> index = new Index<>(minValue, maxValue, precision);
        final IndexStrategy<Object> indexStrategy = new RangeDivisionIndexStrategy<>(true);
        final NetworkValidator<Object> networkValidator = new NetworkValidatorMock(limit);
        final DichotomyEngine<Object> engine = DichotomyEngine.builder()
                .withIndex(index)
                .withIndexStrategy(indexStrategy)
                .withNetworkShifter(mock(NetworkShifter.class))
                .withNetworkValidator(networkValidator)
                .withMaxIteration(maxIterations)
                .build();

        engine.run(initialNetwork);

        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(index.testedSteps().size()).isEqualTo(5);

        assertResultValidEquals(assertions, index.highestValidStep(), -500);
        assertResultInvalidEquals(assertions, index.lowestInvalidStep(), -250);

        assertResultValidEquals(assertions, index.testedSteps().get(0), -1000);
        assertResultInvalidEquals(assertions, index.testedSteps().get(1), 1000);
        assertResultInvalidEquals(assertions, index.testedSteps().get(2), 0);
        assertResultValidEquals(assertions, index.testedSteps().get(3), -500);
        assertResultInvalidEquals(assertions, index.testedSteps().get(4), -250);
        assertions.assertAll();
    }

    @Test
    void checkAllSecure() {
        final double limit = 1500;
        final double minValue = -1000;
        final double maxValue = 1000;
        final double precision = 200;
        final Index<Object> index = new Index<>(minValue, maxValue, precision);
        final IndexStrategy<Object> indexStrategy = new RangeDivisionIndexStrategy<>(true);
        final NetworkValidator<Object> networkValidator = new NetworkValidatorMock(limit);
        final DichotomyEngine<Object> engine = DichotomyEngine.builder()
                .withIndex(index)
                .withIndexStrategy(indexStrategy)
                .withNetworkShifter(mock(NetworkShifter.class))
                .withNetworkValidator(networkValidator)
                .build();

        engine.run(initialNetwork);

        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(index.testedSteps().size()).isEqualTo(2);

        assertResultValidEquals(assertions, index.highestValidStep(), 1000);
        assertions.assertThat(index.lowestInvalidStep()).isNull();

        assertResultValidEquals(assertions, index.testedSteps().get(0), -1000);
        assertResultValidEquals(assertions, index.testedSteps().get(1), 1000);
        assertions.assertAll();
    }

    @Test
    void checkAllUnsecure() {
        final double limit = -1500;
        final double minValue = -1000;
        final double maxValue = 1000;
        final double precision = 200;
        final Index<Object> index = new Index<>(minValue, maxValue, precision);
        final IndexStrategy<Object> indexStrategy = new RangeDivisionIndexStrategy<>(true);
        final NetworkValidator<Object> networkValidator = new NetworkValidatorMock(limit);
        final DichotomyEngine<Object> engine = DichotomyEngine.builder()
                .withIndex(index)
                .withIndexStrategy(indexStrategy)
                .withNetworkShifter(mock(NetworkShifter.class))
                .withNetworkValidator(networkValidator)
                .build();

        engine.run(initialNetwork);

        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(index.testedSteps().size()).isEqualTo(1);

        assertions.assertThat(index.highestValidStep()).isNull();
        assertResultInvalidEquals(assertions, index.lowestInvalidStep(), -1000);

        assertResultInvalidEquals(assertions, index.testedSteps().getFirst(), -1000);
        assertions.assertAll();
    }

    @Test
    void checkThatEngineFailsWhenMaxIterationTooLow() {
        final double limit = -1500;
        final double minValue = -1000;
        final double maxValue = 1000;
        final double precision = 200;
        final Index<Object> index = new Index<>(minValue, maxValue, precision);
        final IndexStrategy<Object> indexStrategy = new RangeDivisionIndexStrategy<>(true);
        final NetworkValidator<Object> networkValidator = new NetworkValidatorMock(limit);

        final DichotomyEngine.Builder<Object> builder = DichotomyEngine.builder()
                .withIndex(index)
                .withIndexStrategy(indexStrategy)
                .withNetworkShifter(mock(NetworkShifter.class))
                .withNetworkValidator(networkValidator)
                .withMaxIteration(2);

        Assertions.assertThatExceptionOfType(DichotomyException.class)
                .isThrownBy(builder::build);
    }

    @Test
    void checkThatStepsIndexStrategyCreationFailsWhenStepsSizeNegative() {
        Assertions.assertThatExceptionOfType(DichotomyException.class)
                        .isThrownBy(() -> new StepsIndexStrategy<>(true, -10));
        Assertions.assertThatExceptionOfType(DichotomyException.class)
                        .isThrownBy(() -> new StepsIndexStrategy<>(true, 0));
    }

    @Test
    void checkDichotomyEngineChecksLimitsWhenIntervalSmallerThanPrecision() {
        final double limit = 0;
        final double minValue = -50;
        final double maxValue = 50;
        final double precision = 200;
        final int maxIterations = 5;
        final Index<Object> index = new Index<>(minValue, maxValue, precision);
        final IndexStrategy<Object> indexStrategy = new RangeDivisionIndexStrategy<>(true);
        final NetworkValidator<Object> networkValidator = new NetworkValidatorMock(limit);
        final DichotomyEngine<Object> engine = DichotomyEngine.builder()
                .withIndex(index)
                .withIndexStrategy(indexStrategy)
                .withNetworkShifter(mock(NetworkShifter.class))
                .withNetworkValidator(networkValidator)
                .withMaxIteration(maxIterations)
                .build();

        engine.run(initialNetwork);

        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(index.testedSteps().size()).isEqualTo(2);

        assertResultValidEquals(assertions, index.highestValidStep(), -50);
        assertResultInvalidEquals(assertions, index.lowestInvalidStep(), 50);

        assertResultValidEquals(assertions, index.testedSteps().get(0), -50);
        assertResultInvalidEquals(assertions, index.testedSteps().get(1), 50);
        assertions.assertAll();
    }

    @Test
    void checkDichotomyEngineStopEarlyWhenIntervalSmallerThanPrecisionButMinUnsecure() {
        final double limit = -100;
        final double minValue = -50;
        final double maxValue = 50;
        final double precision = 100;
        final int maxIterations = 5;
        final Index<Object> index = new Index<>(minValue, maxValue, precision);
        final IndexStrategy<Object> indexStrategy = new RangeDivisionIndexStrategy<>(true);
        final NetworkValidator<Object> networkValidator = new NetworkValidatorMock(limit);
        final DichotomyEngine<Object> engine = DichotomyEngine.builder()
                .withIndex(index)
                .withIndexStrategy(indexStrategy)
                .withNetworkShifter(mock(NetworkShifter.class))
                .withNetworkValidator(networkValidator)
                .withMaxIteration(maxIterations)
                .build();

        engine.run(initialNetwork);

        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(index.testedSteps().size()).isEqualTo(1);

        assertions.assertThat(index.highestValidStep()).isNull();
        assertResultInvalidEquals(assertions, index.lowestInvalidStep(), -50);

        assertResultInvalidEquals(assertions, index.testedSteps().getFirst(), -50);
        assertions.assertAll();
    }

    @Test
    void checkDichotomyEngineStopEarlyWhenIntervalSmallerThanPrecisionButMaxSecure() {
        final double limit = 100;
        final double minValue = -50;
        final double maxValue = 50;
        final double precision = 100;
        final int maxIterations = 5;
        final Index<Object> index = new Index<>(minValue, maxValue, precision);
        final IndexStrategy<Object> indexStrategy = new RangeDivisionIndexStrategy<>(false);
        final NetworkValidator<Object> networkValidator = new NetworkValidatorMock(limit);
        final DichotomyEngine<Object> engine = DichotomyEngine.builder()
                .withIndex(index)
                .withIndexStrategy(indexStrategy)
                .withNetworkShifter(mock(NetworkShifter.class))
                .withNetworkValidator(networkValidator)
                .withMaxIteration(maxIterations)
                .build();

        engine.run(initialNetwork);

        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(index.testedSteps().size()).isEqualTo(1);

        assertions.assertThat(index.lowestInvalidStep()).isNull();
        assertResultValidEquals(assertions, index.highestValidStep(), 50);

        assertResultValidEquals(assertions, index.testedSteps().getFirst(), 50);
        assertions.assertAll();
    }

    @Test
    void checkSoftInterruptionNotInterrupted() {
        final double limit = -340;
        final double minValue = -1000;
        final double maxValue = 1000;
        final double precision = 200;
        final double stepsSize = 400;
        final Index<Object> index = new Index<>(minValue, maxValue, precision);
        final IndexStrategy<Object> indexStrategy = new StepsIndexStrategy<>(true, stepsSize);
        final NetworkValidator<Object> networkValidator = new NetworkValidatorMock(limit);
        final InterruptionStrategy interruptionStrategy = mock(InterruptionStrategy.class);
        when(interruptionStrategy.shouldRunBeInterruptedSoftly("id")).thenReturn(false);
        final DichotomyEngine<Object> engine = DichotomyEngine.builder()
                .withIndex(index)
                .withIndexStrategy(indexStrategy)
                .withInterruptionStrategy(interruptionStrategy)
                .withNetworkShifter(mock(NetworkShifter.class))
                .withNetworkValidator(networkValidator)
                .withRunId("id")
                .build();

        final DichotomyResult<Object> dichotomyResult = engine.run(initialNetwork);

        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(dichotomyResult.isInterrupted()).isFalse();
        assertions.assertThat(index.testedSteps().size()).isEqualTo(4);

        assertResultValidEquals(assertions, index.highestValidStep(), -400);
        assertResultInvalidEquals(assertions, index.lowestInvalidStep(), -200);

        assertResultValidEquals(assertions, index.testedSteps().get(0), -1000);
        assertResultValidEquals(assertions, index.testedSteps().get(1), -600);
        assertResultInvalidEquals(assertions, index.testedSteps().get(2), -200);
        assertResultValidEquals(assertions, index.testedSteps().get(3), -400);
        assertions.assertAll();
    }

    @Test
    void checkSoftInterruptionBeforeFirstRao() {
        final double limit = -340;
        final double minValue = -1000;
        final double maxValue = 1000;
        final double precision = 200;
        final double stepsSize = 400;
        final Index<Object> index = new Index<>(minValue, maxValue, precision);
        final IndexStrategy<Object> indexStrategy = new StepsIndexStrategy<>(true, stepsSize);
        final NetworkValidator<Object> networkValidator = new NetworkValidatorMock(limit);
        final InterruptionStrategy interruptionStrategy = mock(InterruptionStrategy.class);
        when(interruptionStrategy.shouldRunBeInterruptedSoftly("id")).thenReturn(true);
        final DichotomyEngine<Object> engine = DichotomyEngine.builder()
                .withIndex(index)
                .withIndexStrategy(indexStrategy)
                .withInterruptionStrategy(interruptionStrategy)
                .withNetworkShifter(mock(NetworkShifter.class))
                .withNetworkValidator(networkValidator)
                .withRunId("id")
                .build();

        final DichotomyResult<Object> dichotomyResult = engine.run(initialNetwork);

        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(dichotomyResult.isInterrupted()).isTrue();
        assertions.assertThat(index.testedSteps().size()).isEqualTo(0);
        assertions.assertAll();
    }

    @Test
    void checkSoftInterruptionBetweenFirstAndSecondRao() {
        final double limit = -340;
        final double minValue = -1000;
        final double maxValue = 1000;
        final double precision = 200;
        final double stepsSize = 400;
        final Index<Object> index = new Index<>(minValue, maxValue, precision);
        final IndexStrategy<Object> indexStrategy = new StepsIndexStrategy<>(true, stepsSize);
        final NetworkValidator<Object> networkValidator = new NetworkValidatorMock(limit);
        final InterruptionStrategy interruptionStrategy = mock(InterruptionStrategy.class);
        when(interruptionStrategy.shouldRunBeInterruptedSoftly("id")).thenReturn(false).thenReturn(true);
        final DichotomyEngine<Object> engine = DichotomyEngine.builder()
                .withIndex(index)
                .withIndexStrategy(indexStrategy)
                .withInterruptionStrategy(interruptionStrategy)
                .withNetworkShifter(mock(NetworkShifter.class))
                .withNetworkValidator(networkValidator)
                .withRunId("id")
                .build();

        final DichotomyResult<Object> dichotomyResult = engine.run(initialNetwork);

        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(dichotomyResult.isInterrupted()).isTrue();
        assertions.assertThat(index.testedSteps().size()).isEqualTo(1);
        assertions.assertAll();
    }

    @Test
    void checkSoftInterruptionDuringRao() throws ValidationException, RaoInterruptionException, RaoFailureException {
        final double minValue = -1000;
        final double maxValue = 1000;
        final double precision = 200;
        final double stepsSize = 400;
        final Index<Object> index = new Index<>(minValue, maxValue, precision);
        final IndexStrategy<Object> indexStrategy = new StepsIndexStrategy<>(false, stepsSize);
        final NetworkValidator<Object> networkValidator = mock(NetworkValidator.class);
        when(networkValidator.validateNetwork(any(), any())).thenThrow(new RaoInterruptionException("test"));
        final InterruptionStrategy interruptionStrategy = mock(InterruptionStrategy.class);
        when(interruptionStrategy.shouldRunBeInterruptedSoftly("id")).thenReturn(false).thenReturn(true);
        final DichotomyEngine<Object> engine = DichotomyEngine.builder()
                .withIndex(index)
                .withIndexStrategy(indexStrategy)
                .withInterruptionStrategy(interruptionStrategy)
                .withNetworkShifter(mock(NetworkShifter.class))
                .withNetworkValidator(networkValidator)
                .withRunId("id")
                .build();

        final DichotomyResult<Object> dichotomyResult = engine.run(initialNetwork);

        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(dichotomyResult.isInterrupted()).isTrue();
        assertions.assertThat(index.testedSteps().size()).isEqualTo(0);
        assertions.assertAll();
    }

    @Test
    void checkRaoFailure() throws ValidationException, RaoInterruptionException, RaoFailureException {
        final String failureMessage = "test";
        final double minValue = -1000;
        final double maxValue = 1000;
        final double precision = 200;
        final double stepsSize = 400;
        final Index<Object> index = new Index<>(minValue, maxValue, precision);
        final IndexStrategy<Object> indexStrategy = new StepsIndexStrategy<>(false, stepsSize);
        final NetworkValidator<Object> networkValidator = mock(NetworkValidator.class);
        when(networkValidator.validateNetwork(any(), any())).thenThrow(new RaoFailureException(failureMessage));
        final InterruptionStrategy interruptionStrategy = mock(InterruptionStrategy.class);
        when(interruptionStrategy.shouldRunBeInterruptedSoftly("id")).thenReturn(false);
        final DichotomyEngine<Object> engine = DichotomyEngine.builder()
                .withIndex(index)
                .withIndexStrategy(indexStrategy)
                .withInterruptionStrategy(interruptionStrategy)
                .withNetworkShifter(mock(NetworkShifter.class))
                .withNetworkValidator(networkValidator)
                .withRunId("id")
                .build();

        final DichotomyResult<Object> dichotomyResult = engine.run(initialNetwork);

        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(dichotomyResult.isRaoFailed()).isTrue();
        assertions.assertThat(dichotomyResult.getRaoFailureMessage()).isEqualTo(failureMessage);
        assertions.assertAll();
    }

    private static void assertResultValidEquals(SoftAssertions assertions, Pair<Double, DichotomyStepResult<Object>> index, int expected) {
        assertions.assertThat(index.getLeft()).isEqualTo(expected, Assertions.withPrecision(EPSILON));
        assertions.assertThat(index.getRight().isValid()).isTrue();
    }

    private static void assertResultInvalidEquals(SoftAssertions assertions, Pair<Double, DichotomyStepResult<Object>> index, int expected) {
        assertions.assertThat(index.getLeft()).isEqualTo(expected, Assertions.withPrecision(EPSILON));
        assertions.assertThat(index.getRight().isValid()).isFalse();
    }

    @Test
    void validateThrowsGlskLimitationException() throws GlskLimitationException, ShiftingException, RaoFailureException {
        final double stepValue = 1600d;
        final String initialVariantName = "initialVariant";
        final NetworkShifter networkShifter = mock(NetworkShifter.class);
        final Network network = mock(Network.class);
        final VariantManager variantManager = mock(VariantManager.class);
        final DichotomyStepResult<Object> lastDichotomyStepResult = mock(DichotomyStepResult.class);
        final Index<Object> index = mock(Index.class);
        final IndexStrategy<Object> indexStrategy = mock(IndexStrategy.class);
        final NetworkValidator<Object> networkValidator = mock(NetworkValidator.class);
        final DichotomyEngine<Object> engine = DichotomyEngine.builder()
                .withIndex(index)
                .withIndexStrategy(indexStrategy)
                .withNetworkShifter(networkShifter)
                .withNetworkValidator(networkValidator)
                .build();

        when(network.getVariantManager()).thenReturn(variantManager);
        when(network.getVariantManager()).thenReturn(variantManager);
        Mockito.doNothing().when(variantManager).cloneVariant(eq(initialVariantName), anyString());
        Mockito.doNothing().when(variantManager).setWorkingVariant(anyString());
        Mockito.doThrow(GlskLimitationException.class).when(networkShifter).shiftNetwork(stepValue, network);

        final DichotomyStepResult<Object> result = engine.validate(stepValue, network, initialVariantName, lastDichotomyStepResult);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getReasonInvalid()).isEqualTo(ReasonInvalid.GLSK_LIMITATION);
    }

    @ParameterizedTest
    @EnumSource(value = ReasonInvalid.class,
            names = {"BALANCE_LOADFLOW_DIVERGENCE", "UNKNOWN_TERMINAL_BUS"})
    void validateThrowsShiftingException(final ReasonInvalid reasonInvalid) throws GlskLimitationException, ShiftingException, RaoFailureException {
        final double stepValue = 1600d;
        final String initialVariantName = "initialVariant";
        final NetworkShifter networkShifter = mock(NetworkShifter.class);
        final Network network = mock(Network.class);
        final VariantManager variantManager = mock(VariantManager.class);
        final DichotomyStepResult<Object> lastDichotomyStepResult = mock(DichotomyStepResult.class);
        final Index<Object> index = mock(Index.class);
        final IndexStrategy<Object> indexStrategy = mock(IndexStrategy.class);
        final NetworkValidator<Object> networkValidator = mock(NetworkValidator.class);
        final DichotomyEngine<Object> engine = DichotomyEngine.builder()
                .withIndex(index)
                .withIndexStrategy(indexStrategy)
                .withNetworkShifter(networkShifter)
                .withNetworkValidator(networkValidator)
                .build();

        when(network.getVariantManager()).thenReturn(variantManager);
        when(network.getVariantManager()).thenReturn(variantManager);
        Mockito.doNothing().when(variantManager).cloneVariant(eq(initialVariantName), anyString());
        Mockito.doNothing().when(variantManager).setWorkingVariant(anyString());
        Mockito.doThrow(new ShiftingException("Error message", reasonInvalid)).when(networkShifter).shiftNetwork(stepValue, network);

        final DichotomyStepResult<Object> result = engine.validate(stepValue, network, initialVariantName, lastDichotomyStepResult);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getReasonInvalid()).isEqualTo(reasonInvalid);
    }

    @Test
    void validateThrowsShiftingExceptionAndExportsNetwork() throws GlskLimitationException, ShiftingException, RaoFailureException {
        final ReasonInvalid reasonInvalid = ReasonInvalid.BALANCE_LOADFLOW_DIVERGENCE;
        final double stepValue = 1600d;
        final String initialVariantName = "initialVariant";
        final NetworkShifter networkShifter = mock(NetworkShifter.class);
        final NetworkExporter networkExporter = mock(NetworkExporter.class);
        final Network network = mock(Network.class);
        final VariantManager variantManager = mock(VariantManager.class);
        final DichotomyStepResult<Object> lastDichotomyStepResult = mock(DichotomyStepResult.class);
        final Index<Object> index = mock(Index.class);
        final IndexStrategy<Object> indexStrategy = mock(IndexStrategy.class);
        final NetworkValidator<Object> networkValidator = mock(NetworkValidator.class);
        final DichotomyEngine<Object> engine = DichotomyEngine.builder()
                .withIndex(index)
                .withIndexStrategy(indexStrategy)
                .withNetworkShifter(networkShifter)
                .withNetworkValidator(networkValidator)
                .withNetworkExporter(networkExporter)
                .withRunId("0")
                .build();

        when(network.getVariantManager()).thenReturn(variantManager);
        when(network.getVariantManager()).thenReturn(variantManager);
        Mockito.doNothing().when(variantManager).cloneVariant(eq(initialVariantName), anyString());
        Mockito.doNothing().when(variantManager).setWorkingVariant(anyString());
        Mockito.doThrow(new ShiftingException("Error message", reasonInvalid)).when(networkShifter).shiftNetwork(stepValue, network);

        final DichotomyStepResult<Object> result = engine.validate(stepValue, network, initialVariantName, lastDichotomyStepResult);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getReasonInvalid()).isEqualTo(reasonInvalid);
        Mockito.verify(networkExporter, Mockito.times(1)).export(network);
    }

    @Test
    void validateThrowsShiftingExceptionAndDoesNotExportNetwork() throws GlskLimitationException, ShiftingException, RaoFailureException {
        final ReasonInvalid reasonInvalid = ReasonInvalid.UNKNOWN_TERMINAL_BUS;
        final double stepValue = 1600d;
        final String initialVariantName = "initialVariant";
        final NetworkShifter networkShifter = mock(NetworkShifter.class);
        final NetworkExporter networkExporter = mock(NetworkExporter.class);
        final Network network = mock(Network.class);
        final VariantManager variantManager = mock(VariantManager.class);
        final DichotomyStepResult<Object> lastDichotomyStepResult = mock(DichotomyStepResult.class);
        final Index<Object> index = mock(Index.class);
        final IndexStrategy<Object> indexStrategy = mock(IndexStrategy.class);
        final NetworkValidator<Object> networkValidator = mock(NetworkValidator.class);
        final DichotomyEngine<Object> engine = DichotomyEngine.builder()
                .withIndex(index)
                .withIndexStrategy(indexStrategy)
                .withNetworkShifter(networkShifter)
                .withNetworkValidator(networkValidator)
                .withNetworkExporter(networkExporter)
                .withRunId("0")
                .build();

        when(network.getVariantManager()).thenReturn(variantManager);
        when(network.getVariantManager()).thenReturn(variantManager);
        Mockito.doNothing().when(variantManager).cloneVariant(eq(initialVariantName), anyString());
        Mockito.doNothing().when(variantManager).setWorkingVariant(anyString());
        Mockito.doThrow(new ShiftingException("Error message", reasonInvalid)).when(networkShifter).shiftNetwork(stepValue, network);

        final DichotomyStepResult<Object> result = engine.validate(stepValue, network, initialVariantName, lastDichotomyStepResult);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getReasonInvalid()).isEqualTo(reasonInvalid);
        Mockito.verify(networkExporter, Mockito.never()).export(network);
    }

    @Test
    void validateThrowsShiftingExceptionDefault() throws GlskLimitationException, ShiftingException, RaoFailureException {
        final double stepValue = 1600d;
        final String initialVariantName = "initialVariant";
        final NetworkShifter networkShifter = mock(NetworkShifter.class);
        final Network network = mock(Network.class);
        final VariantManager variantManager = mock(VariantManager.class);
        final DichotomyStepResult<Object> lastDichotomyStepResult = mock(DichotomyStepResult.class);
        final Index<Object> index = mock(Index.class);
        final IndexStrategy<Object> indexStrategy = mock(IndexStrategy.class);
        final NetworkValidator<Object> networkValidator = mock(NetworkValidator.class);
        final DichotomyEngine<Object> engine = DichotomyEngine.builder()
                .withIndex(index)
                .withIndexStrategy(indexStrategy)
                .withNetworkShifter(networkShifter)
                .withNetworkValidator(networkValidator)
                .build();

        when(network.getVariantManager()).thenReturn(variantManager);
        when(network.getVariantManager()).thenReturn(variantManager);
        Mockito.doNothing().when(variantManager).cloneVariant(eq(initialVariantName), anyString());
        Mockito.doNothing().when(variantManager).setWorkingVariant(anyString());
        Mockito.doThrow(new ShiftingException("Error message")).when(networkShifter).shiftNetwork(stepValue, network);

        final DichotomyStepResult<Object> result = engine.validate(stepValue, network, initialVariantName, lastDichotomyStepResult);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getReasonInvalid()).isEqualTo(ReasonInvalid.VALIDATION_FAILED);
    }

    @Test
    void validateThrowsValidationException() throws GlskLimitationException, ShiftingException, ValidationException, RaoInterruptionException, RaoFailureException {
        final double stepValue = 1600d;
        final String initialVariantName = "initialVariant";
        final NetworkShifter networkShifter = mock(NetworkShifter.class);
        final Network network = mock(Network.class);
        final VariantManager variantManager = mock(VariantManager.class);
        final DichotomyStepResult<Object> lastDichotomyStepResult = mock(DichotomyStepResult.class);
        final Index<Object> index = mock(Index.class);
        final IndexStrategy<Object> indexStrategy = mock(IndexStrategy.class);
        final NetworkValidator<Object> networkValidator = mock(NetworkValidator.class);
        final DichotomyEngine<Object> engine = DichotomyEngine.builder()
                .withIndex(index)
                .withIndexStrategy(indexStrategy)
                .withNetworkShifter(networkShifter)
                .withNetworkValidator(networkValidator)
                .build();

        when(network.getVariantManager()).thenReturn(variantManager);
        when(network.getVariantManager()).thenReturn(variantManager);
        Mockito.doNothing().when(variantManager).cloneVariant(eq(initialVariantName), anyString());
        Mockito.doNothing().when(variantManager).setWorkingVariant(anyString());
        Mockito.doNothing().when(networkShifter).shiftNetwork(stepValue, network);
        Mockito.when(networkValidator.validateNetwork(network, lastDichotomyStepResult)).thenThrow(ValidationException.class);

        final DichotomyStepResult<Object> result = engine.validate(stepValue, network, initialVariantName, lastDichotomyStepResult);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getReasonInvalid()).isEqualTo(ReasonInvalid.VALIDATION_FAILED);
    }

    @Test
    void validateThrowsRaoInterruptionException() throws GlskLimitationException, ShiftingException, ValidationException, RaoInterruptionException, RaoFailureException {
        final double stepValue = 1600d;
        final String initialVariantName = "initialVariant";
        final NetworkShifter networkShifter = mock(NetworkShifter.class);
        final Network network = mock(Network.class);
        final VariantManager variantManager = mock(VariantManager.class);
        final DichotomyStepResult<Object> lastDichotomyStepResult = mock(DichotomyStepResult.class);
        final Index<Object> index = mock(Index.class);
        final IndexStrategy<Object> indexStrategy = mock(IndexStrategy.class);
        final NetworkValidator<Object> networkValidator = mock(NetworkValidator.class);
        final DichotomyEngine<Object> engine = DichotomyEngine.builder()
                .withIndex(index)
                .withIndexStrategy(indexStrategy)
                .withNetworkShifter(networkShifter)
                .withNetworkValidator(networkValidator)
                .build();

        when(network.getVariantManager()).thenReturn(variantManager);
        when(network.getVariantManager()).thenReturn(variantManager);
        Mockito.doNothing().when(variantManager).cloneVariant(eq(initialVariantName), anyString());
        Mockito.doNothing().when(variantManager).setWorkingVariant(anyString());
        Mockito.doNothing().when(networkShifter).shiftNetwork(stepValue, network);
        Mockito.when(networkValidator.validateNetwork(network, lastDichotomyStepResult)).thenThrow(RaoInterruptionException.class);

        final DichotomyStepResult<Object> result = engine.validate(stepValue, network, initialVariantName, lastDichotomyStepResult);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getReasonInvalid()).isEqualTo(ReasonInvalid.RAO_INTERRUPTION);
    }

    @Test
    void checkBuilderDefaultValues() {
        final Index<Object> index = mock(Index.class);
        final IndexStrategy<Object> indexStrategy = mock(IndexStrategy.class);
        final NetworkShifter networkShifter = mock(NetworkShifter.class);
        final NetworkValidator<Object> networkValidator = mock(NetworkValidator.class);

        final DichotomyEngine<Object> engine = DichotomyEngine.builder()
                .withIndex(index)
                .withIndexStrategy(indexStrategy)
                .withNetworkShifter(networkShifter)
                .withNetworkValidator(networkValidator)
                .build();

        Assertions.assertThat(engine)
                .isNotNull()
                .hasFieldOrPropertyWithValue("interruptionStrategy", null)
                .hasFieldOrPropertyWithValue("networkExporter", null)
                .hasFieldOrPropertyWithValue("maxIteration", 100)
                .hasFieldOrPropertyWithValue("runId", null);
    }

    @Test
    void checkBuilderValues() {
        final Index<Object> index = mock(Index.class);
        final IndexStrategy<Object> indexStrategy = mock(IndexStrategy.class);
        final InterruptionStrategy interruptionStrategy = mock(InterruptionStrategy.class);
        final NetworkValidator<Object> networkValidator = mock(NetworkValidator.class);
        final NetworkShifter networkShifter = mock(NetworkShifter.class);
        final NetworkExporter networkExporter = mock(NetworkExporter.class);

        final DichotomyEngine<Object> engine = DichotomyEngine.builder()
                .withIndex(index)
                .withIndexStrategy(indexStrategy)
                .withInterruptionStrategy(interruptionStrategy)
                .withNetworkShifter(networkShifter)
                .withNetworkValidator(networkValidator)
                .withNetworkExporter(networkExporter)
                .withMaxIteration(72)
                .withRunId("0")
                .build();

        Assertions.assertThat(engine)
                .isNotNull()
                .hasFieldOrPropertyWithValue("index", index)
                .hasFieldOrPropertyWithValue("indexStrategy", indexStrategy)
                .hasFieldOrPropertyWithValue("interruptionStrategy", interruptionStrategy)
                .hasFieldOrPropertyWithValue("networkShifter", networkShifter)
                .hasFieldOrPropertyWithValue("networkValidator", networkValidator)
                .hasFieldOrPropertyWithValue("networkExporter", networkExporter)
                .hasFieldOrPropertyWithValue("maxIteration", 72)
                .hasFieldOrPropertyWithValue("runId", "0");
    }

    @Test
    void checkBuilderRequiredValues() {
        final Index<Object> index = mock(Index.class);
        final IndexStrategy<Object> indexStrategy = mock(IndexStrategy.class);
        final NetworkValidator<Object> networkValidator = mock(NetworkValidator.class);

        final DichotomyEngine.Builder<Object> builderWithoutIndex = DichotomyEngine.builder()
                .withIndexStrategy(indexStrategy)
                .withNetworkValidator(networkValidator);

        final DichotomyEngine.Builder<Object> builderWithoutIndesStrategy = DichotomyEngine.builder()
                .withIndex(index)
                .withNetworkValidator(networkValidator);

        final DichotomyEngine.Builder<Object> builderWithoutNetworkValidator = DichotomyEngine.builder()
                .withIndex(index)
                .withIndexStrategy(indexStrategy);

        Assertions.assertThatExceptionOfType(NullPointerException.class).isThrownBy(builderWithoutIndex::build);
        Assertions.assertThatExceptionOfType(NullPointerException.class).isThrownBy(builderWithoutIndesStrategy::build);
        Assertions.assertThatExceptionOfType(NullPointerException.class).isThrownBy(builderWithoutNetworkValidator::build);
    }
}
