package com.farao_community.farao.dichotomy.network.scaling;

import com.farao_community.farao.commons.FaraoException;
import com.farao_community.farao.commons.ZonalData;
import com.farao_community.farao.data.glsk.api.io.GlskDocumentImporters;
import com.farao_community.farao.dichotomy.api.ValidationException;
import com.farao_community.farao.dichotomy.network.*;
import com.powsybl.action.util.Scalable;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;
import java.util.Objects;

import static com.farao_community.farao.dichotomy.network.ReasonUnsecure.GLSK_LIMITATION;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
class ScalingNetworkValidationStrategyTest {

    @Test
    void scalingNetworkValidationStrategyWithGlskLimitation() throws ValidationException {
        String networkFilename = "20210901_2230_test_network.uct";
        Network network = Importers.loadNetwork(networkFilename, getClass().getResourceAsStream(networkFilename));

        String glskFilename = "20210901_2230_213_GSK_CO_CSE1.xml";
        ZonalData<Scalable> zonalScalable = GlskDocumentImporters
                .importGlsk(Objects.requireNonNull(getClass().getResourceAsStream(glskFilename)))
                .getZonalScalable(network);

        NetworkValidator<?> networkValidator = Mockito.mock(NetworkValidator.class);

        ShiftDispatcher shiftDispatcher = Mockito.mock(ShiftDispatcher.class);
        Mockito.when(shiftDispatcher.dispatch(200)).thenReturn(Map.of(
                "10YCH-SWISSGRIDZ", 5000.
        ));

        ScalingNetworkValidationStrategy<?> scalingNetworkValidationStrategy = new ScalingNetworkValidationStrategy<>(
                network,
                networkValidator,
                zonalScalable,
                shiftDispatcher
        );

        NetworkStepResultWrapper<?> networkStepResult = scalingNetworkValidationStrategy.validateStep(200);
        assertEquals(GLSK_LIMITATION, networkStepResult.getReasonUnsecure());
    }

    @Test
    void scalingNetworkValidationStrategyWithSecure() throws ValidationException {
        String networkFilename = "20210901_2230_test_network.uct";
        Network network = Importers.loadNetwork(networkFilename, getClass().getResourceAsStream(networkFilename));

        String glskFilename = "20210901_2230_213_GSK_CO_CSE1.xml";
        ZonalData<Scalable> zonalScalable = GlskDocumentImporters
                .importGlsk(Objects.requireNonNull(getClass().getResourceAsStream(glskFilename)))
                .getZonalScalable(network);

        ShiftDispatcher shiftDispatcher = Mockito.mock(ShiftDispatcher.class);
        Mockito.when(shiftDispatcher.dispatch(200)).thenReturn(Map.of(
                "10YCH-SWISSGRIDZ", 200.
        ));

        NetworkValidator<NetworkValidationResultImpl> networkValidator = Mockito.mock(NetworkValidator.class);
        Mockito.when(networkValidator.validateNetwork(network)).thenReturn(new NetworkValidationResultImpl(true));

        ScalingNetworkValidationStrategy<?> scalingNetworkValidationStrategy = new ScalingNetworkValidationStrategy<>(
                network,
                networkValidator,
                zonalScalable,
                shiftDispatcher
        );

        NetworkStepResultWrapper<?> networkStepResult = scalingNetworkValidationStrategy.validateStep(200);
        assertTrue(networkStepResult.isSecure());
        assertEquals(200, networkStepResult.stepValue());
    }

    @Test
    void scalingNetworkValidationStrategyWithUnsecure() throws ValidationException {
        String networkFilename = "20210901_2230_test_network.uct";
        Network network = Importers.loadNetwork(networkFilename, getClass().getResourceAsStream(networkFilename));

        String glskFilename = "20210901_2230_213_GSK_CO_CSE1.xml";
        ZonalData<Scalable> zonalScalable = GlskDocumentImporters
                .importGlsk(Objects.requireNonNull(getClass().getResourceAsStream(glskFilename)))
                .getZonalScalable(network);

        ShiftDispatcher shiftDispatcher = Mockito.mock(ShiftDispatcher.class);
        Mockito.when(shiftDispatcher.dispatch(200)).thenReturn(Map.of(
                "10YCH-SWISSGRIDZ", 200.
        ));

        NetworkValidator<NetworkValidationResultImpl> networkValidator = Mockito.mock(NetworkValidator.class);
        Mockito.when(networkValidator.validateNetwork(network)).thenReturn(new NetworkValidationResultImpl(false));

        ScalingNetworkValidationStrategy<?> scalingNetworkValidationStrategy = new ScalingNetworkValidationStrategy<>(
                network,
                networkValidator,
                zonalScalable,
                shiftDispatcher
        );

        NetworkStepResultWrapper<?> networkStepResult = scalingNetworkValidationStrategy.validateStep(200);
        assertFalse(networkStepResult.isSecure());
        assertEquals(200, networkStepResult.stepValue());
    }

    @Test
    void scalingNetworkValidationStrategyWithFailure() {
        String networkFilename = "20210901_2230_test_network.uct";
        Network network = Importers.loadNetwork(networkFilename, getClass().getResourceAsStream(networkFilename));

        String glskFilename = "20210901_2230_213_GSK_CO_CSE1.xml";
        ZonalData<Scalable> zonalScalable = GlskDocumentImporters
                .importGlsk(Objects.requireNonNull(getClass().getResourceAsStream(glskFilename)))
                .getZonalScalable(network);

        ShiftDispatcher shiftDispatcher = Mockito.mock(ShiftDispatcher.class);
        Mockito.when(shiftDispatcher.dispatch(200)).thenReturn(Map.of(
                "10YCH-SWISSGRIDZ", 200.
        ));

        NetworkValidator<?> networkValidator = Mockito.mock(NetworkValidator.class);
        Mockito.when(networkValidator.validateNetwork(network)).thenThrow(new NetworkValidationException("RAO failure", new FaraoException()));

        ScalingNetworkValidationStrategy<?> scalingNetworkValidationStrategy = new ScalingNetworkValidationStrategy<>(
                network,
                networkValidator,
                zonalScalable,
                shiftDispatcher
        );

        assertThrows(ValidationException.class, () -> scalingNetworkValidationStrategy.validateStep(200));
    }
}
