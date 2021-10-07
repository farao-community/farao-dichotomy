/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy.network;

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
