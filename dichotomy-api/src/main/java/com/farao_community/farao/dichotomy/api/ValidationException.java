package com.farao_community.farao.dichotomy.api;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public class ValidationException extends Exception {

    public ValidationException(String message, Throwable e) {
        super(message, e);
    }
}
