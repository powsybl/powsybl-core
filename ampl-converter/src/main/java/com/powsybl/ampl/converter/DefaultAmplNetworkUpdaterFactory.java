package com.powsybl.ampl.converter;

import com.powsybl.commons.util.StringToIntMapper;
import com.powsybl.iidm.network.Network;

public class DefaultAmplNetworkUpdaterFactory implements AmplNetworkUpdaterFactory {
    @Override
    public AmplNetworkUpdater create(StringToIntMapper<AmplSubset> mapper, Network network) {
        return new DefaultAmplNetworkUpdater(mapper);
    }
}
