package com.farao_community.farao.dichotomy.network;

import com.farao_community.farao.dichotomy.api.ValidationException;
import com.farao_community.farao.dichotomy.api.ValidationStrategy;
import com.powsybl.iidm.network.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public abstract class AbstractNetworkValidationStrategy<I extends NetworkValidationResult> implements ValidationStrategy<NetworkStepResultWrapper<I>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractNetworkValidationStrategy.class);

    protected final Network network;
    protected final NetworkValidator<I> networkValidator;

    protected AbstractNetworkValidationStrategy(Network network,
                                                NetworkValidator<I> networkValidator) {
        this.network = network;
        this.networkValidator = networkValidator;
    }

    @Override
    public NetworkStepResultWrapper<I> validateStep(double stepValue) throws ValidationException {
        String initialVariant = network.getVariantManager().getWorkingVariantId();
        String newVariant = variantName(stepValue);
        network.getVariantManager().cloneVariant(initialVariant, newVariant);
        network.getVariantManager().setWorkingVariant(newVariant);

        try {
            LOGGER.debug("Shifting network");
            shiftNetwork(stepValue);
            LOGGER.debug("Validating network");
            I networkStepResult = networkValidator.validateNetwork(network);
            return  NetworkStepResultWrapper.fromNetworkValidationResult(stepValue, networkStepResult);
        } catch (GlskLimitationException e) {
            LOGGER.warn("GLSK limits have been reached for step value {}", stepValue);
            return NetworkStepResultWrapper.withGlskLimitation(stepValue);
        } catch (ShiftingException | NetworkValidationException e) {
            throw new ValidationException(String.format("Impossible to validate step %.0f", stepValue), e);
        } finally {
            network.getVariantManager().setWorkingVariant(initialVariant);
            network.getVariantManager().removeVariant(newVariant);
        }
    }

    private String variantName(double stepValue) {
        return String.format("ScaledBy-%d", (int) stepValue);
    }

    protected abstract void shiftNetwork(double stepValue) throws GlskLimitationException;
}
