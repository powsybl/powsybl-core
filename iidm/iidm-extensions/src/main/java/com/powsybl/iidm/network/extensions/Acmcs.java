package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Network;

import java.util.List;
import java.util.Optional;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
public interface Acmcs extends Extension<Network> {

    String NAME = "acmcs";

    List<Acmc> getAcmcs();

    Optional<Acmc> getAcmc(String name);

    @Override
    default String getName() {
        return NAME;
    }
}
