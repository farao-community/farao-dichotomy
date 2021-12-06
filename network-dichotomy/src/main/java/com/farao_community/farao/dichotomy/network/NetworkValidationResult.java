/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy.network;

import com.farao_community.farao.data.rao_result_api.RaoResult;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public interface NetworkValidationResult<I> {

    RaoResult getRaoResult();

    I getValidationData();
}
