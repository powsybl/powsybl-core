package com.powsybl.cgmes.conversion.update.elements16;

import com.powsybl.cgmes.conversion.update.IidmToCgmes;
import com.powsybl.cgmes.model.CgmesSubset;

public class ShuntCompensatorToShuntCompensator extends IidmToCgmes {

    ShuntCompensatorToShuntCompensator() {
        // XXX LUMA ensure that all attributes used in update config are present in IIDM
        addSimpleUpdate("bPerSection", "cim:LinearShuntCompensator.bPerSection", CgmesSubset.EQUIPMENT, false);
        addSimpleUpdate("maximumSectionCount", "cim:ShuntCompensator.maximumSections", CgmesSubset.EQUIPMENT, false);
        addSimpleUpdate("nomU", "cim:ShuntCompensator.nomU", CgmesSubset.EQUIPMENT, false);
        addSimpleUpdate("normalSections", "cim:ShuntCompensator.normalSections", CgmesSubset.EQUIPMENT, false);
    }

}
