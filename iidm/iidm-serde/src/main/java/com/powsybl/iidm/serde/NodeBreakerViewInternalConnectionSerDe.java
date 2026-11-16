/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.VoltageLevel;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class NodeBreakerViewInternalConnectionSerDe {

    static final NodeBreakerViewInternalConnectionSerDe INSTANCE = new NodeBreakerViewInternalConnectionSerDe();
    static final String ROOT_ELEMENT_NAME = "internalConnection";
    static final String ARRAY_ELEMENT_NAME = "internalConnections";

    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    protected void write(int node1, int node2, NetworkSerializerContext context) {
        context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), getRootElementName());
        context.getWriter().writeIntAttribute("node1", node1);
        context.getWriter().writeIntAttribute("node2", node2);
        context.getWriter().writeEndNode();
    }

    protected void read(VoltageLevel vl, NetworkDeserializerContext context) {
        int node1 = context.getReader().readIntAttribute("node1");
        int node2 = context.getReader().readIntAttribute("node2");
        context.getReader().readEndNode();
        vl.getNodeBreakerView().newInternalConnection().setNode1(node1).setNode2(node2).add();
    }
}
