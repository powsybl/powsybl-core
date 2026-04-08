/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.IdentifiableAdder;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.serde.util.IidmSerDeUtil;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
abstract class AbstractSwitchSerDe<A extends IdentifiableAdder<Switch, A>> extends AbstractSimpleIdentifiableSerDe<Switch, A, VoltageLevel> {

    static final String ROOT_ELEMENT_NAME = "switch";
    static final String ARRAY_ELEMENT_NAME = "switches";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(Switch s, VoltageLevel vl, NetworkSerializerContext context) {
        context.getWriter().writeEnumAttribute("kind", s.getKind());
        context.getWriter().writeBooleanAttribute("retained", s.isRetained());
        context.getWriter().writeBooleanAttribute("open", s.isOpen());

        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_1, context, () -> context.getWriter().writeBooleanAttribute("fictitious", s.isFictitious(), false));
    }

    @Override
    protected void readSubElements(Switch s, NetworkDeserializerContext context) {
        context.getReader().readChildNodes(elementName -> readSubElement(elementName, s, context));
    }
}
