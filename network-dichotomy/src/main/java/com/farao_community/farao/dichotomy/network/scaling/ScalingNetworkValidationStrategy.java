package com.farao_community.farao.dichotomy.network.scaling;

import com.farao_community.farao.commons.ZonalData;
import com.farao_community.farao.dichotomy.network.*;
import com.powsybl.action.util.Scalable;
import com.powsybl.iidm.network.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public class ScalingNetworkValidationStrategy<I extends NetworkValidationResult> extends AbstractNetworkValidationStrategy<I> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScalingNetworkValidationStrategy.class);
    private static final double EPSILON = 1e-3;

    private final ZonalData<Scalable> zonalScalable;
    private final ShiftDispatcher shiftDispatcher;

    public ScalingNetworkValidationStrategy(Network network,
                                               NetworkValidator<I> networkValidator,
                                               ZonalData<Scalable> zonalScalable,
                                               ShiftDispatcher shiftDispatcher) {
        super(network, networkValidator);
        this.zonalScalable = zonalScalable;
        this.shiftDispatcher = shiftDispatcher;
    }

    @Override
    protected void shiftNetwork(double stepValue) throws GlskLimitationException, ShiftingException {
        LOGGER.info("Scale network file");
        Map<String, Double> scalingValuesByCountry = shiftDispatcher.dispatch(stepValue);
        List<String> limitingCountries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : scalingValuesByCountry.entrySet()) {
            String zoneId = entry.getKey();
            double asked = entry.getValue();
            LOGGER.info(String.format("Applying variation on zone %s (target: %.2f)", zoneId, asked));
            double done = zonalScalable.getData(zoneId).scale(network, asked);
            if (Math.abs(done - asked) > EPSILON) {
                LOGGER.warn(String.format("Incomplete variation on zone %s (target: %.2f, done: %.2f)", zoneId, asked, done));
                limitingCountries.add(zoneId);
            }
        }
        if (!limitingCountries.isEmpty()) {
            StringJoiner sj = new StringJoiner(", ", "There are Glsk limitation(s) in ", ".");
            limitingCountries.forEach(sj::add);
            throw new GlskLimitationException(sj.toString());
        }
    }
}
