/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
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
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.xml.NetworkXmlReaderContext;
import com.powsybl.iidm.xml.NetworkXmlWriterContext;

import javax.xml.stream.XMLStreamException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 * <p>
 * WARNING: this class is still in a beta version, it will change in the future
 */
@AutoService(ExtensionXmlSerializer.class)
public class CgmesIidmMappingXmlSerializer extends AbstractExtensionXmlSerializer<Network, CgmesIidmMapping> {

    private static final String SOURCE = "source";

    public CgmesIidmMappingXmlSerializer() {
        super("cgmesIidmMapping", "network", CgmesIidmMapping.class, true, "cgmesIidmMapping.xsd",
                "http://www.powsybl.org/schema/iidm/ext/cgmes_iidm_mapping/1_0", "ci");
    }

    @Override
    public void write(CgmesIidmMapping extension, XmlWriterContext context) throws XMLStreamException {
        NetworkXmlWriterContext networkContext = (NetworkXmlWriterContext) context;
        Map<Double, CgmesIidmMapping.BaseVoltageSource> sortedBaseVoltages = extension.getBaseVoltages().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                    (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        sortedBaseVoltages.forEach((nominalV, baseVoltageSource) -> {
            try {
                context.getWriter().writeEmptyElement(getNamespaceUri(), "baseVoltage");
                context.getWriter().writeAttribute("nominalVoltage", Double.toString(nominalV));
                context.getWriter().writeAttribute(SOURCE, baseVoltageSource.getSource().name());
                context.getWriter().writeAttribute("id", baseVoltageSource.getCgmesId());
            } catch (XMLStreamException e) {
                throw new UncheckedXmlStreamException(e);
            }
        });
    }

    @Override
    public CgmesIidmMapping read(Network extendable, XmlReaderContext context) throws XMLStreamException {
        NetworkXmlReaderContext networkContext = (NetworkXmlReaderContext) context;
        CgmesIidmMappingAdder mappingAdder = extendable.newExtension(CgmesIidmMappingAdder.class);
        mappingAdder.add();
        CgmesIidmMapping mapping = extendable.getExtension(CgmesIidmMapping.class);
        XmlUtil.readUntilEndElement("cgmesIidmMapping", context.getReader(), () -> {
            switch (context.getReader().getLocalName()) {
                case "baseVoltage":
                    double nominalV = Double.parseDouble(context.getReader().getAttributeValue(null, "nominalVoltage"));
                    String sourceBV = context.getReader().getAttributeValue(null, SOURCE);
                    String baseVoltageId = context.getReader().getAttributeValue(null, "id");
                    mapping.addBaseVoltage(nominalV, baseVoltageId, CgmesIidmMapping.Source.valueOf(sourceBV));
                    break;
                default:
                    throw new PowsyblException("Unknown element name <" + context.getReader().getLocalName() + "> in <cgmesIidmMapping>");
            }
        });
        return mapping;
    }

    /**
     * A {@link CgmesIidmMapping} extension is serializable if it contains at least one link
     *
     * @param cgmesIidmMapping The extension to check
     * @return true if it contains at least one link, false if not
     */
    @Override
    public boolean isSerializable(CgmesIidmMapping cgmesIidmMapping) {
        return !cgmesIidmMapping.isBaseVoltageEmpty();
    }
}
