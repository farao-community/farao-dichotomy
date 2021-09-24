package com.farao_community.farao.dichotomy.network.scaling;

import java.util.Map;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public interface ShiftDispatcher {

    Map<String, Double> dispatch(double value);
}
