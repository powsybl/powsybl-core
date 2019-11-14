package com.powsybl.cgmes.conversion.update.elements16;

import com.powsybl.cgmes.conversion.update.IidmToCgmes;

public class VoltageLevelToVoltageLevel extends IidmToCgmes {

    VoltageLevelToVoltageLevel() {
        addSimpleUpdate("highVoltageLimit", "cim:VoltageLevel.highVoltageLimit", "_EQ", false);
        addSimpleUpdate("lowVoltageLimit", "cim:VoltageLevel.lowVoltageLimit", "_EQ", false);
    }

}
