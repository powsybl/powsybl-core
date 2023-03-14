package com.powsybl.ampl.converter;

import com.powsybl.commons.util.StringToIntMapper;
import com.powsybl.iidm.network.Network;

public class DefaultAmplNetworkUpdaterFactory extends AbstractAmplNetworkUpdaterFactory {
    @Override
    public AmplNetworkUpdater of(StringToIntMapper<AmplSubset> mapper, Network network) {
        return new DefaultAmplNetworkUpdater(mapper);
    }
}
