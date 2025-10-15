package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.VoltageLevelLoadCharacteristics;

import java.util.Objects;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
public class VoltageLevelLoadCharacteristicsImpl extends AbstractExtension<VoltageLevel> implements VoltageLevelLoadCharacteristics {

    private final String characteristic;

    public VoltageLevelLoadCharacteristicsImpl(VoltageLevel voltageLevel, String characteristic) {
        super(voltageLevel);
        this.characteristic = Objects.requireNonNull(characteristic);
    }

    @Override
    public String getCharacteristic() {
        return characteristic;
    }
}
