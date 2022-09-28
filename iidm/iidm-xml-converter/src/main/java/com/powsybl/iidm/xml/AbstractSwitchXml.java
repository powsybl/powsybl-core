/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.iidm.network.IdentifiableAdder;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class AbstractSwitchXml<A extends IdentifiableAdder<A>> extends AbstractIdentifiableXml<Switch, A, VoltageLevel> {

    static final String ROOT_ELEMENT_NAME = "switch";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(Switch s, VoltageLevel vl, NetworkXmlWriterContext context) {
        context.getWriter().writeEnumAttribute("kind", s.getKind());
        context.getWriter().writeBooleanAttribute("retained", s.isRetained());
        context.getWriter().writeBooleanAttribute("open", s.isOpen());

        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_1, context, () -> context.getWriter().writeBooleanAttribute("fictitious", s.isFictitious(), false));
    }

    @Override
    protected void readSubElements(Switch s, NetworkXmlReaderContext context) {
        context.getReader().readUntilEndNode(getRootElementName(), () -> AbstractSwitchXml.super.readSubElements(s, context));
    }
}
