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
public class DcNodeSerDe extends AbstractSimpleIdentifiableSerDe<DcNode, DcNodeAdder, Network> {

    static final DcNodeSerDe INSTANCE = new DcNodeSerDe();
    static final String ROOT_ELEMENT_NAME = "dcNode";
    static final String ARRAY_ELEMENT_NAME = "dcNodes";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(final DcNode dcNode, final Network parent, final NetworkSerializerContext context) {
        context.getWriter().writeDoubleAttribute("nominalV", dcNode.getNominalV());
        context.getWriter().writeDoubleAttribute("v", dcNode.getV());
    }

    @Override
    protected void writeSubElements(final DcNode dcNode, final Network parent, final NetworkSerializerContext context) {

    }

    @Override
    protected DcNodeAdder createAdder(final Network network) {
        return network.newDcNode();
    }

    @Override
    protected DcNode readRootElementAttributes(final DcNodeAdder adder, final Network parent, final NetworkDeserializerContext context) {
        double nominalV = context.getReader().readDoubleAttribute("nominalV");
        double v = context.getReader().readDoubleAttribute("v");
        DcNode dcNode = adder
                .setNominalV(nominalV)
                .add();
        dcNode.setV(v);
        return dcNode;
    }

    @Override
    protected void readSubElements(final DcNode dcNode, final NetworkDeserializerContext context) {
        context.getReader().readChildNodes(elementName -> readSubElement(elementName, dcNode, context));
    }
}
