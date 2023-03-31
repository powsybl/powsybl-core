/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author Sebastien Murgey <sebastien.murgey at rte-france.com>
 */
public final class AliasesXml {

    static final String ALIASES = "aliases";
    static final String ALIAS = "alias";

    public static void write(Identifiable<?> identifiable, String rootElementName, NetworkXmlWriterContext context) {
        IidmXmlUtil.assertMinimumVersionIfNotDefault(!identifiable.getAliases().isEmpty(), rootElementName, ALIAS, IidmXmlUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmXmlVersion.V_1_3, context);
        context.getWriter().writeStartNodes(ALIASES);
        for (String alias : identifiable.getAliases()) {
            context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), ALIAS);
            IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_4, context, () -> identifiable.getAliasType(alias).ifPresent(type -> {
                context.getWriter().writeStringAttribute("type", type);
            }));
            context.getWriter().writeNodeContent(context.getAnonymizer().anonymizeString(alias));
            context.getWriter().writeEndNode();
        }
        context.getWriter().writeEndNodes();
    }

    public static <T extends Identifiable> void read(T identifiable, NetworkXmlReaderContext context) {
        read(context).accept(identifiable);
    }

    public static <T extends Identifiable> void read(List<Consumer<T>> toApply, NetworkXmlReaderContext context) {
        toApply.add(read(context));
    }

    private static <T extends Identifiable> Consumer<T> read(NetworkXmlReaderContext context) {
        if (!context.getReader().getNodeName().equals(ALIAS)) {
            throw new IllegalStateException();
        }
        String[] aliasType = new String[1];
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_4, context, () -> aliasType[0] = context.getReader().readStringAttribute("type"));
        String alias = context.getAnonymizer().deanonymizeString(context.getReader().readContent());
        return identifiable -> identifiable.addAlias(alias, aliasType[0]);
    }

    private AliasesXml() {
    }
}
