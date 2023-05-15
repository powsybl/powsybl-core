/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.IdentifiableAdder;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

/**
 * Abstract class for equipment that can and/or must be entirely created before reading their sub-elements.
 *
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
abstract class AbstractSimpleIdentifiableXml<T extends Identifiable<? super T>, A extends IdentifiableAdder<T, A>, P extends Identifiable> extends AbstractIdentifiableXml<T, A, P> {

    protected abstract T readRootElementAttributes(A adder, P parent, NetworkXmlReaderContext context);

    protected void readSubElements(T identifiable, NetworkXmlReaderContext context) {
        if (context.getReader().getNodeName().equals(PropertiesXml.PROPERTY)) {
            PropertiesXml.read(identifiable, context);
        } else if (context.getReader().getNodeName().equals(AliasesXml.ALIAS)) {
            IidmXmlUtil.assertMinimumVersion(getRootElementName(), AliasesXml.ALIAS, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_3, context);
            AliasesXml.read(identifiable, context);
        } else {
            throw new PowsyblException("Unknown element name <" + context.getReader().getNodeName() + "> in <" + identifiable.getId() + ">");
        }
    }

    @Override
    public final void read(P parent, NetworkXmlReaderContext context) {
        A adder = createAdder(parent);
        readIdentifierAttributes(adder, context);
        T identifiable = readRootElementAttributes(adder, parent, context);
        if (identifiable != null) {
            readSubElements(identifiable, context);
        }
    }
}
