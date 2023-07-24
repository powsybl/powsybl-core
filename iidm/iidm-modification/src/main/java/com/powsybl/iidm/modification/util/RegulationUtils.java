package com.powsybl.iidm.modification.util;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;

import java.util.stream.Stream;

public final class RegulationUtils {
    private RegulationUtils() {
    }

    public static Stream<Generator> getRegulatingGenerators(Network network, Bus bus) {
        return network.getGeneratorStream().filter(Generator::isVoltageRegulatorOn)
            .filter(g -> bus.equals(g.getRegulatingTerminal().getBusView().getBus()));
    }
}
