/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy.api.exceptions;

/**
 * @author Vincent Bochet {@literal <vincent.bochet at rte-france.com>}
 */
public class RaoInterruptionException extends Exception {

    public RaoInterruptionException(String message) {
        super(message);
    }
}
