/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.DcSwitch;
import com.powsybl.iidm.network.DcSwitchAdder;
import com.powsybl.iidm.network.DcSwitchKind;
import com.powsybl.iidm.network.DcTerminal;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.serde.util.IidmSerDeUtil;

import static com.powsybl.iidm.serde.ConnectableSerDeUtil.readPI;
import static com.powsybl.iidm.serde.ConnectableSerDeUtil.writePI;

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
        DcTerminal dcTerminal1 = dcSwitch.getDcTerminal1();
        DcTerminal dcTerminal2 = dcSwitch.getDcTerminal2();
        context.getWriter().writeStringAttribute("dcNode1", dcTerminal1.getDcNode().getId());
        context.getWriter().writeStringAttribute("dcNode2", dcTerminal2.getDcNode().getId());
        context.getWriter().writeEnumAttribute("kind", dcSwitch.getKind());
        context.getWriter().writeBooleanAttribute("open", dcSwitch.isOpen());
        IidmSerDeUtil.writeDoubleAttributeFromMinimumVersion(getRootElementName(), "r", dcSwitch.getR(), 0.0, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_17, context);
        IidmSerDeUtil.writeBooleanAttributeFromMinimumVersion(getRootElementName(),
                                               "connected1",
                                                              dcTerminal1.isConnected(),
                                                               true,
                                                               IidmSerDeUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED,
                                                               IidmVersion.V_1_18,
                                                               context);
        IidmSerDeUtil.writeBooleanAttributeFromMinimumVersion(getRootElementName(),
                                                "connected2",
                                                               dcTerminal2.isConnected(),
                                                               true,
                                                               IidmSerDeUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED,
                                                               IidmVersion.V_1_18,
                                                               context);
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_18, context, () -> {
            writePI(dcTerminal1, context.getWriter());
            writePI(dcTerminal2, context.getWriter());
        });
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

        // 0.0 Ohm as default value for IIDM version < 1.17
        double[] r = {0.0};
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_17, context,
            () -> r[0] = context.getReader().readDoubleAttribute("r"));

        // connected1/connected2 default to true for IIDM version < 1.18 (or when the attributes are absent)
        boolean[] connected1 = {true};
        boolean[] connected2 = {true};
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_18, context, () -> {
            connected1[0] = context.getReader().readBooleanAttribute("connected1", true);
            connected2[0] = context.getReader().readBooleanAttribute("connected2", true);
        });

        DcSwitch dcSwitch = adder.setDcNode1(dcNode1Id)
                    .setConnected1(connected1[0])
                    .setDcNode2(dcNode2Id)
                    .setConnected2(connected2[0])
                    .setKind(kind)
                    .setOpen(open)
                    .setR(r[0])
                    .add();

        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_18, context, () -> {
            readPI(dcSwitch.getDcTerminal1(), context.getReader());
            readPI(dcSwitch.getDcTerminal2(), context.getReader());
        });
        return dcSwitch;
    }

    @Override
    protected void readSubElements(final DcSwitch dcSwitch, final NetworkDeserializerContext context) {
        context.getReader().readChildNodes(elementName -> readSubElement(elementName, dcSwitch, context));
    }
}
