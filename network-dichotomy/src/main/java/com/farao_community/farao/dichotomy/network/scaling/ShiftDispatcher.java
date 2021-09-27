package com.farao_community.farao.dichotomy.network.scaling;

import com.farao_community.farao.dichotomy.network.ShiftingException;

import java.util.Map;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public interface ShiftDispatcher {

    Map<String, Double> dispatch(double value) throws ShiftingException;
}
