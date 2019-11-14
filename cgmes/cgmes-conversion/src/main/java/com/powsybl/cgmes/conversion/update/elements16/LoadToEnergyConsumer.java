package com.powsybl.cgmes.conversion.update.elements16;

import com.powsybl.cgmes.conversion.update.IidmToCgmes;

public class LoadToEnergyConsumer extends IidmToCgmes {

    LoadToEnergyConsumer() {
        addSimpleUpdate("p0", "cim:EnergyConsumer.p", "_SSH", false);
        addSimpleUpdate("p", "cim:EnergyConsumer.p", "_SSH", false);
        addSimpleUpdate("q0", "cim:EnergyConsumer.q", "_SSH", false);
        addSimpleUpdate("q", "cim:EnergyConsumer.q", "_SSH", false);
    }

}
