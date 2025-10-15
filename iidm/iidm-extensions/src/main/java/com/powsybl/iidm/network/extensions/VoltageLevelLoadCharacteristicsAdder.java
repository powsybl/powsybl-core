package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.VoltageLevel;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface VoltageLevelLoadCharacteristicsAdder
        extends ExtensionAdder<VoltageLevel, VoltageLevelLoadCharacteristics> {

    @Override
    default Class<VoltageLevelLoadCharacteristics> getExtensionClass() {
        return VoltageLevelLoadCharacteristics.class;
    }

    VoltageLevelLoadCharacteristicsAdder withCharacteristic(String characteristic);
}
