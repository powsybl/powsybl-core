package com.powsybl.cgmes.conversion.update.elements16;

import com.powsybl.cgmes.conversion.update.IidmToCgmes;
import com.powsybl.cgmes.model.CgmesSubset;

public class LoadToEnergyConsumer extends IidmToCgmes {

    LoadToEnergyConsumer() {
        addSimpleUpdate("p0", "cim:EnergyConsumer.p", CgmesSubset.STEADY_STATE_HYPOTHESIS, false);
        addSimpleUpdate("p", "cim:EnergyConsumer.p", CgmesSubset.STEADY_STATE_HYPOTHESIS, false);
        addSimpleUpdate("q0", "cim:EnergyConsumer.q", CgmesSubset.STEADY_STATE_HYPOTHESIS, false);
        addSimpleUpdate("q", "cim:EnergyConsumer.q", CgmesSubset.STEADY_STATE_HYPOTHESIS, false);
    }

}
