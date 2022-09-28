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

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public final class PropertiesXml {

    static final String PROPERTY = "property";

    private static final String NAME = "name";
    private static final String VALUE = "value";

    public static void write(Identifiable<?> identifiable, NetworkXmlWriterContext context) {
        if (identifiable.hasProperty()) {
            context.getWriter().writeStartNodes("properties");
            for (String name : IidmXmlUtil.sortedNames(identifiable.getPropertyNames(), context.getOptions())) {
                String value = identifiable.getProperty(name);
                context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(identifiable.getNetwork().getValidationLevel() == ValidationLevel.STEADY_STATE_HYPOTHESIS), PROPERTY);
                context.getWriter().writeStringAttribute(NAME, name);
                context.getWriter().writeStringAttribute(VALUE, value);
                context.getWriter().writeEndNode();
            }
            context.getWriter().writeEndNodes();
        }
    }

    public static void read(Identifiable identifiable, NetworkXmlReaderContext context) {
        assert context.getReader().getNodeName().equals(PROPERTY);
        String name = context.getReader().readStringAttribute(NAME);
        String value = context.getReader().readStringAttribute(VALUE);
        identifiable.setProperty(name, value);
    }

    private PropertiesXml() {
    }
}
