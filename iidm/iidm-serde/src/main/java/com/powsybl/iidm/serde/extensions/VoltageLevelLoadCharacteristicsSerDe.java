package com.powsybl.iidm.serde.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionSerDe;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.VoltageLevelLoadCharacteristics;
import com.powsybl.iidm.network.extensions.VoltageLevelLoadCharacteristicsAdder;
import com.powsybl.iidm.network.extensions.VoltageLevelLoadCharacteristicsType;

/**
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
@AutoService(ExtensionSerDe.class)
public class VoltageLevelLoadCharacteristicsSerDe extends AbstractExtensionSerDe<VoltageLevel, VoltageLevelLoadCharacteristics> {

    public VoltageLevelLoadCharacteristicsSerDe() {
        super(VoltageLevelLoadCharacteristics.NAME, "network", VoltageLevelLoadCharacteristics.class, "voltageLevelLoadCharacteristics.xsd",
                "http://www.powsybl.org/schema/iidm/ext/voltageLevel_load_characteristics/1_0", "vllc");
    }

    @Override
    public void write(VoltageLevelLoadCharacteristics voltageLevelLoadCharacteristics, SerializerContext context) {
        context.getWriter().writeEnumAttribute("characteristic", voltageLevelLoadCharacteristics.getCharacteristic());
    }

    @Override
    public VoltageLevelLoadCharacteristics read(VoltageLevel voltageLevel, DeserializerContext context) {
        VoltageLevelLoadCharacteristicsType characteristic = context.getReader().readEnumAttribute("characteristic", VoltageLevelLoadCharacteristicsType.class);
        context.getReader().readEndNode();
        return voltageLevel.newExtension(VoltageLevelLoadCharacteristicsAdder.class)
                .withCharacteristic(characteristic)
                .add();
    }

}
