package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.ObservabilityArea;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
abstract class AbstractAreaCharacteristics implements ObservabilityArea.AreaCharacteristics {

    static class Characteristics {
        private final int areaNumber;
        private final ObservabilityArea.ObservabilityStatus status;

        Characteristics(int areaNumber, ObservabilityArea.ObservabilityStatus status) {
            this.areaNumber = areaNumber;
            this.status = status;
        }

        int getAreaNumber() {
            return areaNumber;
        }

        ObservabilityArea.ObservabilityStatus getStatus() {
            return status;
        }
    }

    protected final VoltageLevel voltageLevel;
    private final Characteristics characteristics;

    AbstractAreaCharacteristics(Characteristics characteristics, VoltageLevel voltageLevel) {
        this.characteristics = characteristics;
        this.voltageLevel = voltageLevel;
    }

    @Override
    public int getAreaNumber() {
        return characteristics.areaNumber;
    }

    @Override
    public ObservabilityArea.ObservabilityStatus getStatus() {
        return characteristics.status;
    }

    Characteristics getCharacteristics() {
        return characteristics;
    }
}
