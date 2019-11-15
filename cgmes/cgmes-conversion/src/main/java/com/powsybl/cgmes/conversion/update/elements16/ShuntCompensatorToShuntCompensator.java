package com.powsybl.cgmes.conversion.update.elements16;

import com.powsybl.cgmes.conversion.update.IidmToCgmes;
import com.powsybl.cgmes.model.CgmesSubset;

public class ShuntCompensatorToShuntCompensator extends IidmToCgmes {

    ShuntCompensatorToShuntCompensator() {
        ignore("q");

        simpleUpdate("bPerSection", "cim:LinearShuntCompensator.bPerSection", CgmesSubset.EQUIPMENT);
        simpleUpdate("maximumSectionCount", "cim:ShuntCompensator.maximumSections", CgmesSubset.EQUIPMENT);
        simpleUpdate("currentSectionCount", "cim:ShuntCompensator.sections", CgmesSubset.STEADY_STATE_HYPOTHESIS);
    }

}
