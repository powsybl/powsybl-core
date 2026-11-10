/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export.elements;

import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.cgmes.conversion.export.CgmesExportUtil;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import static com.powsybl.cgmes.model.CgmesNames.*;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public final class CgmesConnectivityNode {

    private static final String CONNECTIVITY_NODE_CONNECTIVITY_NODE_CONTAINER = CONNECTIVITY_NODE + "." + CONNECTIVITY_NODE_CONTAINER;

    private CgmesConnectivityNode() {
    }

    public static void writeEq(ConnectivityNodeEq node, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String namespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(CONNECTIVITY_NODE, node.rdfId, true, writer, context);
        CgmesExportUtil.writeAttribute(namespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(node.name), writer);
        CgmesExportUtil.writeResource(CONNECTIVITY_NODE_CONNECTIVITY_NODE_CONTAINER, node.connectivityNodeContainer, writer, context);
        CgmesExportUtil.writeEnd(writer);
    }

    public record ConnectivityNodeEq(
        String rdfId,
        String name,
        String connectivityNodeContainer
    ) {
    }

}
