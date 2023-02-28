/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.MergedDanglingLineAdder;
import com.powsybl.iidm.network.TieLine;

import javax.xml.stream.XMLStreamException;

import java.util.List;
import java.util.function.Consumer;

import static com.powsybl.iidm.xml.DanglingLineXml.hasValidGeneration;

/**
 * @author Bertrand Rix <bertrand.rix at artelys.com>
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public final class MergedDanglingLineXml extends AbstractIdentifiableXml<DanglingLine, MergedDanglingLineAdder, TieLine> {

    static final MergedDanglingLineXml INSTANCE_1 = new MergedDanglingLineXml(1);
    static final MergedDanglingLineXml INSTANCE_2 = new MergedDanglingLineXml(2);

    private final int side;

    private MergedDanglingLineXml(int side) {
        this.side = side;
    }

    void read(MergedDanglingLineAdder adder, List<Consumer<DanglingLine>> subElements, NetworkXmlReaderContext context) throws XMLStreamException {
        String id = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "id"));
        String name = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "name"));
        adder.setId(id)
                .setName(name);
        boolean fictitious = XmlUtil.readOptionalBoolAttribute(context.getReader(), "fictitious", false);
        adder.setFictitious(fictitious);
        double p = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "p");
        double q = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "q");
        subElements.add(dl -> dl.getParentTerminal().ifPresent(t -> t.setP(p)));
        subElements.add(dl -> dl.getParentTerminal().ifPresent(t -> t.setQ(q)));
        readElement(id, adder, context);

        readUntilEndRootElement(context.getReader(), () -> {
            if (context.getReader().getLocalName().equals(PropertiesXml.PROPERTY)) {
                String pName = context.getReader().getAttributeValue(null, PropertiesXml.NAME);
                String value = context.getReader().getAttributeValue(null, PropertiesXml.VALUE);
                subElements.add(dl -> dl.setProperty(pName, value));
            } else if (context.getReader().getLocalName().equals(AliasesXml.ALIAS)) {
                String aliasType = context.getReader().getAttributeValue(null, "type");
                String alias = context.getAnonymizer().deanonymizeString(context.getReader().getElementText());
                subElements.add(dl -> dl.addAlias(alias, aliasType));
            } else {
                throw new PowsyblException("Unknown element name <" + context.getReader().getLocalName() + "> in <" + id + ">");
            }
        });
    }

    @Override
    protected String getRootElementName() {
        return DanglingLineXml.ROOT_ELEMENT_NAME + side;
    }

    @Override
    protected boolean hasSubElements(DanglingLine dl) {
        throw new IllegalStateException("Should not be called");
    }

    @Override
    protected boolean hasSubElements(DanglingLine identifiable, NetworkXmlWriterContext context) {
        return hasValidGeneration(identifiable, context);
    }

    @Override
    protected void writeRootElementAttributes(DanglingLine identifiable, TieLine parent, NetworkXmlWriterContext context) throws XMLStreamException {
        DanglingLineXml.writeRootElementAttributesInternal(identifiable, () -> identifiable.getParentTerminal().orElseThrow(IllegalStateException::new), context);
    }

    @Override
    protected MergedDanglingLineAdder createAdder(TieLine parent) {
        throw new UnsupportedOperationException(); // is never called
    }

    @Override
    protected DanglingLine readRootElementAttributes(MergedDanglingLineAdder adder, NetworkXmlReaderContext context) {
        throw new UnsupportedOperationException(); // is never called
    }

    @Override
    protected void readElement(String id, MergedDanglingLineAdder adder, NetworkXmlReaderContext context) {
        DanglingLineXml.readRootElementAttributesInternal(adder, context);
        adder.add();
    }
}
