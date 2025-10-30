package com.powsybl.iidm.network.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.VoltageLevelLoadCharacteristics;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
@AutoService(ExtensionAdderProvider.class)
public class VoltageLevelLoadCharacteristicsAdderImplProvider implements
        ExtensionAdderProvider<VoltageLevel, VoltageLevelLoadCharacteristics, VoltageLevelLoadCharacteristicsAdderImpl> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return VoltageLevelLoadCharacteristics.NAME;
    }

    @Override
    public Class<VoltageLevelLoadCharacteristicsAdderImpl> getAdderClass() {
        return VoltageLevelLoadCharacteristicsAdderImpl.class;
    }

    @Override
    public VoltageLevelLoadCharacteristicsAdderImpl newAdder(VoltageLevel voltageLevel) {
        return new VoltageLevelLoadCharacteristicsAdderImpl(voltageLevel);
    }
}
