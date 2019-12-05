/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.extensions;

import com.powsybl.commons.xml.IidmXmlVersion;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlWriterContext;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.util.List;

import static com.powsybl.commons.xml.IidmXmlConstants.CURRENT_IIDM_XML_VERSION;

/**
 * An ExtensionProvider able to serialize/deserialize extensions from XML.
 *
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public interface ExtensionXmlSerializer<T extends Extendable, E extends Extension<T>> extends ExtensionProvider<T, E> {

    boolean hasSubElements();

    /**
     * @deprecated Use {@link #getXsdAsStreamList()} instead.
     */
    @Deprecated
    default InputStream getXsdAsStream() {
        throw new UnsupportedOperationException("Deprecated");
    }

    default List<InputStream> getXsdAsStreamList() {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated Use {@link #getNamespaceUri(IidmXmlVersion)} instead.
     */
    @Deprecated
    default String getNamespaceUri() {
        return getNamespaceUri(CURRENT_IIDM_XML_VERSION);
    }

    default String getNamespaceUri(IidmXmlVersion version) {
        throw new UnsupportedOperationException();
    }

    String getNamespacePrefix();

    void write(E extension, XmlWriterContext context) throws XMLStreamException;

    E read(T extendable, XmlReaderContext context) throws XMLStreamException;
}
