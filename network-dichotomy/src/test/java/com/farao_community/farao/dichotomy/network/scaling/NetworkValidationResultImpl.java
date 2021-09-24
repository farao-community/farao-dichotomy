package com.farao_community.farao.dichotomy.network.scaling;

import com.farao_community.farao.dichotomy.network.NetworkValidationResult;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public class NetworkValidationResultImpl implements NetworkValidationResult {

    private final boolean secure;

    public NetworkValidationResultImpl(boolean secure) {
        this.secure = secure;
    }

    @Override
    public boolean isSecure() {
        return secure;
    }
}
