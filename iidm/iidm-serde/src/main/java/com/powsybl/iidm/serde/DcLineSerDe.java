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

import static com.powsybl.iidm.serde.ConnectableSerDeUtil.readPI;
import static com.powsybl.iidm.serde.ConnectableSerDeUtil.writePI;

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
        context.getWriter().writeBooleanAttribute("connected2", dcTerminal2.isConnected());
        writePI(dcTerminal1, context.getWriter());
        writePI(dcTerminal2, context.getWriter());
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
        boolean connected2 = context.getReader().readBooleanAttribute("connected2");
        DcLine dcLine = adder
                .setDcNode1(dcNode1Id)
                .setConnected1(connected1)
                .setDcNode2(dcNode2Id)
                .setConnected2(connected2)
                .setR(r)
                .add();
        readPI(dcLine.getDcTerminal1(), context.getReader());
        readPI(dcLine.getDcTerminal2(), context.getReader());
        return dcLine;
    }

    @Override
    protected void readSubElements(final DcLine dcLine, final NetworkDeserializerContext context) {
        context.getReader().readChildNodes(elementName -> readSubElement(elementName, dcLine, context));
    }
}
