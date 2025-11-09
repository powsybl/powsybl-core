/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.*;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public class DcGroundSerDe extends AbstractSimpleIdentifiableSerDe<DcGround, DcGroundAdder, Network> {

    static final DcGroundSerDe INSTANCE = new DcGroundSerDe();
    static final String ROOT_ELEMENT_NAME = "dcGround";
    static final String ARRAY_ELEMENT_NAME = "dcGrounds";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(final DcGround dcGround, final Network parent, final NetworkSerializerContext context) {
        DcTerminal dcTerminal = dcGround.getDcTerminal();
        context.getWriter().writeStringAttribute("dcNode", dcTerminal.getDcNode().getId());
        context.getWriter().writeDoubleAttribute("r", dcGround.getR());
        context.getWriter().writeBooleanAttribute("connected", dcTerminal.isConnected());
        context.getWriter().writeDoubleAttribute("p", dcTerminal.getP());
        context.getWriter().writeDoubleAttribute("i", dcTerminal.getI());
    }

    @Override
    protected DcGroundAdder createAdder(final Network network) {
        return network.newDcGround();
    }

    @Override
    protected DcGround readRootElementAttributes(final DcGroundAdder adder, final Network parent, final NetworkDeserializerContext context) {
        String dcNodeId = context.getReader().readStringAttribute("dcNode");
        double r = context.getReader().readDoubleAttribute("r");
        boolean connected = context.getReader().readBooleanAttribute("connected");
        double p = context.getReader().readDoubleAttribute("p");
        double i = context.getReader().readDoubleAttribute("i");
        DcGround dcGround = adder
                .setDcNode(dcNodeId)
                .setR(r)
                .add();
        dcGround.getDcTerminal()
                .setI(i)
                .setP(p)
                .setConnected(connected);
        return dcGround;
    }

    @Override
    protected void readSubElements(final DcGround dcGround, final NetworkDeserializerContext context) {
        context.getReader().readChildNodes(elementName -> readSubElement(elementName, dcGround, context));
    }
}
