/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.serde;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.io.TreeDataWriter;
import com.powsybl.iidm.network.PropertiesHolder;
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

    public static void write(PropertiesHolder propertiesHolder, TreeDataWriter writer, String nsUri, ExportOptions exportOptions) {
        if (propertiesHolder.hasProperty()) {
            writer.writeStartNodes();
            for (String name : IidmSerDeUtil.sortedNames(propertiesHolder.getPropertyNames(), exportOptions)) {
                String value = propertiesHolder.getProperty(name);
                writer.writeStartNode(nsUri, ROOT_ELEMENT_NAME);
                writer.writeStringAttribute(NAME, name);
                writer.writeStringAttribute(VALUE, value);
                writer.writeEndNode();
            }
            writer.writeEndNodes();
        }
    }

    public static void write(PropertiesHolder propertiesHolder, NetworkSerializerContext context) {
        write(propertiesHolder, context.getWriter(), context.getNamespaceURI(), context.getOptions());
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

    public static void readProperties(NetworkDeserializerContext context, PropertiesHolder holder) {
        if (context.getVersion().compareTo(IidmVersion.V_1_16) >= 0) {
            context.getReader().readChildNodes(elementName -> {
                if (elementName.equals(PropertiesSerDe.ROOT_ELEMENT_NAME)) {
                    String name = context.getReader().readStringAttribute(NAME);
                    String value = context.getReader().readStringAttribute(VALUE);
                    context.getReader().readEndNode();
                    holder.setProperty(name, value);
                } else {
                    throw new PowsyblException(String.format("Unknown element name '%s' in '%s'", elementName, holder.getClass().getSimpleName()));
                }
            });
        } else {
            context.getReader().readEndNode();
        }
    }
}
