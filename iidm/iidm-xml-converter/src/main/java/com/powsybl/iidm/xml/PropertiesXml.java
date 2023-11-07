/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.xml;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.ValidationLevel;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
public final class PropertiesXml {

    static final String ROOT_ELEMENT_NAME = "property";
    static final String ARRAY_ELEMENT_NAME = "properties";

    static final String NAME = "name";
    static final String VALUE = "value";

    public static void write(Identifiable<?> identifiable, NetworkXmlWriterContext context) {
        if (identifiable.hasProperty()) {
            context.getWriter().writeStartNodes(ARRAY_ELEMENT_NAME);
            for (String name : IidmXmlUtil.sortedNames(identifiable.getPropertyNames(), context.getOptions())) {
                String value = identifiable.getProperty(name);
                context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(identifiable.getNetwork().getValidationLevel() == ValidationLevel.STEADY_STATE_HYPOTHESIS), ROOT_ELEMENT_NAME);
                context.getWriter().writeStringAttribute(NAME, name);
                context.getWriter().writeStringAttribute(VALUE, value);
                context.getWriter().writeEndNode();
            }
            context.getWriter().writeEndNodes();
        }
    }

    public static void read(Identifiable identifiable, NetworkXmlReaderContext context) {
        read(context).accept(identifiable);
    }

    public static <T extends Identifiable> void read(List<Consumer<T>> toApply, NetworkXmlReaderContext context) {
        toApply.add(read(context));
    }

    private static <T extends Identifiable> Consumer<T> read(NetworkXmlReaderContext context) {
        String nodeName = context.getReader().getNodeName();
        if (!nodeName.equals(ROOT_ELEMENT_NAME) && !nodeName.equals(ARRAY_ELEMENT_NAME)) {
            throw new IllegalStateException();
        }
        String name = context.getReader().readStringAttribute(NAME);
        String value = context.getReader().readStringAttribute(VALUE);
        return identifiable -> identifiable.setProperty(name, value);
    }

    private PropertiesXml() {
    }
}
