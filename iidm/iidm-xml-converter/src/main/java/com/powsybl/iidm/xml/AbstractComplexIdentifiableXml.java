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

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Abstract class for equipment that need their sub-elements to be entirely created.
 *
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
abstract class AbstractComplexIdentifiableXml<T extends Identifiable<T>, A extends IdentifiableAdder<T, A>, P extends Identifiable> extends AbstractIdentifiableXml<T, A, P> {

    protected abstract void readRootElementAttributes(A adder, List<Consumer<T>> toApply, NetworkXmlReaderContext context);

    protected void readSubElements(String id, List<Consumer<T>> toApply, NetworkXmlReaderContext context) throws XMLStreamException {
        if (context.getReader().getLocalName().equals(PropertiesXml.PROPERTY)) {
            PropertiesXml.read(toApply, context);
        } else if (context.getReader().getLocalName().equals(AliasesXml.ALIAS)) {
            IidmXmlUtil.assertMinimumVersion(getRootElementName(), AliasesXml.ALIAS, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_3, context);
            AliasesXml.read(toApply, context);
        } else {
            throw new PowsyblException("Unknown element name <" + context.getReader().getLocalName() + "> in <" + id + ">");
        }
    }

    protected abstract void readSubElements(String id, A adder, List<Consumer<T>> toApply, NetworkXmlReaderContext context) throws XMLStreamException;

    @Override
    public final void read(P parent, NetworkXmlReaderContext context) throws XMLStreamException {
        List<Consumer<T>> toApply = new ArrayList<>();
        A adder = createAdder(parent);
        String id = readIdentifierAttributes(adder, context);
        readRootElementAttributes(adder, toApply, context);
        readSubElements(id, adder, toApply, context);
        T identifiable = adder.add();
        toApply.forEach(consumer -> consumer.accept(identifiable));
    }
}
