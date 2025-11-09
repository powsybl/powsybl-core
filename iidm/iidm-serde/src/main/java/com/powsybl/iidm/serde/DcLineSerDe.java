/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.DcLine;
import com.powsybl.iidm.network.DcLineAdder;
import com.powsybl.iidm.network.DcTerminal;
import com.powsybl.iidm.network.Network;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public class DcLineSerDe extends AbstractSimpleIdentifiableSerDe<DcLine, DcLineAdder, Network> {

    static final DcLineSerDe INSTANCE = new DcLineSerDe();
    static final String ROOT_ELEMENT_NAME = "dcLine";
    static final String ARRAY_ELEMENT_NAME = "dcLines";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(final DcLine dcLine, final Network parent, final NetworkSerializerContext context) {
        DcTerminal dcTerminal1 = dcLine.getDcTerminal1();
        DcTerminal dcTerminal2 = dcLine.getDcTerminal2();
        context.getWriter().writeStringAttribute("dcNode1", dcTerminal1.getDcNode().getId());
        context.getWriter().writeStringAttribute("dcNode2", dcTerminal2.getDcNode().getId());
        context.getWriter().writeDoubleAttribute("r", dcLine.getR());
        context.getWriter().writeBooleanAttribute("connected1", dcTerminal1.isConnected());
        context.getWriter().writeDoubleAttribute("p1", dcTerminal1.getP());
        context.getWriter().writeDoubleAttribute("i1", dcTerminal1.getI());
        context.getWriter().writeBooleanAttribute("connected2", dcTerminal2.isConnected());
        context.getWriter().writeDoubleAttribute("p2", dcTerminal2.getP());
        context.getWriter().writeDoubleAttribute("i2", dcTerminal2.getI());
    }

    @Override
    protected DcLineAdder createAdder(final Network network) {
        return network.newDcLine();
    }

    @Override
    protected DcLine readRootElementAttributes(final DcLineAdder adder, final Network parent, final NetworkDeserializerContext context) {
        String dcNode1Id = context.getReader().readStringAttribute("dcNode1");
        String dcNode2Id = context.getReader().readStringAttribute("dcNode2");
        double r = context.getReader().readDoubleAttribute("r");
        boolean connected1 = context.getReader().readBooleanAttribute("connected1");
        double p1 = context.getReader().readDoubleAttribute("p1");
        double i1 = context.getReader().readDoubleAttribute("i1");
        boolean connected2 = context.getReader().readBooleanAttribute("connected2");
        double p2 = context.getReader().readDoubleAttribute("p2");
        double i2 = context.getReader().readDoubleAttribute("i2");
        DcLine dcLine = adder
                .setDcNode1(dcNode1Id)
                .setDcNode2(dcNode2Id)
                .setR(r)
                .add();
        dcLine.getDcTerminal1().setI(i1).setP(p1).setConnected(connected1);
        dcLine.getDcTerminal2().setI(i2).setP(p2).setConnected(connected2);
        return dcLine;
    }

    @Override
    protected void readSubElements(final DcLine dcLine, final NetworkDeserializerContext context) {
        context.getReader().readChildNodes(elementName -> readSubElement(elementName, dcLine, context));
    }
}
