package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.VoltageLevelLoadCharacteristics;
import com.powsybl.iidm.network.extensions.VoltageLevelLoadCharacteristicsAdder;
import com.powsybl.iidm.network.extensions.VoltageLevelLoadCharacteristicsType;

import java.util.Objects;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
public class VoltageLevelLoadCharacteristicsAdderImpl extends AbstractExtensionAdder<VoltageLevel, VoltageLevelLoadCharacteristics> implements VoltageLevelLoadCharacteristicsAdder {

    private VoltageLevelLoadCharacteristicsType characteristic;

    protected VoltageLevelLoadCharacteristicsAdderImpl(VoltageLevel voltageLevel) {
        super(voltageLevel);
    }

    @Override
    protected VoltageLevelLoadCharacteristics createExtension(VoltageLevel voltageLevel) {
        return new VoltageLevelLoadCharacteristicsImpl(voltageLevel, characteristic);
    }

    @Override
    public VoltageLevelLoadCharacteristicsAdder withCharacteristic(VoltageLevelLoadCharacteristicsType characteristic) {
        this.characteristic = Objects.requireNonNull(characteristic);
        return this;
    }
}
