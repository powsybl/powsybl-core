/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
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
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.xml.NetworkXmlReaderContext;
import com.powsybl.iidm.xml.NetworkXmlWriterContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class CgmesTapChangersXmlSerializer<C extends Connectable<C>> extends AbstractExtensionXmlSerializer<C, CgmesTapChangers<C>> {

    public CgmesTapChangersXmlSerializer() {
        super("cgmesTapChangers", "network", CgmesTapChangers.class,
                true, "cgmesTapChangers.xsd",
                "http://www.powsybl.org/schema/iidm/ext/cgmes_tap_changers/1_0", "ctc");
    }

    @Override
    public void write(CgmesTapChangers<C> extension, XmlWriterContext context) throws XMLStreamException {
        NetworkXmlWriterContext networkContext = (NetworkXmlWriterContext) context;
        XMLStreamWriter writer = networkContext.getWriter();
        for (CgmesTapChanger tapChanger : extension.getTapChangers()) {
            writer.writeStartElement(getNamespaceUri(), "tapChanger");
            writer.writeAttribute("id", tapChanger.getId());
            if (tapChanger.getCombinedTapChangerId() != null) {
                writer.writeAttribute("combinedTapChangerId", tapChanger.getCombinedTapChangerId());
            }
            if (tapChanger.getType() != null) {
                writer.writeAttribute("type", tapChanger.getType());
            }
            if (tapChanger.isHidden()) {
                writer.writeAttribute("hidden", "true");
                writer.writeAttribute("step", String.valueOf(tapChanger.getStep()
                        .orElseThrow(() -> new PowsyblException("Step should be defined"))));
            }
            if (tapChanger.getControlId() != null) {
                writer.writeAttribute("controlId", tapChanger.getControlId());
            }
            writer.writeEndElement();
        }
    }

    @Override
    public CgmesTapChangers<C> read(C extendable, XmlReaderContext context) throws XMLStreamException {
        NetworkXmlReaderContext networkContext = (NetworkXmlReaderContext) context;
        XMLStreamReader reader = networkContext.getReader();
        extendable.newExtension(CgmesTapChangersAdder.class).add();
        CgmesTapChangers<C> tapChangers = extendable.getExtension(CgmesTapChangers.class);
        XmlUtil.readUntilEndElement(getExtensionName(), reader, () -> {
            if (reader.getLocalName().equals("tapChanger")) {
                CgmesTapChangerAdder adder = tapChangers.newTapChanger()
                        .setId(reader.getAttributeValue(null, "id"))
                        .setCombinedTapChangerId(reader.getAttributeValue(null, "combinedTapChangerId"))
                        .setType(reader.getAttributeValue(null, "type"))
                        .setHiddenStatus(XmlUtil.readOptionalBoolAttribute(reader, "hidden", false))
                        .setControlId(reader.getAttributeValue(null, "controlId"));
                String stepStr = reader.getAttributeValue(null, "step");
                if (stepStr != null) {
                    adder.setStep(Integer.parseInt(stepStr));
                }
                adder.add();
            } else {
                throw new PowsyblException("Unknown element name <" + reader.getLocalName() + "> in <cgmesTapChangers>");
            }
        });
        return extendable.getExtension(CgmesTapChangers.class);
    }
}
