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
public class DcSwitchSerDe extends AbstractSimpleIdentifiableSerDe<DcSwitch, DcSwitchAdder, Network> {

    static final DcSwitchSerDe INSTANCE = new DcSwitchSerDe();
    static final String ROOT_ELEMENT_NAME = "dcSwitch";
    static final String ARRAY_ELEMENT_NAME = "dcSwitches";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(final DcSwitch dcSwitch, final Network parent, final NetworkSerializerContext context) {
        context.getWriter().writeStringAttribute("dcNode1", dcSwitch.getDcNode1().getId());
        context.getWriter().writeStringAttribute("dcNode2", dcSwitch.getDcNode2().getId());
        context.getWriter().writeEnumAttribute("kind", dcSwitch.getKind());
        context.getWriter().writeBooleanAttribute("open", dcSwitch.isOpen());
    }

    @Override
    protected DcSwitchAdder createAdder(final Network network) {
        return network.newDcSwitch();
    }

    @Override
    protected DcSwitch readRootElementAttributes(final DcSwitchAdder adder, final Network parent, final NetworkDeserializerContext context) {
        String dcNode1Id = context.getReader().readStringAttribute("dcNode1");
        String dcNode2Id = context.getReader().readStringAttribute("dcNode2");
        DcSwitchKind kind = context.getReader().readEnumAttribute("kind", DcSwitchKind.class);
        boolean open = context.getReader().readBooleanAttribute("open");
        return adder
                .setDcNode1(dcNode1Id)
                .setDcNode2(dcNode2Id)
                .setKind(kind)
                .setOpen(open)
                .add();
    }

    @Override
    protected void readSubElements(final DcSwitch dcSwitch, final NetworkDeserializerContext context) {
        context.getReader().readChildNodes(elementName -> readSubElement(elementName, dcSwitch, context));
    }
}
