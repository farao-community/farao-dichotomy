/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy.network;

import com.farao_community.farao.dichotomy.api.ValidationStrategy;
import com.powsybl.iidm.network.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parent implementation of a {@link ValidationStrategy} based on network validation. It usually consists in shifting
 * a network based on the {@code stepValue} -- for that a lot of different implementations could exist -- and then
 * validating it -- usually with a RAO but then again strategies can be different.
 * Shifting strategies would be defined in child classes and validation strategies and network validation would be
 * handled by implementations of {@link NetworkValidator}.
 * This implementation is based on {@link NetworkValidationResultWrapper} that can wraps meta-data about the validation
 * process around concrete network validation data.
 *
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public abstract class AbstractNetworkValidationStrategy<I extends NetworkValidationResult> implements ValidationStrategy<NetworkValidationResultWrapper<I>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractNetworkValidationStrategy.class);

    protected final Network network;
    private final String initialVariant;
    protected final NetworkValidator<I> networkValidator;

    protected AbstractNetworkValidationStrategy(Network network,
                                                NetworkValidator<I> networkValidator) {
        this.network = network;
        this.initialVariant = network.getVariantManager().getWorkingVariantId();
        this.networkValidator = networkValidator;
    }

    @Override
    public NetworkValidationResultWrapper<I> validateStep(double stepValue) {
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
            return NetworkValidationResultWrapper.fromNetworkValidationFailure(stepValue, ReasonInvalid.GLSK_LIMITATION, e.getMessage());
        } catch (ShiftingException | NetworkValidationException e) {
            return NetworkValidationResultWrapper.fromNetworkValidationFailure(stepValue, ReasonInvalid.VALIDATION_FAILED, e.getMessage());
        } finally {
            network.getVariantManager().setWorkingVariant(initialVariant);
            network.getVariantManager().removeVariant(newVariant);
        }
    }

    private String variantName(double stepValue) {
        return String.format("%s-ScaledBy-%d", initialVariant, (int) stepValue);
    }

    protected abstract void shiftNetwork(double stepValue) throws GlskLimitationException, ShiftingException;
}
