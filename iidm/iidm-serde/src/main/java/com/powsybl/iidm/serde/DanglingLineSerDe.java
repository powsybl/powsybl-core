/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.BoundaryLine;
import com.powsybl.iidm.network.BoundaryLineAdder;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.serde.util.IidmSerDeUtil;

/**
 * Used for backward-compatibility with IIDM < 1.16
 *
 * @author Hugo Kulesza {@literal <hugo.kulesza at rte-france.com>}
 */

class DanglingLineSerDe extends BoundaryLineSerDe {

    static final String ROOT_ELEMENT_NAME = "danglingLine";
    static final String ARRAY_ELEMENT_NAME = "danglingLines";

    static final DanglingLineSerDe INSTANCE = new DanglingLineSerDe();

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(BoundaryLine danglingLine, VoltageLevel parent, NetworkSerializerContext context) {
        writeRootElementAttributesInternal(INSTANCE.getRootElementName(), danglingLine, danglingLine::getTerminal, context);
    }

    @Override
    protected BoundaryLine readRootElementAttributes(BoundaryLineAdder adder, VoltageLevel voltageLevel, NetworkDeserializerContext context) {
        IidmSerDeUtil.assertMaximumVersion(getRootElementName(), IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_15, context);
        return readRootElementAttributesInternal(adder, voltageLevel, context);
    }
}
