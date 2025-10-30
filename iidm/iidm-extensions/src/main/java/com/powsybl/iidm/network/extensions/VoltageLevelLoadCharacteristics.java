package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.VoltageLevel;

public interface VoltageLevelLoadCharacteristics extends Extension<VoltageLevel> {

    String NAME = "voltageLevelLoadCharacteristics";

    @Override
    default String getName() {
        return NAME;
    }

    VoltageLevelLoadCharacteristicsType getCharacteristic();

    void setCharacteristic(VoltageLevelLoadCharacteristicsType characteristic);
}
