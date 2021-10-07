package com.farao_community.farao.dichotomy.network.scaling;

import com.farao_community.farao.commons.ZonalData;
import com.farao_community.farao.data.glsk.api.io.GlskDocumentImporters;
import com.farao_community.farao.dichotomy.network.*;
import com.powsybl.action.util.Scalable;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;
import java.util.Objects;

import static com.farao_community.farao.dichotomy.network.ReasonNotValid.GLSK_LIMITATION;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
class ScalingNetworkValidationStrategyTest {
    private Network network;
    private ZonalData<Scalable> zonalScalable;
    private NetworkValidator<NetworkValidationResultImpl> networkValidator;
    private ShiftDispatcher shiftDispatcher;

    @BeforeEach
    void setUp() {
        String networkFilename = "20210901_2230_test_network.uct";
        network = Importers.loadNetwork(networkFilename, getClass().getResourceAsStream(networkFilename));

        String glskFilename = "20210901_2230_213_GSK_CO_CSE1.xml";
        zonalScalable = GlskDocumentImporters
                .importGlsk(Objects.requireNonNull(getClass().getResourceAsStream(glskFilename)))
                .getZonalScalable(network);

        networkValidator = Mockito.mock(NetworkValidator.class);
        shiftDispatcher = Mockito.mock(ShiftDispatcher.class);
    }

    @Test
    void scalingNetworkValidationStrategyWithGlskLimitation() throws ShiftingException {
        Mockito.when(shiftDispatcher.dispatch(200)).thenReturn(Map.of(
                "10YCH-SWISSGRIDZ", 5000.
        ));

        ScalingNetworkValidationStrategy<?> scalingNetworkValidationStrategy = new ScalingNetworkValidationStrategy<>(
                network,
                networkValidator,
                zonalScalable,
                shiftDispatcher
        );

        NetworkValidationResultWrapper<?> networkStepResult = scalingNetworkValidationStrategy.validateStep(200);
        assertEquals(GLSK_LIMITATION, networkStepResult.getReasonNotValid());
    }

    @Test
    void scalingNetworkValidationStrategyWithSecure() throws ShiftingException, NetworkValidationException {
        Mockito.when(shiftDispatcher.dispatch(200)).thenReturn(Map.of(
                "10YCH-SWISSGRIDZ", 200.
        ));
        Mockito.when(networkValidator.validateNetwork(network)).thenReturn(new NetworkValidationResultImpl(true));

        ScalingNetworkValidationStrategy<?> scalingNetworkValidationStrategy = new ScalingNetworkValidationStrategy<>(
                network,
                networkValidator,
                zonalScalable,
                shiftDispatcher
        );

        NetworkValidationResultWrapper<?> networkStepResult = scalingNetworkValidationStrategy.validateStep(200);
        assertTrue(networkStepResult.isValid());
        assertEquals(200, networkStepResult.stepValue());
    }

    @Test
    void scalingNetworkValidationStrategyWithUnsecure() throws ShiftingException, NetworkValidationException {
        Mockito.when(shiftDispatcher.dispatch(200)).thenReturn(Map.of(
                "10YCH-SWISSGRIDZ", 200.
        ));
        Mockito.when(networkValidator.validateNetwork(network)).thenReturn(new NetworkValidationResultImpl(false));

        ScalingNetworkValidationStrategy<?> scalingNetworkValidationStrategy = new ScalingNetworkValidationStrategy<>(
                network,
                networkValidator,
                zonalScalable,
                shiftDispatcher
        );

        NetworkValidationResultWrapper<?> networkStepResult = scalingNetworkValidationStrategy.validateStep(200);
        assertFalse(networkStepResult.isValid());
        assertEquals(200, networkStepResult.stepValue());
    }

    @Test
    void scalingNetworkValidationStrategyWithFailure() throws ShiftingException, NetworkValidationException {
        Mockito.when(shiftDispatcher.dispatch(200)).thenReturn(Map.of(
                "10YCH-SWISSGRIDZ", 200.
        ));
        Mockito.when(networkValidator.validateNetwork(network)).thenThrow(new NetworkValidationException("RAO failure"));

        ScalingNetworkValidationStrategy<?> scalingNetworkValidationStrategy = new ScalingNetworkValidationStrategy<>(
                network,
                networkValidator,
                zonalScalable,
                shiftDispatcher
        );

        NetworkValidationResultWrapper<?> networkStepResult = scalingNetworkValidationStrategy.validateStep(200);
        assertTrue(networkStepResult.isFailed());
        assertEquals("RAO failure", networkStepResult.getFailureMessage());
    }

    @Test
    void scalingNetworkValidationStrategyWithShiftingException() throws ShiftingException {
        Mockito.when(shiftDispatcher.dispatch(200)).thenThrow(new ShiftingException("Impossible to shift"));
        ScalingNetworkValidationStrategy<?> scalingNetworkValidationStrategy = new ScalingNetworkValidationStrategy<>(
                network,
                networkValidator,
                zonalScalable,
                shiftDispatcher
        );

        NetworkValidationResultWrapper<?> networkStepResult = scalingNetworkValidationStrategy.validateStep(200);
        assertTrue(networkStepResult.isFailed());
        assertEquals("Impossible to shift", networkStepResult.getFailureMessage());
    }
}
