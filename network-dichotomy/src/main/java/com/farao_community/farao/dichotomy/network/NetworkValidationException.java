package com.farao_community.farao.dichotomy.network;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public class NetworkValidationException extends RuntimeException {

    public NetworkValidationException(String message, Throwable e) {
        super(message, e);
    }
}
