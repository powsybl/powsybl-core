/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.PropertiesHolder;
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

    public static void write(PropertiesHolder propertiesHolder, NetworkSerializerContext context) {
        if (propertiesHolder.hasProperty()) {
            context.getWriter().writeStartNodes();
            for (String name : IidmSerDeUtil.sortedNames(propertiesHolder.getPropertyNames(), context.getOptions())) {
                String value = propertiesHolder.getProperty(name);
                context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(propertiesHolder.getNetwork().getValidationLevel() == ValidationLevel.STEADY_STATE_HYPOTHESIS), ROOT_ELEMENT_NAME);
                context.getWriter().writeStringAttribute(NAME, name);
                context.getWriter().writeStringAttribute(VALUE, value);
                context.getWriter().writeEndNode();
            }
            context.getWriter().writeEndNodes();
        }
    }

    public static void read(PropertiesHolder propertiesHolder, NetworkDeserializerContext context) {
        read(context).accept(propertiesHolder);
    }

    public static <T extends PropertiesHolder> void read(List<Consumer<T>> toApply, NetworkDeserializerContext context) {
        toApply.add(read(context));
    }

    private static <T extends PropertiesHolder> Consumer<T> read(NetworkDeserializerContext context) {
        String name = context.getReader().readStringAttribute(NAME);
        String value = context.getReader().readStringAttribute(VALUE);
        context.getReader().readEndNode();
        return propertiesBearer -> propertiesBearer.setProperty(name, value);
    }

    private PropertiesSerDe() {
    }
}
