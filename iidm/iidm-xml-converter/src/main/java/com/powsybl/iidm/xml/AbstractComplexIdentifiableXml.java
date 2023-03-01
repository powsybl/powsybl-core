/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.xml;

import com.google.common.base.Supplier;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.IdentifiableAdder;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
abstract class AbstractComplexIdentifiableXml<T extends Identifiable, A extends IdentifiableAdder<A>, P extends Identifiable> extends AbstractIdentifiableXml<T, A, P> {

    protected void readUntilEndRootElement(XMLStreamReader reader, XmlUtil.XmlEventHandler eventHandler) throws XMLStreamException {
        XmlUtil.readUntilEndElement(getRootElementName(), reader, eventHandler);
    }

    protected abstract void readRootElementAttributes(A adder, List<Consumer<T>> toApply, NetworkXmlReaderContext context);

    protected void readSubElements(String id, A adder, List<Consumer<T>> toApply, NetworkXmlReaderContext context) throws XMLStreamException {
        if (context.getReader().getLocalName().equals(PropertiesXml.PROPERTY)) {
            PropertiesXml.read(toApply, context);
        } else if (context.getReader().getLocalName().equals(AliasesXml.ALIAS)) {
            IidmXmlUtil.assertMinimumVersion(getRootElementName(), AliasesXml.ALIAS, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_3, context);
            AliasesXml.read(toApply, context);
        } else {
            throw new PowsyblException("Unknown element name <" + context.getReader().getLocalName() + "> in <" + id + ">");
        }
    }

    protected void readElement(String id, A adder, List<Consumer<T>> toApply, NetworkXmlReaderContext context) throws XMLStreamException {
        readRootElementAttributes(adder, toApply, context);
        readSubElements(id, adder, toApply, context);
    }

    protected void apply(T identifiable, List<Consumer<T>> toApply) {
        toApply.forEach(consumer -> consumer.accept(identifiable));
    }

    public final void read(Supplier<A> createAdder, Function<A, T> create, NetworkXmlReaderContext context) throws XMLStreamException {
        List<Consumer<T>> toApply = new ArrayList<>();
        A adder = read(createAdder, toApply, context);
        T identifiable = create.apply(adder);
        apply(identifiable, toApply);
    }

    public final A read(Supplier<A> createAdder, List<Consumer<T>> toApply, NetworkXmlReaderContext context) throws XMLStreamException {
        A adder = createAdder.get();
        read(adder, toApply, context);
        return adder;
    }

    private void read(A adder, List<Consumer<T>> toApply, NetworkXmlReaderContext context) throws XMLStreamException {
        String id = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "id"));
        String name = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "name"));
        adder.setId(id)
                .setName(name);
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_2, context, () -> {
            boolean fictitious = XmlUtil.readOptionalBoolAttribute(context.getReader(), "fictitious", false);
            adder.setFictitious(fictitious);
        });
        readElement(id, adder, toApply, context);
    }
}
