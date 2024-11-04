package com.farao_community.farao.dichotomy.api.results;

import com.farao_community.farao.dichotomy.api.index.Index;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.api.SoftAssertions;
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

        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(result).isNotNull();
        assertions.assertThat(result.hasValidStep()).isTrue();
        assertions.assertThat(result.isInterrupted()).isFalse();
        assertions.assertThat(result.isRaoFailed()).isFalse();
        assertions.assertThat(result.getRaoFailureMessage()).isNull();
        assertions.assertThat(result.getHighestValidStep()).isEqualTo(highestValidStepResult);
        assertions.assertThat(result.getHighestValidStepValue()).isEqualTo(highestValidStepValue);
        assertions.assertThat(result.getLowestInvalidStep()).isNull();
        assertions.assertThat(result.getLowestInvalidStepValue()).isNaN();
        assertions.assertThat(result.getLimitingCause()).isEqualTo(LimitingCause.INDEX_EVALUATION_OR_MAX_ITERATION);
        assertions.assertThat(result.getLimitingFailureMessage()).isEqualTo("None");
        assertions.assertAll();
    }

    @Test
    void noValidStep() {
        final Index<Object> index = Mockito.mock(Index.class);
        final DichotomyStepResult<Object> lowestInvalidStepResult = Mockito.mock(DichotomyStepResult.class);
        final double lowestInvalidStepValue = -12d;
        Mockito.when(index.highestValidStep()).thenReturn(null);
        Mockito.when(index.lowestInvalidStep()).thenReturn(Pair.of(lowestInvalidStepValue, lowestInvalidStepResult));

        final DichotomyResult<Object> result = DichotomyResult.buildFromIndex(index);

        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(result).isNotNull();
        assertions.assertThat(result.hasValidStep()).isFalse();
        assertions.assertThat(result.isInterrupted()).isFalse();
        assertions.assertThat(result.isRaoFailed()).isFalse();
        assertions.assertThat(result.getRaoFailureMessage()).isNull();
        assertions.assertThat(result.getHighestValidStep()).isNull();
        assertions.assertThat(result.getHighestValidStepValue()).isNaN();
        assertions.assertThat(result.getLowestInvalidStep()).isEqualTo(lowestInvalidStepResult);
        assertions.assertThat(result.getLowestInvalidStepValue()).isEqualTo(lowestInvalidStepValue);
        assertions.assertThat(result.getLimitingCause()).isEqualTo(LimitingCause.INDEX_EVALUATION_OR_MAX_ITERATION);
        assertions.assertThat(result.getLimitingFailureMessage()).isEqualTo("None");
        assertions.assertAll();
    }

    @Test
    void interruptedDichotomy() {
        final Index<Object> index = Mockito.mock(Index.class);
        Mockito.when(index.highestValidStep()).thenReturn(null);
        Mockito.when(index.lowestInvalidStep()).thenReturn(null);

        final DichotomyResult<Object> result = DichotomyResult.buildFromIndex(index);
        result.setInterrupted(true);

        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(result).isNotNull();
        assertions.assertThat(result.hasValidStep()).isFalse();
        assertions.assertThat(result.isInterrupted()).isTrue();
        assertions.assertThat(result.isRaoFailed()).isFalse();
        assertions.assertThat(result.getRaoFailureMessage()).isNull();
        assertions.assertThat(result.getHighestValidStep()).isNull();
        assertions.assertThat(result.getHighestValidStepValue()).isNaN();
        assertions.assertThat(result.getLowestInvalidStep()).isNull();
        assertions.assertThat(result.getLowestInvalidStepValue()).isNaN();
        assertions.assertThat(result.getLimitingCause()).isEqualTo(LimitingCause.INDEX_EVALUATION_OR_MAX_ITERATION);
        assertions.assertThat(result.getLimitingFailureMessage()).isEqualTo("None");
        assertions.assertAll();
    }

    @Test
    void failedRao() {
        final String failureMessage = "Insert failure message here";
        final Index<Object> index = Mockito.mock(Index.class);
        Mockito.when(index.highestValidStep()).thenReturn(null);
        Mockito.when(index.lowestInvalidStep()).thenReturn(null);

        final DichotomyResult<Object> result = DichotomyResult.buildFromRaoFailure(failureMessage);

        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(result).isNotNull();
        assertions.assertThat(result.hasValidStep()).isFalse();
        assertions.assertThat(result.isInterrupted()).isFalse();
        assertions.assertThat(result.isRaoFailed()).isTrue();
        assertions.assertThat(result.getRaoFailureMessage()).isEqualTo(failureMessage);
        assertions.assertThat(result.getHighestValidStep()).isNull();
        assertions.assertThat(result.getHighestValidStepValue()).isNaN();
        assertions.assertThat(result.getLowestInvalidStep()).isNull();
        assertions.assertThat(result.getLowestInvalidStepValue()).isNaN();
        assertions.assertThat(result.getLimitingCause()).isNull();
        assertions.assertThat(result.getLimitingFailureMessage()).isNull();
        assertions.assertAll();
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

        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(result).isNotNull();
        assertions.assertThat(result.hasValidStep()).isTrue();
        assertions.assertThat(result.isInterrupted()).isFalse();
        assertions.assertThat(result.isRaoFailed()).isFalse();
        assertions.assertThat(result.getRaoFailureMessage()).isNull();
        assertions.assertThat(result.getHighestValidStep()).isEqualTo(highestValidStepResult);
        assertions.assertThat(result.getHighestValidStepValue()).isEqualTo(highestValidStepValue);
        assertions.assertThat(result.getLowestInvalidStep()).isEqualTo(lowestInvalidStepResult);
        assertions.assertThat(result.getLowestInvalidStepValue()).isEqualTo(lowestInvalidStepValue);
        assertions.assertThat(result.getLimitingCause()).isEqualTo(LimitingCause.CRITICAL_BRANCH);
        assertions.assertThat(result.getLimitingFailureMessage()).isEqualTo("None");
        assertions.assertAll();
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

        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(result).isNotNull();
        assertions.assertThat(result.hasValidStep()).isTrue();
        assertions.assertThat(result.isInterrupted()).isFalse();
        assertions.assertThat(result.isRaoFailed()).isFalse();
        assertions.assertThat(result.getRaoFailureMessage()).isNull();
        assertions.assertThat(result.getHighestValidStep()).isEqualTo(highestValidStepResult);
        assertions.assertThat(result.getHighestValidStepValue()).isEqualTo(highestValidStepValue);
        assertions.assertThat(result.getLowestInvalidStep()).isEqualTo(lowestInvalidStepResult);
        assertions.assertThat(result.getLowestInvalidStepValue()).isEqualTo(lowestInvalidStepValue);
        assertions.assertThat(result.getLimitingCause()).isEqualTo(limitingCause);
        assertions.assertThat(result.getLimitingFailureMessage()).isEqualTo(failureMessage);
        assertions.assertAll();
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

        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(result).isNotNull();
        assertions.assertThat(result.hasValidStep()).isTrue();
        assertions.assertThat(result.isInterrupted()).isFalse();
        assertions.assertThat(result.isRaoFailed()).isFalse();
        assertions.assertThat(result.getRaoFailureMessage()).isNull();
        assertions.assertThat(result.getHighestValidStep()).isEqualTo(highestValidStepResult);
        assertions.assertThat(result.getHighestValidStepValue()).isEqualTo(highestValidStepValue);
        assertions.assertThat(result.getLowestInvalidStep()).isEqualTo(lowestInvalidStepResult);
        assertions.assertThat(result.getLowestInvalidStepValue()).isEqualTo(lowestInvalidStepValue);
        assertions.assertThat(result.getLimitingCause()).isEqualTo(LimitingCause.COMPUTATION_FAILURE);
        assertions.assertThat(result.getLimitingFailureMessage()).isEqualTo(failureMessage);
        assertions.assertAll();
    }
}
