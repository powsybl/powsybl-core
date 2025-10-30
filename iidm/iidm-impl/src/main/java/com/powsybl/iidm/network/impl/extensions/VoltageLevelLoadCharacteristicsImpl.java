package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.VoltageLevelLoadCharacteristics;
import com.powsybl.iidm.network.extensions.VoltageLevelLoadCharacteristicsType;

import java.util.Objects;

/**
 *
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
public class VoltageLevelLoadCharacteristicsImpl extends AbstractExtension<VoltageLevel> implements VoltageLevelLoadCharacteristics {

    private VoltageLevelLoadCharacteristicsType characteristic;

    public VoltageLevelLoadCharacteristicsImpl(VoltageLevel voltageLevel, VoltageLevelLoadCharacteristicsType characteristic) {
        super(voltageLevel);
        this.characteristic = Objects.requireNonNull(characteristic);
    }

    @Override
    public VoltageLevelLoadCharacteristicsType getCharacteristic() {
        return characteristic;
    }

    @Override
    public void setCharacteristic(VoltageLevelLoadCharacteristicsType characteristic) {
        this.characteristic = characteristic;
    }
}
