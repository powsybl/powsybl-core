/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.securityindexes;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import java.io.IOException;
import java.io.StringWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class AbstractSecurityIndex implements SecurityIndex {

    protected final static Supplier<XMLOutputFactory> xmlof = Suppliers.memoize(new Supplier<XMLOutputFactory>() {
        @Override
        public XMLOutputFactory get() {
            return XMLOutputFactory.newInstance();
        }
    });

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

    abstract protected void toXml(XMLStreamWriter xmlWriter) throws XMLStreamException;

    @Override
    public String toXml() {
        StringWriter writer = new StringWriter();
        try {
            XMLStreamWriter xmlWriter = xmlof.get().createXMLStreamWriter(writer);
            try {
                toXml(xmlWriter);
            } finally {
                xmlWriter.close();
            }
            writer.close();
        } catch (IOException | XMLStreamException e) {
            throw new RuntimeException(e);
        }
        return writer.toString();
    }

}
