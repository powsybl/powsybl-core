package com.powsybl.iidm.network.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.ObservabilityArea;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
@AutoService(ExtensionAdderProvider.class)
public class ObservabilityAreaAdderImplProvider implements ExtensionAdderProvider<VoltageLevel, ObservabilityArea, ObservabilityAreaAdderImpl> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return ObservabilityArea.NAME;
    }

    @Override
    public Class<? super ObservabilityAreaAdderImpl> getAdderClass() {
        return ObservabilityAreaAdderImpl.class;
    }

    @Override
    public ObservabilityAreaAdderImpl newAdder(VoltageLevel voltageLevel) {
        return new ObservabilityAreaAdderImpl(voltageLevel);
    }
}
