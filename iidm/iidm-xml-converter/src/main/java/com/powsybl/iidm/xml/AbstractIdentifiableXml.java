/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.IdentifiableAdder;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class AbstractIdentifiableXml<T extends Identifiable<? super T>, A extends IdentifiableAdder<T, A>, P extends Identifiable> {

    protected abstract String getRootElementName();

    protected boolean isValid(T identifiable, P parent) {
        return true;
    }

    protected abstract void writeRootElementAttributes(T identifiable, P parent, NetworkXmlWriterContext context);

    protected void writeSubElements(T identifiable, P parent, NetworkXmlWriterContext context) {
    }

    public final void write(T identifiable, P parent, NetworkXmlWriterContext context) {
        if (!isValid(identifiable, parent)) {
            return;
        }
        context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), getRootElementName());
        context.getWriter().writeStringAttribute("id", context.getAnonymizer().anonymizeString(identifiable.getId()));
        ((Identifiable<?>) identifiable).getOptionalName().ifPresent(name -> {
            context.getWriter().writeStringAttribute("name", context.getAnonymizer().anonymizeString(name));
        });

        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_2, context, () -> context.getWriter().writeBooleanAttribute("fictitious", identifiable.isFictitious(), false));

        writeRootElementAttributes(identifiable, parent, context);

        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_3, context, () -> {
            AliasesXml.write(identifiable, getRootElementName(), context);
        });

        PropertiesXml.write(identifiable, context);

        writeSubElements(identifiable, parent, context);

        context.getWriter().writeEndNode();

        context.addExportedEquipment(identifiable);
    }

    protected abstract A createAdder(P parent);

    public abstract void read(P parent, NetworkXmlReaderContext context);

    protected String readIdentifierAttributes(A adder, NetworkXmlReaderContext context) {
        String id = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute(null, "id"));
        String name = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute(null, "name"));
        adder.setId(id)
                .setName(name);
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_2, context, () -> {
            boolean fictitious = context.getReader().readBooleanAttribute("fictitious", false);
            adder.setFictitious(fictitious);
        });
        return id;
    }

    protected void readUntilEndRootElement(XMLStreamReader reader, XmlUtil.XmlEventHandler eventHandler) throws XMLStreamException {
        XmlUtil.readUntilEndElement(getRootElementName(), reader, eventHandler);
    }
}
