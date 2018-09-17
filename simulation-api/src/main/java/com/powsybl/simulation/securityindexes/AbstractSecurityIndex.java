/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.simulation.securityindexes;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class AbstractSecurityIndex implements SecurityIndex {

    protected static final Supplier<XMLOutputFactory> XML_OUTPUT_FACTORY_SUPPLIER = Suppliers.memoize(XMLOutputFactory::newInstance);

    protected final SecurityIndexId id;

    protected AbstractSecurityIndex(String contingencyId, SecurityIndexType securityIndexType) {
        id = new SecurityIndexId(contingencyId, securityIndexType);
    }

    @Override
    public SecurityIndexId getId() {
        return id;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("SecurityIndex(id=");
        builder.append(id);
        toMap().entrySet().forEach(e -> builder.append(", ").append(e.getKey()).append("=").append(e.getValue()));
        builder.append(")");
        return builder.toString();
    }

    protected abstract void toXml(XMLStreamWriter xmlWriter) throws XMLStreamException;

    @Override
    public String toXml() {
        StringWriter writer = new StringWriter();
        try {
            XMLStreamWriter xmlWriter = XML_OUTPUT_FACTORY_SUPPLIER.get().createXMLStreamWriter(writer);
            try {
                toXml(xmlWriter);
            } finally {
                xmlWriter.close();
            }
            writer.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
        return writer.toString();
    }

}
