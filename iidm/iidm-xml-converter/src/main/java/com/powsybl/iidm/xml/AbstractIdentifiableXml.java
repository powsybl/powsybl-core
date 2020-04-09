/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.IdentifiableAdder;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class AbstractIdentifiableXml<T extends Identifiable, A extends IdentifiableAdder<A>, P extends Identifiable> {

    protected abstract String getRootElementName();

    protected abstract boolean hasSubElements(T identifiable);

    protected abstract void writeRootElementAttributes(T identifiable, P parent, NetworkXmlWriterContext context) throws XMLStreamException;

    protected void writeSubElements(T identifiable, P parent, NetworkXmlWriterContext context) throws XMLStreamException {
    }

    public final void write(T identifiable, P parent, NetworkXmlWriterContext context) throws XMLStreamException {
        boolean hasSubElements = hasSubElements(identifiable);
        if (hasSubElements || identifiable.hasProperty()) {
            context.getWriter().writeStartElement(context.getVersion().getNamespaceURI(), getRootElementName());
        } else {
            context.getWriter().writeEmptyElement(context.getVersion().getNamespaceURI(), getRootElementName());
        }
        context.getWriter().writeAttribute("id", context.getAnonymizer().anonymizeString(identifiable.getId()));
        ((Identifiable<?>) identifiable).getOptionalName().ifPresent(name -> {
            try {
                context.getWriter().writeAttribute("name", context.getAnonymizer().anonymizeString(name));
            } catch (XMLStreamException e) {
                throw new UncheckedXmlStreamException(e);
            }
        });

        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_2, context, () -> {
            try {
                XmlUtil.writeOptionalBoolean("fictitious", identifiable.isFictitious(), false, context.getWriter());
            } catch (XMLStreamException e) {
                throw new UncheckedXmlStreamException(e);
            }
        });

        writeRootElementAttributes(identifiable, parent, context);

        PropertiesXml.write(identifiable, context);

        writeSubElements(identifiable, parent, context);
        if (hasSubElements || identifiable.hasProperty()) {
            context.getWriter().writeEndElement();
        }

        context.addExportedEquipment(identifiable);
    }

    protected void readUntilEndRootElement(XMLStreamReader reader, XmlUtil.XmlEventHandler eventHandler) throws XMLStreamException {
        XmlUtil.readUntilEndElement(getRootElementName(), reader, eventHandler);
    }

    protected abstract A createAdder(P parent);

    protected abstract T readRootElementAttributes(A adder, NetworkXmlReaderContext context);

    protected void readSubElements(T identifiable, NetworkXmlReaderContext context) throws XMLStreamException {
        if (context.getReader().getLocalName().equals("property")) {
            PropertiesXml.read(identifiable, context);
        } else {
            throw new PowsyblException("Unknown element name <" + context.getReader().getLocalName() + "> in <" + identifiable.getId() + ">");
        }
    }

    protected void readElement(String id, A adder, NetworkXmlReaderContext context) throws XMLStreamException {
        T identifiable = readRootElementAttributes(adder, context);
        readSubElements(identifiable, context);
    }

    public final void read(P parent, NetworkXmlReaderContext context) throws XMLStreamException {
        A adder = createAdder(parent);
        String id = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "id"));
        String name = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "name"));
        adder.setId(id)
                .setName(name);
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_2, context, () -> {
            boolean fictitious = XmlUtil.readOptionalBoolAttribute(context.getReader(), "fictitious", false);
            adder.setFictitious(fictitious);
        });
        readElement(id, adder, context);
    }
}
