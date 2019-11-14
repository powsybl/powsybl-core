package com.powsybl.cgmes.conversion.update.elements16;

import com.powsybl.cgmes.conversion.update.IidmToCgmes;

public class ShuntCompensatorToShuntCompensator extends IidmToCgmes {

    ShuntCompensatorToShuntCompensator() {
        // XXX LUMA ensure that all attributes used in update config are present in IIDM
        addSimpleUpdate("bPerSection", "cim:LinearShuntCompensator.bPerSection", "_EQ", false);
        addSimpleUpdate("maximumSectionCount", "cim:ShuntCompensator.maximumSections", "_EQ", false);
        addSimpleUpdate("nomU", "cim:ShuntCompensator.nomU", "_EQ", false);
        addSimpleUpdate("normalSections", "cim:ShuntCompensator.normalSections", "_EQ", false);
    }

}
