package com.powsybl.cgmes.conversion.update.elements16;

import com.powsybl.cgmes.conversion.update.IidmToCgmes;
import com.powsybl.cgmes.model.CgmesSubset;

public class LoadToEnergyConsumer extends IidmToCgmes {

    LoadToEnergyConsumer() {
        // Ignore changes on (p, q) from Load Terminal
        // They will be added directly as new objects in SV subset
        ignore("p");
        ignore("q");

        simpleUpdate("p0", "cim:EnergyConsumer.p", CgmesSubset.STEADY_STATE_HYPOTHESIS);
        simpleUpdate("q0", "cim:EnergyConsumer.q", CgmesSubset.STEADY_STATE_HYPOTHESIS);
    }

}
