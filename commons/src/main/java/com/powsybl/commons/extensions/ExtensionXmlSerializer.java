/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.extensions;

import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlWriterContext;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;

/**
 * An ExtensionProvider able to serialize/deserialize extensions from XML.
 *
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public interface ExtensionXmlSerializer<T extends Extendable, E extends Extension<T>> extends ExtensionProvider<T, E> {

    boolean hasSubElements();

    InputStream getXsdAsStream();

    String getNamespaceUri();

    String getNamespacePrefix();

    void write(E extension, XmlWriterContext context) throws XMLStreamException;

    E read(T extendable, XmlReaderContext context) throws XMLStreamException;
}
