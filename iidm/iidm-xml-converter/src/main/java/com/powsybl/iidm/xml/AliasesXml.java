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

    public static void write(Identifiable<?> identifiable, String rootElementName, NetworkXmlWriterContext context) {
        IidmXmlUtil.assertMinimumVersionIfNotDefault(!identifiable.getAliases().isEmpty(), rootElementName, ALIAS, IidmXmlUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmXmlVersion.V_1_3, context);
        for (String alias : identifiable.getAliases()) {
            context.getWriter().writeStartElement(context.getVersion().getNamespaceURI(context.isValid()), ALIAS);
            IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_4, context, () -> identifiable.getAliasType(alias).ifPresent(type -> {
                context.getWriter().writeStringAttribute("type", type);
            }));
            context.getWriter().writeElementContent(context.getAnonymizer().anonymizeString(alias));
            context.getWriter().writeEndElement();
        }
    }

    public static void read(Identifiable<?> identifiable, NetworkXmlReaderContext context) throws XMLStreamException {
        assert context.getReader().getLocalName().equals(ALIAS);
        String[] aliasType = new String[1];
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_4, context, () -> aliasType[0] = context.getReader().getAttributeValue(null, "type"));
        String alias = context.getAnonymizer().deanonymizeString(context.getReader().getElementText());
        identifiable.addAlias(alias, aliasType[0]);
    }

    private AliasesXml() {
    }
}
