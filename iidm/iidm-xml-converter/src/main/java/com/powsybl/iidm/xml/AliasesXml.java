/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

import javax.xml.stream.XMLStreamException;

/**
 * @author Sebastien Murgey <sebastien.murgey at rte-france.com>
 */
public final class AliasesXml {

    static final String ALIAS = "alias";

    public static void write(Identifiable<?> identifiable, String rootElementName, NetworkXmlWriterContext context) throws XMLStreamException {
        IidmXmlUtil.assertMinimumVersionIfNotDefault(!identifiable.getAliases().isEmpty(), rootElementName, ALIAS, IidmXmlUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmXmlVersion.V_1_3, context);
        for (String alias : identifiable.getAliases()) {
            context.getWriter().writeStartElement(context.getVersion().getNamespaceURI(), ALIAS);
            context.getWriter().writeCharacters(context.getAnonymizer().anonymizeString(alias));
            context.getWriter().writeEndElement();
        }
    }

    public static void read(Identifiable<?> identifiable, NetworkXmlReaderContext context) throws XMLStreamException {
        assert context.getReader().getLocalName().equals(ALIAS);
        String alias = context.getAnonymizer().deanonymizeString(context.getReader().getElementText());
        identifiable.addAlias(alias);
    }

    private AliasesXml() {
    }
}
