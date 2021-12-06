package com.farao_community.farao.dichotomy.network;

import com.farao_community.farao.data.rao_result_api.RaoResult;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public class NetworkValidationResultTest<I> implements NetworkValidationResult<I> {

    private final RaoResultMock raoResultMock;

    public NetworkValidationResultTest(boolean secure) {
        this.raoResultMock = new RaoResultMock(secure);
    }

    @Override
    public RaoResult getRaoResult() {
        return raoResultMock;
    }

    @Override
    public I getValidationData() {
        return null;
    }
}
