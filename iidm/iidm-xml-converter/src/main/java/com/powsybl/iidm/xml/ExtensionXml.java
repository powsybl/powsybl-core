/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.xml;

import eu.itesla_project.iidm.network.Identifiable;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface ExtensionXml<I extends Identifiable, E extends Identifiable.Extension<I>> {

    String getExtensionName();

    Class<? super E> getExtensionClass();

    boolean hasSubElements();

    InputStream getXsdAsStream();

    String getNamespaceUri();

    String getNamespacePrefix();

    void write(E extension, XmlWriterContext context) throws XMLStreamException;

    E read(I identifiable, XmlReaderContext context) throws XMLStreamException;
}
