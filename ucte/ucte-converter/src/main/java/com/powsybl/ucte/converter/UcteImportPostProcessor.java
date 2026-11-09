package com.powsybl.ucte.converter;

import com.powsybl.iidm.network.Network;
import com.powsybl.ucte.network.UcteNetwork;

public interface UcteImportPostProcessor {

    /**
     * Get the post-processor name, which has to be unique among all UCTE import post-processors.
     */
    String getName();

    default void process(Network network, UcteNetwork ucteNetwork) {
    }
}
