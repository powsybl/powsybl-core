/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.PropertiesBearer;
import com.powsybl.iidm.network.ValidationLevel;
import com.powsybl.iidm.serde.util.IidmSerDeUtil;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
public final class PropertiesSerDe {

    static final String ROOT_ELEMENT_NAME = "property";
    static final String ARRAY_ELEMENT_NAME = "properties";

    static final String NAME = "name";
    static final String VALUE = "value";

    public static void write(PropertiesBearer propertiesBearer, NetworkSerializerContext context) {
        if (propertiesBearer.hasProperty()) {
            context.getWriter().writeStartNodes();
            for (String name : IidmSerDeUtil.sortedNames(propertiesBearer.getPropertyNames(), context.getOptions())) {
                String value = propertiesBearer.getProperty(name);
                context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(propertiesBearer.getNetwork().getValidationLevel() == ValidationLevel.STEADY_STATE_HYPOTHESIS), ROOT_ELEMENT_NAME);
                context.getWriter().writeStringAttribute(NAME, name);
                context.getWriter().writeStringAttribute(VALUE, value);
                context.getWriter().writeEndNode();
            }
            context.getWriter().writeEndNodes();
        }
    }

    public static void read(PropertiesBearer propertiesBearer, NetworkDeserializerContext context) {
        read(context).accept(propertiesBearer);
    }

    public static <T extends PropertiesBearer> void read(List<Consumer<T>> toApply, NetworkDeserializerContext context) {
        toApply.add(read(context));
    }

    private static <T extends PropertiesBearer> Consumer<T> read(NetworkDeserializerContext context) {
        String name = context.getReader().readStringAttribute(NAME);
        String value = context.getReader().readStringAttribute(VALUE);
        context.getReader().readEndNode();
        return propertiesBearer -> propertiesBearer.setProperty(name, value);
    }

    private PropertiesSerDe() {
    }
}
