package com.farao_community.farao.dichotomy.api;

import com.powsybl.iidm.network.Network;

public interface NetworkExporter {
    void export(Network network);
}
