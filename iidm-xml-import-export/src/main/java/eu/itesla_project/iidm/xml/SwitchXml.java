/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.xml;

import eu.itesla_project.iidm.network.IdentifiableAdder;
import eu.itesla_project.iidm.network.Switch;
import eu.itesla_project.iidm.network.VoltageLevel;

import javax.xml.stream.XMLStreamException;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class SwitchXml<A extends IdentifiableAdder<A>> extends IdentifiableXml<Switch, A, VoltageLevel> {

    static final String ROOT_ELEMENT_NAME = "switch";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected boolean hasSubElements(Switch s) {
        return false;
    }

    @Override
    protected void writeRootElementAttributes(Switch s, VoltageLevel vl, XmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeAttribute("kind", s.getKind().name());
        context.getWriter().writeAttribute("retained", Boolean.toString(s.isRetained()));
        context.getWriter().writeAttribute("open", Boolean.toString(s.isOpen()));
    }

    @Override
    protected void writeSubElements(Switch s, VoltageLevel vl, XmlWriterContext context) throws XMLStreamException {
    }

    @Override
    protected void readSubElements(Switch s, XmlReaderContext context) throws XMLStreamException {
        readUntilEndRootElement(context.getReader(), () -> SwitchXml.super.readSubElements(s, context));
    }
}
