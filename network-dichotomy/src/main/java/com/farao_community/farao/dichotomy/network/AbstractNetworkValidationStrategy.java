package com.farao_community.farao.dichotomy.network;

import com.farao_community.farao.dichotomy.api.ValidationStrategy;
import com.powsybl.iidm.network.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public abstract class AbstractNetworkValidationStrategy<I extends NetworkValidationResult> implements ValidationStrategy<NetworkValidationResultWrapper<I>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractNetworkValidationStrategy.class);

    protected final Network network;
    protected final NetworkValidator<I> networkValidator;

    protected AbstractNetworkValidationStrategy(Network network,
                                                NetworkValidator<I> networkValidator) {
        this.network = network;
        this.networkValidator = networkValidator;
    }

    @Override
    public NetworkValidationResultWrapper<I> validateStep(double stepValue) {
        String initialVariant = network.getVariantManager().getWorkingVariantId();
        String newVariant = variantName(stepValue);
        network.getVariantManager().cloneVariant(initialVariant, newVariant);
        network.getVariantManager().setWorkingVariant(newVariant);

        try {
            LOGGER.debug("Shifting network");
            shiftNetwork(stepValue);
            LOGGER.debug("Validating network");
            I networkStepResult = networkValidator.validateNetwork(network);
            return  NetworkValidationResultWrapper.fromNetworkValidationResult(stepValue, networkStepResult);
        } catch (GlskLimitationException e) {
            LOGGER.warn("GLSK limits have been reached for step value {}", stepValue);
            return NetworkValidationResultWrapper.fromNetworkValidationFailure(stepValue, ReasonNotValid.GLSK_LIMITATION, e.getMessage());
        } catch (ShiftingException | NetworkValidationException e) {
            return NetworkValidationResultWrapper.fromNetworkValidationFailure(stepValue, ReasonNotValid.VALIDATION_FAILED, e.getMessage());
        } finally {
            network.getVariantManager().setWorkingVariant(initialVariant);
            network.getVariantManager().removeVariant(newVariant);
        }
    }

    private String variantName(double stepValue) {
        return String.format("ScaledBy-%d", (int) stepValue);
    }

    protected abstract void shiftNetwork(double stepValue) throws GlskLimitationException, ShiftingException;
}
