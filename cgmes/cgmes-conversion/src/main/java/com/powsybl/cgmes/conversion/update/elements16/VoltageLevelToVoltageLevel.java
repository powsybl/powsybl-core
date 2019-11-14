package com.powsybl.cgmes.conversion.update.elements16;

import com.powsybl.cgmes.conversion.update.IidmToCgmes;
import com.powsybl.cgmes.model.CgmesSubset;

public class VoltageLevelToVoltageLevel extends IidmToCgmes {

    VoltageLevelToVoltageLevel() {
        addSimpleUpdate("highVoltageLimit", "cim:VoltageLevel.highVoltageLimit", CgmesSubset.EQUIPMENT, false);
        addSimpleUpdate("lowVoltageLimit", "cim:VoltageLevel.lowVoltageLimit", CgmesSubset.EQUIPMENT, false);
    }

}
