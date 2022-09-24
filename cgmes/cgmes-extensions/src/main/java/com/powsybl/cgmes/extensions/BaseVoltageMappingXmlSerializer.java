/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionXmlSerializer;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.Network;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class BaseVoltageMappingXmlSerializer extends AbstractExtensionXmlSerializer<Network, BaseVoltageMapping> {

    public BaseVoltageMappingXmlSerializer() {
        super(BaseVoltageMapping.NAME, "network", BaseVoltageMapping.class, true, "baseVoltageMapping.xsd",
                "http://www.powsybl.org/schema/iidm/ext/base_voltage_mapping/1_0", "bv");
    }

    @Override
    public void write(BaseVoltageMapping extension, XmlWriterContext context) {
        Map<Double, BaseVoltageMapping.BaseVoltageSource> sortedBaseVoltages = extension.getBaseVoltages().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        sortedBaseVoltages.forEach((nominalV, baseVoltageSource) -> {
            context.getWriter().writeEmptyNode(getNamespaceUri(), "baseVoltage");
            context.getWriter().writeDoubleAttribute("nominalVoltage", nominalV);
            context.getWriter().writeEnumAttribute("source", baseVoltageSource.getSource());
            context.getWriter().writeStringAttribute("id", baseVoltageSource.getId());
        });
    }

    @Override
    public BaseVoltageMapping read(Network extendable, XmlReaderContext context) {
        BaseVoltageMappingAdder mappingAdder = extendable.newExtension(BaseVoltageMappingAdder.class);
        mappingAdder.add();
        BaseVoltageMapping mapping = extendable.getExtension(BaseVoltageMapping.class);
        context.getReader().readUntilEndNode(getName(), () -> {
            if (context.getReader().getNodeName().equals("baseVoltage")) {
                double nominalV = context.getReader().readDoubleAttribute("nominalVoltage");
                String sourceBV = context.getReader().readStringAttribute("source");
                String baseVoltageId = context.getReader().readStringAttribute("id");
                mapping.addBaseVoltage(nominalV, baseVoltageId, Source.valueOf(sourceBV));
            } else {
                throw new PowsyblException("Unknown element name <" + context.getReader().getNodeName() + "> in <baseVoltageMapping>");
            }
        });
        return mapping;
    }

    /**
     * A {@link BaseVoltageMapping} extension is serializable if it contains at least one base voltage
     *
     * @param extension The extension to check
     * @return true if it contains at least one link, false if not
     */
    @Override
    public boolean isSerializable(BaseVoltageMapping extension) {
        return !extension.isBaseVoltageEmpty();
    }
}
