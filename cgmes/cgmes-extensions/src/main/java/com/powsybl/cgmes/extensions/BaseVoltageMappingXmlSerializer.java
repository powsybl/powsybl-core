/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.commons.extensions.AbstractExtensionXmlSerializer;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.Network;

import javax.xml.stream.XMLStreamException;
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
    public void write(BaseVoltageMapping extension, XmlWriterContext context) throws XMLStreamException {
        Map<Double, BaseVoltageMapping.BaseVoltageSource> sortedBaseVoltages = extension.getBaseVoltages().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        sortedBaseVoltages.forEach((nominalV, baseVoltageSource) -> {
            try {
                context.getWriter().writeEmptyElement(getNamespaceUri(), "baseVoltage");
                context.getWriter().writeAttribute("nominalVoltage", Double.toString(nominalV));
                context.getWriter().writeAttribute("source", baseVoltageSource.getSource().name());
                context.getWriter().writeAttribute("id", baseVoltageSource.getId());
            } catch (XMLStreamException e) {
                throw new UncheckedXmlStreamException(e);
            }
        });
    }

    @Override
    public BaseVoltageMapping read(Network extendable, XmlReaderContext context) throws XMLStreamException {
        BaseVoltageMappingAdder mappingAdder = extendable.newExtension(BaseVoltageMappingAdder.class);
        mappingAdder.add();
        BaseVoltageMapping mapping = extendable.getExtension(BaseVoltageMapping.class);
        XmlUtil.readUntilEndElement(getName(), context.getReader(), () -> {
            if (context.getReader().getLocalName().equals("baseVoltage")) {
                double nominalV = Double.parseDouble(context.getReader().getAttributeValue(null, "nominalVoltage"));
                String sourceBV = context.getReader().getAttributeValue(null, "source");
                String baseVoltageId = context.getReader().getAttributeValue(null, "id");
                mapping.addBaseVoltage(nominalV, baseVoltageId, Source.valueOf(sourceBV));
            } else {
                throw new PowsyblException("Unknown element name <" + context.getReader().getLocalName() + "> in <baseVoltageMapping>");
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
