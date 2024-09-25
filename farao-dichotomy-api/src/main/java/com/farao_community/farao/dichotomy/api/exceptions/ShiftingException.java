/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy.api.exceptions;

import com.farao_community.farao.dichotomy.api.results.ReasonInvalid;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public class ShiftingException extends Exception {
    private final ReasonInvalid reason;

    public ShiftingException(String message) {
        this(message, null, null);
    }

    public ShiftingException(String message, Throwable cause) {
        this(message, cause, null);
    }

    public ShiftingException(String message, ReasonInvalid reason) {
        this(message, null, reason);
    }

    public ShiftingException(String message, Throwable cause, ReasonInvalid reason) {
        super(message, cause);
        this.reason = reason;
    }

    public ReasonInvalid getReason() {
        return reason;
    }
}
