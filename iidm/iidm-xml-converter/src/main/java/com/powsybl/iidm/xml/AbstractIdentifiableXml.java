/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.IdentifiableAdder;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
abstract class AbstractIdentifiableXml<T extends Identifiable<? super T>, A extends IdentifiableAdder<T, A>, P extends Identifiable> {

    protected abstract String getRootElementName();

    protected abstract boolean hasSubElements(T identifiable);

    protected boolean hasSubElements(T identifiable, NetworkXmlWriterContext context) {
        return hasSubElements(identifiable);
    }

    protected boolean isValid(T identifiable, P parent) {
        return true;
    }

    protected abstract void writeRootElementAttributes(T identifiable, P parent, NetworkXmlWriterContext context) throws XMLStreamException;

    protected void writeSubElements(T identifiable, P parent, NetworkXmlWriterContext context) throws XMLStreamException {
    }

    public final void write(T identifiable, P parent, NetworkXmlWriterContext context) throws XMLStreamException {
        if (!isValid(identifiable, parent)) {
            return;
        }
        boolean isNotEmptyElement = hasSubElements(identifiable, context) || identifiable.hasProperty() || identifiable.hasAliases();
        if (isNotEmptyElement) {
            context.getWriter().writeStartElement(context.getVersion().getNamespaceURI(context.isValid()), getRootElementName());
        } else {
            context.getWriter().writeEmptyElement(context.getVersion().getNamespaceURI(context.isValid()), getRootElementName());
        }
        context.getWriter().writeAttribute("id", context.getAnonymizer().anonymizeString(identifiable.getId()));
        ((Identifiable<?>) identifiable).getOptionalName().ifPresent(name -> {
            try {
                context.getWriter().writeAttribute("name", context.getAnonymizer().anonymizeString(name));
            } catch (XMLStreamException e) {
                throw new UncheckedXmlStreamException(e);
            }
        });

        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_2, context, () -> XmlUtil.writeOptionalBoolean("fictitious", identifiable.isFictitious(), false, context.getWriter()));

        writeRootElementAttributes(identifiable, parent, context);

        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_3, context, () -> {
            try {
                AliasesXml.write(identifiable, getRootElementName(), context);
            } catch (XMLStreamException e) {
                throw new UncheckedXmlStreamException(e);
            }
        });

        PropertiesXml.write(identifiable, context);

        writeSubElements(identifiable, parent, context);
        if (isNotEmptyElement) {
            context.getWriter().writeEndElement();
        }

        context.addExportedEquipment(identifiable);
    }

    protected abstract A createAdder(P parent);

    public abstract void read(P parent, NetworkXmlReaderContext context) throws XMLStreamException;

    protected String readIdentifierAttributes(A adder, NetworkXmlReaderContext context) {
        String id = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "id"));
        String name = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "name"));
        adder.setId(id)
                .setName(name);
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_2, context, () -> {
            boolean fictitious = XmlUtil.readOptionalBoolAttribute(context.getReader(), "fictitious", false);
            adder.setFictitious(fictitious);
        });
        return id;
    }

    protected void readUntilEndRootElement(XMLStreamReader reader, XmlUtil.XmlEventHandler eventHandler) throws XMLStreamException {
        XmlUtil.readUntilEndElement(getRootElementName(), reader, eventHandler);
    }
}
