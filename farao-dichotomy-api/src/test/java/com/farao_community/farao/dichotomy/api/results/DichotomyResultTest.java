package com.farao_community.farao.dichotomy.api.results;

import com.farao_community.farao.dichotomy.api.index.Index;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;

class DichotomyResultTest {
    @Test
    void noInvalidStep() {
        final Index<Object> index = Mockito.mock(Index.class);
        final DichotomyStepResult<Object> highestValidStepResult = Mockito.mock(DichotomyStepResult.class);
        final double highestValidStepValue = 27d;
        Mockito.when(index.highestValidStep()).thenReturn(Pair.of(highestValidStepValue, highestValidStepResult));
        Mockito.when(index.lowestInvalidStep()).thenReturn(null);

        final DichotomyResult<Object> result = DichotomyResult.buildFromIndex(index);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.hasValidStep()).isTrue();
        Assertions.assertThat(result.isInterrupted()).isFalse();
        Assertions.assertThat(result.getHighestValidStep()).isEqualTo(highestValidStepResult);
        Assertions.assertThat(result.getHighestValidStepValue()).isEqualTo(highestValidStepValue);
        Assertions.assertThat(result.getLowestInvalidStep()).isNull();
        Assertions.assertThat(result.getLowestInvalidStepValue()).isNaN();
        Assertions.assertThat(result.getLimitingCause()).isEqualTo(LimitingCause.INDEX_EVALUATION_OR_MAX_ITERATION);
        Assertions.assertThat(result.getLimitingFailureMessage()).isEqualTo("None");
    }

    @Test
    void noValidStep() {
        final Index<Object> index = Mockito.mock(Index.class);
        final DichotomyStepResult<Object> lowestInvalidStepResult = Mockito.mock(DichotomyStepResult.class);
        final double lowestInvalidStepValue = -12d;
        Mockito.when(index.highestValidStep()).thenReturn(null);
        Mockito.when(index.lowestInvalidStep()).thenReturn(Pair.of(lowestInvalidStepValue, lowestInvalidStepResult));

        final DichotomyResult<Object> result = DichotomyResult.buildFromIndex(index);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.hasValidStep()).isFalse();
        Assertions.assertThat(result.isInterrupted()).isFalse();
        Assertions.assertThat(result.getHighestValidStep()).isNull();
        Assertions.assertThat(result.getHighestValidStepValue()).isNaN();
        Assertions.assertThat(result.getLowestInvalidStep()).isEqualTo(lowestInvalidStepResult);
        Assertions.assertThat(result.getLowestInvalidStepValue()).isEqualTo(lowestInvalidStepValue);
        Assertions.assertThat(result.getLimitingCause()).isEqualTo(LimitingCause.INDEX_EVALUATION_OR_MAX_ITERATION);
        Assertions.assertThat(result.getLimitingFailureMessage()).isEqualTo("None");
    }

    @Test
    void interruptedDichotomy() {
        final Index<Object> index = Mockito.mock(Index.class);
        Mockito.when(index.highestValidStep()).thenReturn(null);
        Mockito.when(index.lowestInvalidStep()).thenReturn(null);

        final DichotomyResult<Object> result = DichotomyResult.buildFromIndex(index);
        result.setInterrupted(true);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.hasValidStep()).isFalse();
        Assertions.assertThat(result.isInterrupted()).isTrue();
        Assertions.assertThat(result.getHighestValidStep()).isNull();
        Assertions.assertThat(result.getHighestValidStepValue()).isNaN();
        Assertions.assertThat(result.getLowestInvalidStep()).isNull();
        Assertions.assertThat(result.getLowestInvalidStepValue()).isNaN();
        Assertions.assertThat(result.getLimitingCause()).isEqualTo(LimitingCause.INDEX_EVALUATION_OR_MAX_ITERATION);
        Assertions.assertThat(result.getLimitingFailureMessage()).isEqualTo("None");
    }

    @Test
    void invalidDueToCriticalBranch() {
        final Index<Object> index = Mockito.mock(Index.class);
        final DichotomyStepResult<Object> highestValidStepResult = Mockito.mock(DichotomyStepResult.class);
        final DichotomyStepResult<Object> lowestInvalidStepResult = Mockito.mock(DichotomyStepResult.class);
        final double highestValidStepValue = 27d;
        final double lowestInvalidStepValue = -12d;
        Mockito.when(index.highestValidStep()).thenReturn(Pair.of(highestValidStepValue, highestValidStepResult));
        Mockito.when(index.lowestInvalidStep()).thenReturn(Pair.of(lowestInvalidStepValue, lowestInvalidStepResult));
        Mockito.when(lowestInvalidStepResult.isFailed()).thenReturn(false);

        final DichotomyResult<Object> result = DichotomyResult.buildFromIndex(index);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.hasValidStep()).isTrue();
        Assertions.assertThat(result.isInterrupted()).isFalse();
        Assertions.assertThat(result.getHighestValidStep()).isEqualTo(highestValidStepResult);
        Assertions.assertThat(result.getHighestValidStepValue()).isEqualTo(highestValidStepValue);
        Assertions.assertThat(result.getLowestInvalidStep()).isEqualTo(lowestInvalidStepResult);
        Assertions.assertThat(result.getLowestInvalidStepValue()).isEqualTo(lowestInvalidStepValue);
        Assertions.assertThat(result.getLimitingCause()).isEqualTo(LimitingCause.CRITICAL_BRANCH);
        Assertions.assertThat(result.getLimitingFailureMessage()).isEqualTo("None");
    }

    @ParameterizedTest
    @CsvSource({
        "GLSK_LIMITATION, GLSK_LIMITATION",
        "BALANCE_LOADFLOW_DIVERGENCE, BALANCE_LOADFLOW_DIVERGENCE",
        "UNKNOWN_TERMINAL_BUS, UNKNOWN_TERMINAL_BUS"
    })
    void invalidDueToParameterized(final ReasonInvalid reasonInvalid, final LimitingCause limitingCause) {
        final Index<Object> index = Mockito.mock(Index.class);
        final DichotomyStepResult<Object> highestValidStepResult = Mockito.mock(DichotomyStepResult.class);
        final DichotomyStepResult<Object> lowestInvalidStepResult = Mockito.mock(DichotomyStepResult.class);
        final double highestValidStepValue = 27d;
        final double lowestInvalidStepValue = -12d;
        Mockito.when(index.highestValidStep()).thenReturn(Pair.of(highestValidStepValue, highestValidStepResult));
        Mockito.when(index.lowestInvalidStep()).thenReturn(Pair.of(lowestInvalidStepValue, lowestInvalidStepResult));
        Mockito.when(lowestInvalidStepResult.isFailed()).thenReturn(true);
        Mockito.when(lowestInvalidStepResult.getReasonInvalid()).thenReturn(reasonInvalid);
        final String failureMessage = "Failure message";
        Mockito.when(lowestInvalidStepResult.getFailureMessage()).thenReturn(failureMessage);

        final DichotomyResult<Object> result = DichotomyResult.buildFromIndex(index);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.hasValidStep()).isTrue();
        Assertions.assertThat(result.isInterrupted()).isFalse();
        Assertions.assertThat(result.getHighestValidStep()).isEqualTo(highestValidStepResult);
        Assertions.assertThat(result.getHighestValidStepValue()).isEqualTo(highestValidStepValue);
        Assertions.assertThat(result.getLowestInvalidStep()).isEqualTo(lowestInvalidStepResult);
        Assertions.assertThat(result.getLowestInvalidStepValue()).isEqualTo(lowestInvalidStepValue);
        Assertions.assertThat(result.getLimitingCause()).isEqualTo(limitingCause);
        Assertions.assertThat(result.getLimitingFailureMessage()).isEqualTo(failureMessage);
    }

    @ParameterizedTest
    @EnumSource(value = ReasonInvalid.class, names = { "RAO_INTERRUPTION", "UNSECURE_AFTER_VALIDATION", "VALIDATION_FAILED", "NONE"})
    void invalidDueToComputationFailure(final ReasonInvalid reasonInvalid) {
        final Index<Object> index = Mockito.mock(Index.class);
        final DichotomyStepResult<Object> highestValidStepResult = Mockito.mock(DichotomyStepResult.class);
        final DichotomyStepResult<Object> lowestInvalidStepResult = Mockito.mock(DichotomyStepResult.class);
        final double highestValidStepValue = 27d;
        final double lowestInvalidStepValue = -12d;
        Mockito.when(index.highestValidStep()).thenReturn(Pair.of(highestValidStepValue, highestValidStepResult));
        Mockito.when(index.lowestInvalidStep()).thenReturn(Pair.of(lowestInvalidStepValue, lowestInvalidStepResult));
        Mockito.when(lowestInvalidStepResult.isFailed()).thenReturn(true);
        Mockito.when(lowestInvalidStepResult.getReasonInvalid()).thenReturn(reasonInvalid);
        final String failureMessage = "Failure message";
        Mockito.when(lowestInvalidStepResult.getFailureMessage()).thenReturn(failureMessage);

        final DichotomyResult<Object> result = DichotomyResult.buildFromIndex(index);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.hasValidStep()).isTrue();
        Assertions.assertThat(result.isInterrupted()).isFalse();
        Assertions.assertThat(result.getHighestValidStep()).isEqualTo(highestValidStepResult);
        Assertions.assertThat(result.getHighestValidStepValue()).isEqualTo(highestValidStepValue);
        Assertions.assertThat(result.getLowestInvalidStep()).isEqualTo(lowestInvalidStepResult);
        Assertions.assertThat(result.getLowestInvalidStepValue()).isEqualTo(lowestInvalidStepValue);
        Assertions.assertThat(result.getLimitingCause()).isEqualTo(LimitingCause.COMPUTATION_FAILURE);
        Assertions.assertThat(result.getLimitingFailureMessage()).isEqualTo(failureMessage);
    }
}
