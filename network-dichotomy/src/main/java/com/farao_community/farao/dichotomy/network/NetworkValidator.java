package com.farao_community.farao.dichotomy.network;

import com.powsybl.iidm.network.Network;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public interface NetworkValidator<I extends NetworkValidationResult> {

    I validateNetwork(Network network);
}
