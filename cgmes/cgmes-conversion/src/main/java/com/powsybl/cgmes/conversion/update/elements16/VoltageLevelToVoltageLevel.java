package com.powsybl.cgmes.conversion.update.elements16;

import com.powsybl.cgmes.conversion.update.IidmToCgmes;
import com.powsybl.cgmes.model.CgmesSubset;

public class VoltageLevelToVoltageLevel extends IidmToCgmes {

    VoltageLevelToVoltageLevel() {
        simpleUpdate("highVoltageLimit", "cim:VoltageLevel.highVoltageLimit", CgmesSubset.EQUIPMENT);
        simpleUpdate("lowVoltageLimit", "cim:VoltageLevel.lowVoltageLimit", CgmesSubset.EQUIPMENT);
    }

}
