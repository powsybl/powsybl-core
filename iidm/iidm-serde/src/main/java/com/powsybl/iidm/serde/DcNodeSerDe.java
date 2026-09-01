/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.serde.util.IidmSerDeUtil;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public class DcNodeSerDe extends AbstractSimpleIdentifiableSerDe<DcNode, DcNodeAdder, Network> {

    static final DcNodeSerDe INSTANCE = new DcNodeSerDe();
    static final String ROOT_ELEMENT_NAME = "dcNode";
    static final String ARRAY_ELEMENT_NAME = "dcNodes";
    private static final String NOMINAL_V = "nominalV";
    private static final String LOW_VOLTAGE_LIMIT = "lowVoltageLimit";
    private static final String HIGH_VOLTAGE_LIMIT = "highVoltageLimit";
    private static final String V = "v";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(final DcNode dcNode, final Network parent, final NetworkSerializerContext context) {
        context.getWriter().writeDoubleAttribute(NOMINAL_V, dcNode.getNominalV());
        IidmSerDeUtil.writeDoubleAttributeFromMinimumVersion(getRootElementName(), LOW_VOLTAGE_LIMIT, dcNode.getLowVoltageLimit(),
                IidmSerDeUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmVersion.V_1_18, context);
        IidmSerDeUtil.writeDoubleAttributeFromMinimumVersion(getRootElementName(), HIGH_VOLTAGE_LIMIT, dcNode.getHighVoltageLimit(),
                IidmSerDeUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmVersion.V_1_18, context);
        context.getWriter().writeDoubleAttribute(V, dcNode.getV());
    }

    @Override
    protected DcNodeAdder createAdder(final Network network) {
        return network.newDcNode();
    }

    @Override
    protected DcNode readRootElementAttributes(final DcNodeAdder adder, final Network parent, final NetworkDeserializerContext context) {
        double nominalV = context.getReader().readDoubleAttribute(NOMINAL_V);
        // undefined voltage limits as default value for IIDM version < 1.18 and for DC nodes declaring no limit
        double[] lowVoltageLimit = {Double.NaN};
        double[] highVoltageLimit = {Double.NaN};
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_18, context, () -> {
            lowVoltageLimit[0] = context.getReader().readDoubleAttribute(LOW_VOLTAGE_LIMIT);
            highVoltageLimit[0] = context.getReader().readDoubleAttribute(HIGH_VOLTAGE_LIMIT);
        });
        double v = context.getReader().readDoubleAttribute(V);
        DcNode dcNode = adder
                .setNominalV(nominalV)
                .setLowVoltageLimit(lowVoltageLimit[0])
                .setHighVoltageLimit(highVoltageLimit[0])
                .add();
        dcNode.setV(v);
        return dcNode;
    }

    @Override
    protected void readSubElements(final DcNode dcNode, final NetworkDeserializerContext context) {
        context.getReader().readChildNodes(elementName -> readSubElement(elementName, dcNode, context));
    }
}
