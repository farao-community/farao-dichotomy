package com.farao_community.farao.dichotomy.network.scaling;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public enum CseCountry {
    IT("10YIT-GRTN-----B"),
    FR("10YFR-RTE------C"),
    AT("10YAT-APG------L"),
    CH("10YCH-SWISSGRIDZ"),
    SI("10YSI-ELES-----O");

    private final String eiCode;

    CseCountry(String eiCode) {
        this.eiCode = eiCode;
    }

    public String getEiCode() {
        return eiCode;
    }

    @Override
    public String toString() {
        return eiCode;
    }
}
